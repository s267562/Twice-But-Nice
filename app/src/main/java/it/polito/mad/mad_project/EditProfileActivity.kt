package it.polito.mad.mad_project

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_edit_profile.*
import java.io.ByteArrayOutputStream

import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Environment
import android.os.PersistableBundle
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_edit_profile.email
import kotlinx.android.synthetic.main.activity_edit_profile.full_name
import kotlinx.android.synthetic.main.activity_edit_profile.location
import kotlinx.android.synthetic.main.activity_edit_profile.nickname
import kotlinx.android.synthetic.main.activity_edit_profile.user_photo
import kotlinx.android.synthetic.main.activity_show_profile.*
import java.io.File
import java.io.IOException
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*

class EditProfileActivity : AppCompatActivity() {

    private var currentPath: String? = null
    private var photo: Bitmap?= null
    private val CAPTURE_IMAGE_REQUEST = 1
    val SELECT_IMAGE = 2
    private var imageFile: File? = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        registerForContextMenu(camera_icon)

        camera_icon.isLongClickable = false
        camera_icon.setOnTouchListener { v, event ->
            if (v is ImageButton && event.action == MotionEvent.ACTION_DOWN) {
                v.showContextMenu(event.x, event.y)
            }
            true
        }

        // Load user information
        val user: User? = intent.getSerializableExtra(IntentRequest.UserData.NAME) as? User?
        Log.d ("MAD_LOG", "RECEIVED-USER: $user")

        if (savedInstanceState != null) {
            photo = savedInstanceState.getParcelable("Photo")
        }

        if (user != null) {
            full_name.setText(user.name)
            nickname.setText(user.nickname)
            email.setText(user.email)
            location.setText(user.location)
            if (user.photoProfilePath != null && user.photoProfilePath.isNotEmpty()) {
                val image: Bitmap = BitmapFactory.decodeFile(user.photoProfilePath)
                if (image != null) user_photo.setImageBitmap(image)
            }
        }else if (this.photo != null){
            user_photo.setImageBitmap(photo)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.edit_profile_menu, menu)
        return true
    }

    // Punto 4
    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val menuInflater: MenuInflater = menuInflater
        menuInflater.inflate(R.menu.context_menu, menu)
        menu.setHeaderTitle("Context Menu")
    }

    // Punto 5
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.save_option -> {
                Toast.makeText(this, "Save button clicked", Toast.LENGTH_SHORT).show()
                saveProfile()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.select_image -> {
                openGallery()
                true
            }
            R.id.take_pic -> {
                openCamera()
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun openGallery(){
        val galleryIntent = Intent()
        galleryIntent.type = "image/*"
        galleryIntent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(galleryIntent, "Select an image from Gallery"), SELECT_IMAGE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 0){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                openCamera()
            } else {
                displayMessage(baseContext, "Camera Permission has been denied")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Open Camera
        if (requestCode == CAPTURE_IMAGE_REQUEST && resultCode == Activity.RESULT_OK){
            try {
                val file = File(this.currentPath)
                val uri:Uri = Uri.fromFile(file)
                var rotatedBitmap: Bitmap?
                rotatedBitmap = handleSamplingAndRotationBitmap(applicationContext, selectedImage = uri)
                user_photo.setImageBitmap(rotatedBitmap)
                this.photo = rotatedBitmap
            }catch (e: IOException){
                e.printStackTrace()
            }
        }
        // Open Gallery
        if (requestCode == SELECT_IMAGE && resultCode == Activity.RESULT_OK){
            try {
                val uriPic = data?.data
                user_photo.setImageURI(uriPic)
            }catch (e: IOException){
                e.printStackTrace()
            }
        } else {
            displayMessage(baseContext, "Select image wrong")
        }
    }
    /**
     * This method is responsible for solving the rotation issue if exist. Also scale the images to
     * 1024x1024 resolution
     *
     * @param context The current context
     * @param selectedImage The Image URI
     * @return Bitmap image results
     * @throws IOException
     */
    @Throws(IOException::class)
    fun handleSamplingAndRotationBitmap(context:Context, selectedImage:Uri): Bitmap? {
        val MAX_HEIGHT = 1024
        val MAX_WIDTH = 1024
        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        var imageStream = context.getContentResolver().openInputStream(selectedImage)
        BitmapFactory.decodeStream(imageStream, null, options)
        if (imageStream != null) {
            imageStream.close()
        }
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT)
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        imageStream = context.getContentResolver().openInputStream(selectedImage)
        var img = BitmapFactory.decodeStream(imageStream, null, options)
        img = img?.let { rotateImageIfRequired(context, it, selectedImage) }
        return img
    }

    /**
     * Calculate an inSampleSize for use in a {@link BitmapFactory.Options} object when decoding
     * bitmaps using the decode* methods from {@link BitmapFactory}. This implementation calculates
     * the closest inSampleSize that will result in the final decoded bitmap having a width and
     * height equal to or larger than the requested width and height. This implementation does not
     * ensure a power of 2 is returned for inSampleSize which can be faster when decoding but
     * results in a larger bitmap which isn't as useful for caching purposes.
     *
     * @param options An options object with out* params already populated (run through a decode*
     * method with inJustDecodeBounds==true
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return The value to be used for inSampleSize
     */
    private fun calculateInSampleSize(options:BitmapFactory.Options,
                                      reqWidth:Int, reqHeight:Int):Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth)
        {
            // Calculate ratios of height and width to requested height and width
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
            // Choose the smallest ratio as inSampleSize value, this will guarantee a final image
            // with both dimensions larger than or equal to the requested height and width.
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).
            val totalPixels = (width * height).toFloat()
            // Anything more than 2x the requested pixels we'll sample down further
            val totalReqPixelsCap = (reqWidth * reqHeight * 2).toFloat()
            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap)
            {
                inSampleSize++
            }
        }
        return inSampleSize
    }

    /**
     * Rotate an image if required.
     *
     * @param img The image bitmap
     * @param selectedImage Image URI
     * @return The resulted Bitmap after manipulation
     */
    @Throws(IOException::class)
    private fun rotateImageIfRequired(context:Context, img:Bitmap, selectedImage:Uri): Bitmap? {
        val input = context.getContentResolver().openInputStream(selectedImage)
        val ei:ExifInterface
        if (Build.VERSION.SDK_INT > 23)
            ei = ExifInterface(input)
        else
            ei = ExifInterface(selectedImage.getPath())
        val orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> return rotateImage(img, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> return rotateImage(img, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> return rotateImage(img, 270)
            else -> return img
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(this.photo != null){
            outState.putParcelable("Photo", this.photo)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Log.i("TeamSVIK",  "${imageFile?.delete()}")

    }

    // point 6
    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
        } else {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (cameraIntent.resolveActivity(packageManager) != null) {
                // Create the File where the photo should go
                try {
                    imageFile = createImageFile()
                    displayMessage(baseContext, imageFile!!.absolutePath)
                    Log.i("TeamSVIK", imageFile!!.absolutePath)

                    // Continue only if the File was successfully created
                    if (imageFile != null) {
                        var photoURI = FileProvider.getUriForFile(this,
                            "it.polito.mad.mad_project",
                            imageFile!!
                        )
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                        startActivityForResult(cameraIntent, CAPTURE_IMAGE_REQUEST)
                    }
                } catch (ex: Exception) {
                    // Error occurred while creating the File
                    displayMessage(baseContext,"Capture Image Bug: "  + ex.message.toString())
                }
            } else {
                displayMessage(baseContext, "Camera Intent Resolve Activity is null.")
            }
        }
    }

    private fun displayMessage(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName, /* prefix */
            ".jpg", /* suffix */
            storageDir      /* directory */
        )
        currentPath = image.absolutePath
        return image
    }

    private fun rotateImage(img:Bitmap, degree:Int):Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true)
        img.recycle()
        return rotatedImg
    }

    // punto 7
    private fun saveProfile() {
        val name = full_name.text.toString()
        val nickname = nickname.text.toString()
        val email = email.text.toString()
        val location = location.text.toString()
        val user = User(name, "", nickname, email, location, imageFile?.absolutePath)

        Log.d ("MAD_LOG", "SEND-USER: $user")

        val intent = Intent()
        intent.putExtra(IntentRequest.UserData.NAME, user)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

}
