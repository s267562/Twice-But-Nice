package it.polito.mad.project.activities.profile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.ImageButton
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.fragment_edit_profile.*

import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Environment
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import it.polito.mad.project.enums.IntentRequest
import it.polito.mad.project.R
import it.polito.mad.project.models.User
import kotlinx.android.synthetic.main.fragment_edit_profile.email
import kotlinx.android.synthetic.main.fragment_edit_profile.full_name
import kotlinx.android.synthetic.main.fragment_edit_profile.location
import kotlinx.android.synthetic.main.fragment_edit_profile.nickname
import kotlinx.android.synthetic.main.fragment_edit_profile.user_photo
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class EditProfileActivity : AppCompatActivity() {

    val SELECT_IMAGE = 2
    private var imageFile: File? = null
    private var imagePath: String? = null
    private var savedImagePath: String? =null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_edit_profile)
        registerForContextMenu(camera_button)

        supportActionBar!!.elevation = 0f

        camera_button.isLongClickable = false
        camera_button.setOnTouchListener { v, event ->
            if (v is ImageButton && event.action == MotionEvent.ACTION_DOWN) {
                v.showContextMenu(event.x, event.y)
            }
            true
        }

        // Load user information
        val user: User? = intent.getSerializableExtra(
            IntentRequest.UserData.NAME
        ) as? User?

        Log.d ("MAD_LOG", "RECEIVED-USER: $user")

        if (savedInstanceState != null) {
            imagePath =savedInstanceState.getString("ImagePath")
        }
        var image: Bitmap?=null
        if (user != null) {
            full_name.setText(user.name)
            nickname.setText(user.nickname)
            email.setText(user.email)
            location.setText(user.location)
            if (user.photoProfilePath != null && user.photoProfilePath.isNotEmpty()) {
                savedImagePath = user.photoProfilePath
                image = BitmapFactory.decodeFile(user.photoProfilePath)
                if (image != null) user_photo.setImageBitmap(image)
            }
        }
        if (this.imagePath != null){
            image = BitmapFactory.decodeFile(imagePath)
            this.user_photo.setImageBitmap(image)
        }else{
            if (user != null) {
                imagePath = user.photoProfilePath
            }
        }

        rotation_button.setOnClickListener{
            user_photo.setDrawingCacheEnabled(true)
            image = user_photo.getDrawingCache(true).copy(Bitmap.Config.ARGB_8888, false)
            user_photo.destroyDrawingCache()
            var rotateBitmap = rotateImage(image!!, 90)
            image = rotateBitmap
            user_photo.setImageBitmap(image)
        }

        if (image == null){
            rotation_button.visibility=View.GONE
        } else {
            rotation_button.visibility=View.VISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.save_menu, menu)
        return true
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val menuInflater: MenuInflater = menuInflater
        menuInflater.inflate(R.menu.camera_menu, menu)
        menu.setHeaderTitle("Context Menu")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.save_option -> {
                //displayMessage(baseContext, "Save button clicked")
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
                //displayMessage(baseContext, "Camera Permission has been denied")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        rotation_button.visibility=View.VISIBLE

        // Open Camera
        if (requestCode == IntentRequest.UserImage.CODE && resultCode == Activity.RESULT_OK){
            val file = File(this.imagePath)
            val uri:Uri = Uri.fromFile(file)
            user_photo.setImageURI(uri)
        }
        // Open Gallery
        else if (requestCode == SELECT_IMAGE && resultCode == Activity.RESULT_OK){
            val uriPic = data?.data
            user_photo.setImageURI(uriPic)
            if (uriPic != null) {
                val file: File = createImageFile()
                val fOut: FileOutputStream = FileOutputStream(file)
                imageFile = file
                imagePath = file.absolutePath
                var mBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uriPic)
                mBitmap.compress(Bitmap.CompressFormat.JPEG,100,fOut)
            }

        } else {
            //displayMessage(baseContext, "Select image wrong")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (this.imagePath != null) {
            outState.putString("ImagePath", this.imagePath)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing && imagePath!=null && imagePath!=savedImagePath){
           File(imagePath).delete()
        }else{
            //it's an orientation change
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
                imageFile = createImageFile()
                //displayMessage(baseContext, imageFile!!.absolutePath)
                Log.i("TeamSVIK", imageFile!!.absolutePath)

                // Continue only if the File was successfully created
                if (imageFile != null) {
                    imagePath = imageFile!!.absolutePath
                    var photoURI = FileProvider.getUriForFile(this,
                        "it.polito.mad.project",
                        imageFile!!
                    )
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(cameraIntent, IntentRequest.UserImage.CODE)
                }
            } else {
                //displayMessage(baseContext, "Camera Intent Resolve Activity is null.")
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        if(imagePath !=null && imagePath != savedImagePath){
            File(imagePath).delete()
            //displayMessage(baseContext, "file deleted")
        }
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName, /* prefix */
            ".jpg", /* suffix */
            storageDir      /* directory */
        )
        this.imagePath= image.absolutePath
        return image
    }

    private fun rotateImage(img:Bitmap, degree:Int):Bitmap {
        //val img = BitmapFactory.decodeFile(path)
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true)
        img.recycle()
        val file: File = createImageFile()
        val fOut = FileOutputStream(file)
        rotatedImg.compress(Bitmap.CompressFormat.JPEG,100,fOut)
        this.imagePath=file.absolutePath
        return rotatedImg
    }

    // punto 7
    private fun saveProfile() {
        if(savedImagePath ==null && imagePath != null){
            savedImagePath = imagePath
        }else if (savedImagePath != null && imagePath != savedImagePath && imagePath != null){
            File(savedImagePath).delete()
            savedImagePath=imagePath
        }

        val name = full_name.text.toString()
        val nickname = nickname.text.toString()
        val email = email.text.toString()
        val location = location.text.toString()
        var path = savedImagePath
        val user =
            User(
                name,
                "",
                nickname,
                email,
                location,
                path
            )

        Log.d ("MAD_LOG", "SEND-USER: $user")

        val intent = Intent()
        intent.putExtra(IntentRequest.UserData.NAME, user)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

}
