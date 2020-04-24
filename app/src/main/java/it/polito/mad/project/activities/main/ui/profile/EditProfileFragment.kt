package it.polito.mad.project.activities.main.ui.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import it.polito.mad.project.R
import it.polito.mad.project.enums.StoreFileKey
import it.polito.mad.project.models.User
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.activity_show_profile.email
import kotlinx.android.synthetic.main.activity_show_profile.full_name
import kotlinx.android.synthetic.main.activity_show_profile.location
import kotlinx.android.synthetic.main.activity_show_profile.nickname
import kotlinx.android.synthetic.main.activity_show_profile.user_photo
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class EditProfileFragment : Fragment() {

    private lateinit var profileViewModel: ProfileViewModel
    val TAKE_PIC = 1
    val SELECT_IMAGE = 2
    private var imageFile: File? = null
    private var imagePath: String? = null
    private var savedImagePath: String? =null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        profileViewModel =
            ViewModelProvider(this).get(ProfileViewModel::class.java)
        loadUserFromStoreFile()
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.activity_edit_profile, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var image: Bitmap?=null

        profileViewModel.user.observe(this.viewLifecycleOwner, Observer{
            if (it.name != null && it.name.isNotEmpty())
                full_name.text = it.name
            if (it.nickname != null && it.nickname.isNotEmpty())
                nickname.text = it.nickname
            if (it.email != null && it.email.isNotEmpty())
                email.text = it.email
            if (it.location != null && it.location.isNotEmpty())
                location.text = it.location
            if (it.photoProfilePath != null && it.photoProfilePath.isNotEmpty()) {
                if (File(it.photoProfilePath).isFile){
                    val image: Bitmap = BitmapFactory.decodeFile(it.photoProfilePath)
                    if (image != null) user_photo.setImageBitmap(image)
                }
            }
        })
        registerForContextMenu(camera_button)

        camera_button.isLongClickable = false
        camera_button.setOnTouchListener { v, event ->
            if (v is ImageButton && event.action == MotionEvent.ACTION_DOWN) {
                v.showContextMenu(event.x, event.y)
            }
            true
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.save_profile, menu)
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        activity?.menuInflater?.inflate(R.menu.camera_menu, menu)
        menu.setHeaderTitle("Camera Menu")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.save_option -> {
                Toast.makeText(activity?.baseContext, "Save button clicked", Toast.LENGTH_SHORT).show()
                this.findNavController().navigate(R.id.action_HomeSecondFragment_to_ProfileFragment)
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
                Toast.makeText(activity?.baseContext, "Select image button clicked", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.take_pic -> {
                openCamera()
                Toast.makeText(activity?.baseContext, "Open Camera clicked", Toast.LENGTH_SHORT).show()
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

    @SuppressLint("UseRequireInsteadOfGet")
    private fun openCamera() {
        /*if (ContextCompat.checkSelfPermission(activity?.baseContext!!, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
        } else {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (cameraIntent.resolveActivity(activity?.packageManager!!) != null) {
                // Create the File where the photo should go
                imageFile = createImageFile()
                //displayMessage(baseContext, imageFile!!.absolutePath)
                Log.i("TeamSVIK", imageFile!!.absolutePath)

                // Continue only if the File was successfully created
                if (imageFile != null) {
                    imagePath = imageFile!!.absolutePath
                    var photoURI = FileProvider.getUriForFile(activity?.baseContext!!,
                        "it.polito.mad.project",
                        imageFile!!
                    )
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(cameraIntent, IntentRequest.UserImage.CODE)
                }
            } else {
                //displayMessage(baseContext, "Camera Intent Resolve Activity is null.")
            }
        }*/
        val cInt = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cInt, TAKE_PIC)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        rotation_button.visibility=View.VISIBLE

        // Open Camera
        /*if (requestCode == IntentRequest.UserImage.CODE && resultCode == Activity.RESULT_OK){
            val file = File(this.imagePath)
            val uri: Uri = Uri.fromFile(file)
            user_photo.setImageURI(uri)
        }*/
        if (requestCode === TAKE_PIC && resultCode === Activity.RESULT_OK) {
            val bp = data!!.extras!!["data"] as Bitmap?
            user_photo.setImageBitmap(bp)
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
                var mBitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, uriPic)
                mBitmap.compress(Bitmap.CompressFormat.JPEG,100,fOut)
            }

        } else {
            //displayMessage(baseContext, "Select image wrong")
            Toast.makeText(activity?.baseContext, "Something wrong", Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        if(imagePath !=null && imagePath != savedImagePath){
            File(imagePath).delete()
        }
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        // QUESTA NON FUNZIONA NON SO COME CORREGGERLA
        //val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName, /* prefix */
            ".jpg" /* suffix */
            //storageDir      /* directory */
        )
        this.imagePath= image.absolutePath
        return image
    }

    override fun onDetach() {
        super.onDetach()
        saveUserToStoreFile()
    }

    private fun loadUserFromStoreFile() {
        // Load store file of our app from shared preferences
        val sharedPreferences =
            this.activity?.getSharedPreferences(getString(R.string.app_store_file_name), Context.MODE_PRIVATE)

        // Load from the store file the user object. For the first time we load empty string.
        val userJson: String? = sharedPreferences?.getString(StoreFileKey.USER, "")

        if (userJson != null && userJson.isNotEmpty()) {
            // Assign the stored user to our view model if it is not empty
            profileViewModel.user.value = Gson().fromJson(userJson, User::class.java)
        }
    }

    private fun saveUserToStoreFile() {
        val sharedPref =
            this.activity?.getSharedPreferences(getString(R.string.app_store_file_name), Context.MODE_PRIVATE)
        val prefsEditor = sharedPref?.edit()
        prefsEditor?.putString(StoreFileKey.USER, Gson().toJson(profileViewModel));
        prefsEditor?.commit();
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
}