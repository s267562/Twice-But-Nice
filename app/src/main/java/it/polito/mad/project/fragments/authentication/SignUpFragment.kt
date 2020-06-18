package it.polito.mad.project.fragments.authentication

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import it.polito.mad.project.R
import it.polito.mad.project.enums.IntentRequest
import it.polito.mad.project.models.user.User
import it.polito.mad.project.utils.Util.Companion.hideKeyboard
import it.polito.mad.project.viewmodels.AuthViewModel
import kotlinx.android.synthetic.main.fragment_signup.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class SignUpFragment: Fragment() {
    private lateinit var authViewModel: AuthViewModel

    private var registerEnable: BooleanArray = booleanArrayOf(false, false, false, false, false)
    private val FULL_NAME = 0
    private val NICKNAME = 1
    private val EMAIL = 2
    private val PASSWORD = 3

    private var imagePath: String? = null
    private var savedImagePath: String? =null
    private var imageFile: File? = null
    private val selectImage = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authViewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
    }

    override fun onStart() {
        super.onStart()
        authViewModel.registeredIn.observe(viewLifecycleOwner, Observer {
            if (authViewModel.error) {
                Toast.makeText(context, authViewModel.errorMessage, Toast.LENGTH_LONG).show()
                authViewModel.error = false
            }
            else {
                if (it) {
                    findNavController().popBackStack()
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var image: Bitmap?
        if (savedInstanceState != null) {
            imagePath = savedInstanceState.getString("ImagePath")
        }

        if (imagePath != null){
            image = BitmapFactory.decodeFile(imagePath)
            signup_photo.setImageBitmap(image)
        }
        au_fullname.doOnTextChanged { text, _, _, _ ->
            if(!text.isNullOrBlank()) {
                /* full name not blank */
                if(!registerEnable[FULL_NAME]) registerEnable[FULL_NAME] = true
                if(registerEnable[NICKNAME] &&
                    registerEnable[EMAIL] &&
                    registerEnable[PASSWORD] &&
                    !regBtn.isEnabled) {
                    /* other field not blank and button not enabled */
                    enableButton(regBtn)
                }
            } else {
                if(registerEnable[FULL_NAME]) {
                    registerEnable[FULL_NAME] = false
                    disableButton(regBtn)
                }
            }
        }

        au_nickname.doOnTextChanged { text, _, _, _ ->
            if(!text.isNullOrBlank()) {
                /* nickname not blank */
                if(!registerEnable[NICKNAME]) registerEnable[NICKNAME] = true
                if(registerEnable[FULL_NAME] &&
                    registerEnable[EMAIL] &&
                    registerEnable[PASSWORD] &&
                    !regBtn.isEnabled) {
                    /* other field not blank and button not enabled */
                    enableButton(regBtn)
                }
            } else {
                if(registerEnable[NICKNAME]) {
                    registerEnable[NICKNAME] = false
                    disableButton(regBtn)
                }
            }
        }

        au_email.doOnTextChanged { text,  _, _, _ ->
            if(!text.isNullOrBlank()) {
                /* email not blank */
                if(!registerEnable[EMAIL]) registerEnable[EMAIL] = true
                if(registerEnable[NICKNAME] &&
                    registerEnable[FULL_NAME] &&
                    registerEnable[PASSWORD] &&
                    !regBtn.isEnabled) {
                    /* other field not blank and button not enabled */
                    enableButton(regBtn)
                }
            } else {
                if(registerEnable[EMAIL]) {
                    registerEnable[EMAIL] = false
                    disableButton(regBtn)
                }
            }
        }

        au_password.doOnTextChanged { text, _, _, _ ->
            if(!text.isNullOrBlank()) {
                /* full name not blank */
                if(!registerEnable[PASSWORD]) registerEnable[PASSWORD] = true
                if(registerEnable[NICKNAME] &&
                    registerEnable[EMAIL] &&
                    registerEnable[FULL_NAME] &&
                    !regBtn.isEnabled) {
                    /* other field not blank and button not enabled */
                    enableButton(regBtn)
                }
            } else {
                if(registerEnable[PASSWORD]) {
                    registerEnable[PASSWORD] = false
                    disableButton(regBtn)
                }
            }
        }

        registerForContextMenu(camera_signup_button)

        camera_signup_button.isLongClickable = false
        camera_signup_button.setOnTouchListener { v, event ->
            if (v is ImageButton && event.action == MotionEvent.ACTION_DOWN) {
                v.showContextMenu(event.x, event.y)
            }
            true
        }

        rotation_signup_button.setOnClickListener {
            signup_photo.isDrawingCacheEnabled = true
            image = signup_photo.getDrawingCache(true).copy(Bitmap.Config.ARGB_8888, false)
            signup_photo.destroyDrawingCache()
            val rotateBitmap = rotateImage(image!!)
            image = rotateBitmap
            signup_photo.setImageBitmap(image)
            // userViewModel.user.image.value = image
        }

        regBtn.setOnClickListener {
            if(savedImagePath == null && imagePath != null){
                savedImagePath = imagePath
            }else if (savedImagePath != null && imagePath != savedImagePath && imagePath != null){
                File(savedImagePath!!).delete()
                savedImagePath = imagePath
            }

            var dataInserted = true

            if (au_nickname.text.isNullOrBlank()){
                au_nickname.error = "Insert Nickname"
                dataInserted = false
            }
            if (au_fullname.text.isNullOrBlank()){
                au_fullname.error = "Insert full name"
                dataInserted = false

            }
            if (au_email.text.isNullOrBlank()){
                au_email.error = "Insert email address"
                dataInserted = false

            }
            if (au_password.text.isNullOrBlank()){
                au_password.error = "Insert password"
                dataInserted = false
            }

            if (dataInserted){
                val user = User(au_fullname.text.toString())
                user.email = au_email.text.toString()
                user.nickname = au_nickname.text.toString()
                user.password = au_password.text.toString()
                user.photoProfilePath = savedImagePath.toString()
                authViewModel.registerUser(user)
            }

        }

        regToLog.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun enableButton(button: Button) {
        button.isEnabled = true
        button.background.setTint(ContextCompat.getColor(requireContext(), R.color.colorAccent))
    }

    private fun disableButton(button: Button) {
        button.isEnabled = false
        button.background.setTint(ContextCompat.getColor(requireContext(), R.color.colorAccentLight))
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        activity?.menuInflater?.inflate(R.menu.camera_menu, menu)
        menu.setHeaderTitle("Camera Menu")
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.select_image -> {
                openGallery()
                Toast.makeText(context, "Select image button clicked", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.take_pic -> {
                openCamera()
                Toast.makeText(context, "Open Camera clicked", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (this.imagePath != null) {
            outState.putString("ImagePath", this.imagePath)
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(context!!, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE),0)
        } else {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (cameraIntent.resolveActivity(activity?.packageManager!!) != null) {
                // Create the File where the photo should go
                imageFile = createImageFile()
                Log.i("TeamSVIK", imageFile!!.absolutePath)

                // Continue only if the File was successfully created
                if (imageFile != null) {
                    imagePath = imageFile!!.absolutePath
                    val photoURI = FileProvider.getUriForFile(context!!,
                        "it.polito.mad.project",
                        imageFile!!
                    )
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(cameraIntent, IntentRequest.UserImage.CODE)
                }
            } else {
                Toast.makeText(context!!, "Camera intent, resolve activity is null", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openGallery(){
        val galleryIntent = Intent()
        galleryIntent.type = "image/*"
        galleryIntent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(galleryIntent, "Select an image from Gallery"), selectImage)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 0){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                openCamera()
            } else {
                Toast.makeText(context, "Camera permission has been denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        rotation_signup_button.visibility=View.VISIBLE
        if (requestCode == IntentRequest.UserImage.CODE && resultCode == Activity.RESULT_OK){
            // Open Camera
            val file = File(imagePath!!)
            val uri: Uri = Uri.fromFile(file)
            signup_photo.setImageURI(uri)
        } else if (requestCode == selectImage && resultCode == Activity.RESULT_OK){
            // Open Gallery
            val uriPic = data?.data
            signup_photo.setImageURI(uriPic)
            if (uriPic != null) {
                val file: File = createImageFile()
                val fOut = FileOutputStream(file)
                imageFile = file
                imagePath = file.absolutePath
                val mBitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, uriPic)
                mBitmap.compress(Bitmap.CompressFormat.JPEG,100,fOut)
            }
        } else {
            Toast.makeText(context, "Something wrong", Toast.LENGTH_SHORT).show()
        }
        //userViewModel.user.image.value = (user_photo.drawable as BitmapDrawable).bitmap
    }

    private fun rotateImage(img:Bitmap, degree: Int = 90) : Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        img.recycle()
        val file: File = createImageFile()
        val fOut = FileOutputStream(file)
        rotatedImg.compress(Bitmap.CompressFormat.JPEG,100,fOut)
        this.imagePath = file.absolutePath
        return rotatedImg
    }

    private fun createImageFile(): File {
        if(imagePath !=null && imagePath != savedImagePath){
            File(imagePath!!).delete()
        }
        // Create an image file name
        val imageFileName = "JPEG_" + SimpleDateFormat("yyyyMMdd_HHmmss").format(Date()) + "_"
        val image = File.createTempFile(imageFileName, ".jpg")
        this.imagePath = image.absolutePath
        return image
    }

    override fun onDestroy() {
        super.onDestroy()
        hideKeyboard(activity)
    }
}