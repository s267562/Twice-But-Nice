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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import it.polito.mad.project.R
import it.polito.mad.project.enums.IntentRequest
import it.polito.mad.project.enums.StoreFileKey
import it.polito.mad.project.fragments.common.StoreFileFragment
import it.polito.mad.project.models.User
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import kotlinx.android.synthetic.main.fragment_show_profile.email
import kotlinx.android.synthetic.main.fragment_show_profile.full_name
import kotlinx.android.synthetic.main.fragment_show_profile.location
import kotlinx.android.synthetic.main.fragment_show_profile.nickname
import kotlinx.android.synthetic.main.fragment_show_profile.user_photo
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

// POINT 6: Convert the EdtiProfileActivity into Fragment

class EditProfileFragment : StoreFileFragment() {

    private lateinit var profileViewModel: ProfileViewModel
    private var imageFile: File? = null
    private var imagePath: String? = null
    private var savedImagePath: String? =null
    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        profileViewModel =
            ViewModelProvider(this).get(ProfileViewModel::class.java)
        profileViewModel.user.value = loadFromStoreFile(StoreFileKey.USER, User::class.java)?:profileViewModel.user.value
        setHasOptionsMenu(true)

        (activity as AppCompatActivity?)!!.app_bar.elevation = 0f

        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            imagePath = savedInstanceState.getString("ImagePath")
        }

        var image: Bitmap?=null

        profileViewModel.user.observe(this.viewLifecycleOwner, Observer{
            if (it != null) {
                if (it.name != null && it.name.isNotEmpty())
                    full_name.text = it.name
                if (it.nickname != null && it.nickname.isNotEmpty())
                    nickname.text = it.nickname
                if (it.email != null && it.email.isNotEmpty())
                    email.text = it.email
                if (it.location != null && it.location.isNotEmpty())
                    location.text = it.location
                if (it.photoProfilePath != null && it.photoProfilePath.isNotEmpty()) {
                    if (File(it.photoProfilePath).isFile)  {
                        savedImagePath = it.photoProfilePath
                        image = BitmapFactory.decodeFile(it.photoProfilePath)
                        if (image != null) {
                            user_photo.setImageBitmap(image)
                            rotation_button.visibility=View.VISIBLE
                        }else{
                            rotation_button.visibility=View.GONE
                        }
                    }
                }
            }
        })

        if (this.imagePath != null){
            image = BitmapFactory.decodeFile(imagePath)
            this.user_photo.setImageBitmap(image)
        }

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
                this.findNavController().popBackStack()
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
                    var photoURI = FileProvider.getUriForFile(mContext,
                        "it.polito.mad.project",
                        imageFile!!
                    )
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(cameraIntent, IntentRequest.UserImage.CODE)
                }
            } else {
                //displayMessage(mContext, "Camera Intent Resolve Activity is null.")
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
            val uri: Uri = Uri.fromFile(file)
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
                var mBitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, uriPic)
                mBitmap.compress(Bitmap.CompressFormat.JPEG,100,fOut)
            }

        } else {
            Toast.makeText(mContext, "Something wrong", Toast.LENGTH_SHORT).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("DEBUG", profileViewModel.user.value.toString())
        //saveToStoreFile(StoreFileKey.USER, profileViewModel.user.value)
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

    private fun saveProfile() {
        if(savedImagePath == null && imagePath != null){
            savedImagePath = imagePath
        }else if (savedImagePath != null && imagePath != savedImagePath && imagePath != null){
            File(savedImagePath).delete()
            savedImagePath = imagePath
        }
        val name = full_name.text.toString()
        val nickname = nickname.text.toString()
        val email = email.text.toString()
        val location = location.text.toString()
        var path = savedImagePath
        saveToStoreFile(StoreFileKey.USER, User(name,"", nickname, email, location, path))

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
            File(imagePath).delete()
        }else{
            //it's an orientation change
        }
    }

}