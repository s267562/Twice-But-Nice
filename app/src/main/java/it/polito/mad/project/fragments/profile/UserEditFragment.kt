package it.polito.mad.project.fragments.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
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
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.Observer
import it.polito.mad.project.R
import it.polito.mad.project.enums.IntentRequest
import it.polito.mad.project.models.user.User
import it.polito.mad.project.utils.Util.Companion.hideKeyboard
import it.polito.mad.project.viewmodels.MapViewModel
import it.polito.mad.project.viewmodels.UserViewModel
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.android.synthetic.main.fragment_edit_profile.loadingLayout
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

// CHIAVE Google Maps AIzaSyCO1c1zV2KftTDLB1Jb3gBwRiMXFRfdAdk

class UserEditFragment : Fragment() {

    private lateinit var supFragmentManager : FragmentManager
    private lateinit var userViewModel: UserViewModel
    private lateinit var mapViewModel: MapViewModel
    private lateinit var mContext: Context

    private var imageFile: File? = null
    private var imagePath: String? = null
    private var savedImagePath: String? =null

    private val selectImage = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        mapViewModel = ViewModelProvider(requireActivity()).get(MapViewModel::class.java)
        mapViewModel.location = null
    }

    override fun onStart() {
        super.onStart()
        userViewModel.loader.observe(viewLifecycleOwner, Observer {
            if (userViewModel.isNotLoading()) {
                loadingLayout.visibility = View.GONE
                if (userViewModel.error) {
                    Toast.makeText(context, "Error on item loading", Toast.LENGTH_SHORT).show()
                }
            } else {
                loadingLayout.visibility = View.VISIBLE
            }
        })
        mapViewModel.updateLocation.observe(viewLifecycleOwner, Observer {
            if(it) {
                userViewModel.user.localData?.location = mapViewModel.location?:location.text.toString()
                location.text = mapViewModel.location?:location.text
                mapViewModel.updateLocation.value = false
                mapViewModel.location = null
            }
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        supFragmentManager = (context as AppCompatActivity).supportFragmentManager
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    @Suppress("DEPRECATION")
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = userViewModel.user.localData?:userViewModel.user.data.value as User
        if (user.name.isNotEmpty())
            full_name.setText(user.name)
        if (user.nickname.isNotEmpty())
            nickname.setText(user.nickname)
        if (user.email.isNotEmpty())
            email.setText(user.email)
        if (user.location.isNotEmpty())
            location.text = mapViewModel.location?:user.location

        var image: Bitmap?
        if (user.photoProfilePath.isNotEmpty()) {
            savedImagePath = user.photoProfilePath
            image = userViewModel.user.localImage?:userViewModel.user.image.value
            if (image != null) {
                user_photo.setImageBitmap(image)
                rotation_button.visibility=View.VISIBLE
            } else {
                rotation_button.visibility=View.GONE
            }
        }

        if (savedInstanceState != null) {
            imagePath = savedInstanceState.getString("ImagePath")
        }

        if (imagePath != null){
            image = BitmapFactory.decodeFile(imagePath)
            this.user_photo.setImageBitmap(image)
            userViewModel.user.localImage = image
        }

        registerForContextMenu(camera_button)

        camera_button.isLongClickable = false
        camera_button.setOnTouchListener { v, event ->
            if (v is ImageButton && event.action == MotionEvent.ACTION_DOWN) {
                v.showContextMenu(event.x, event.y)
            }
            true
        }

        rotation_button.setOnClickListener {
            user_photo.isDrawingCacheEnabled = true
            image = user_photo.getDrawingCache(true).copy(Bitmap.Config.ARGB_8888, false)
            user_photo.destroyDrawingCache()
            val rotateBitmap = rotateImage(image!!)
            image = rotateBitmap
            user_photo.setImageBitmap(image)
            //userViewModel.user.image.value = image
        }

        location.setOnClickListener {
            updateLocalData()
            mapViewModel.location = mapViewModel.location?:location.text.toString()
            this.findNavController().navigate(R.id.action_editProfileFragment_to_mapFragment)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.save_menu, menu)
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
                Toast.makeText(mContext, "Select image button clicked", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.take_pic -> {
                openCamera()
                Toast.makeText(mContext, "Open Camera clicked", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
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
                    val photoURI = FileProvider.getUriForFile(mContext,
                        "it.polito.mad.project",
                        imageFile!!
                    )
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(cameraIntent, IntentRequest.UserImage.CODE)
                }
            } else {
                Toast.makeText(mContext, "Camera intent, resolve activity is null", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 0){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                openCamera()
            } else {
                Toast.makeText(mContext, "Camera permission has been denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        rotation_button.visibility=View.VISIBLE

        if (requestCode == IntentRequest.UserImage.CODE && resultCode == Activity.RESULT_OK){
            // Open Camera
            val file = File(imagePath!!)
            val uri: Uri = Uri.fromFile(file)
            user_photo.setImageURI(uri)
        } else if (requestCode == selectImage && resultCode == Activity.RESULT_OK){
            // Open Gallery
            val uriPic = data?.data
            user_photo.setImageURI(uriPic)
            if (uriPic != null) {
                val file: File = createImageFile()
                val fOut = FileOutputStream(file)
                imageFile = file
                imagePath = file.absolutePath
                val mBitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, uriPic)
                mBitmap.compress(Bitmap.CompressFormat.JPEG,100,fOut)
            }
        } else {
            Toast.makeText(mContext, "Something wrong", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
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

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("DEBUG", userViewModel.user.data.value.toString())
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

    private fun saveProfile() {
        if (!isFormCompleted()){
            Toast.makeText(context, "Some information are missing. Complete all required fields.", Toast.LENGTH_LONG).show()
            return
        }

        if (userViewModel.error) {
            Toast.makeText(context, "Error on loading your profile, is not possible to proceed.", Toast.LENGTH_LONG).show()
            return
        }

        if(savedImagePath == null && imagePath != null){
            savedImagePath = imagePath
        }else if (savedImagePath != null && imagePath != savedImagePath && imagePath != null){
            File(savedImagePath!!).delete()
            savedImagePath = imagePath
        }

        updateLocalData()
        if (userViewModel.user.localData != null) {
            val user = userViewModel.user.localData!!
            // Save file in the Cloud DB
            userViewModel.saveUser(user)
                .addOnCompleteListener {
                    if (!it.isSuccessful) {
                        Toast.makeText(mContext, "Error during user update info", Toast.LENGTH_SHORT).show()
                    } else {
                        if (userViewModel.user.localImage != null) {
                            userViewModel.user.image.value = userViewModel.user.localImage
                            userViewModel.user.localImage = null
                        }
                    }
                    findNavController().popBackStack()
                }
        }
    }

    private fun updateLocalData() {
        val updateUser =User(full_name.text.toString())
        updateUser.nickname = nickname.text.toString()
        updateUser.email = email.text.toString()
        updateUser.location = location.text.toString()
        updateUser.photoProfilePath = savedImagePath?:""
        userViewModel.user.localData = updateUser
    }

    private fun isFormCompleted(): Boolean {
        var dataInserted = true

        if (nickname.text.isNullOrBlank()){
            nickname.error = "Insert Nickname"
            dataInserted = false
        }
        if (full_name.text.isNullOrBlank()){
            full_name.error = "Insert full name"
            dataInserted = false
        }
        if (email.text.isNullOrBlank()){
            email.error = "Insert email address"
            dataInserted = false
        }
        if (location.text.isNullOrBlank()){
            location.error = "Insert location name"
            dataInserted = false
        }
        return dataInserted
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (this.imagePath != null) {
            outState.putString("ImagePath", this.imagePath)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (activity?.isFinishing!! && imagePath!=null && imagePath!=savedImagePath){
            //it's NOT an orientation change
            File(imagePath!!).delete()
        }
        hideKeyboard(activity)
    }

    private fun openGallery(){
        val galleryIntent = Intent()
        galleryIntent.type = "image/*"
        galleryIntent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(galleryIntent, "Select an image from Gallery"), selectImage)
    }
}
