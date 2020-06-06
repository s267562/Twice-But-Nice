package it.polito.mad.project.fragments.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
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
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import it.polito.mad.project.R
import it.polito.mad.project.customViews.CustomMapView
import it.polito.mad.project.enums.IntentRequest
import it.polito.mad.project.models.user.User
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

    lateinit var supFragmentManager : FragmentManager

    private lateinit var mContext: Context
    private lateinit var userViewModel: UserViewModel

    private lateinit var searchEditText: EditText
    private lateinit var googleMap: GoogleMap
    private var lastPositionToSave: String = " "

    lateinit var geocode: Geocoder
    lateinit var address: Address

    private var imageFile: File? = null
    private var imagePath: String? = null
    private var savedImagePath: String? =null
    private val selectImage = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
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

        if (savedInstanceState != null) {
            imagePath = savedInstanceState.getString("ImagePath")
        }

        var image: Bitmap?
        val user = userViewModel.user.data.value as User
        if (user.name.isNotEmpty())
            full_name.setText(user.name)
        if (user.nickname.isNotEmpty())
            nickname.setText(user.nickname)
        if (user.email.isNotEmpty())
            email.setText(user.email)
        if (user.location.isNotEmpty())
            //location.setText(user.location)
        if (user.photoProfilePath.isNotEmpty()) {
            savedImagePath = user.photoProfilePath
            image = userViewModel.user.image.value
            if (image != null) {
                user_photo.setImageBitmap(image)
                rotation_button.visibility=View.VISIBLE
            } else {
                rotation_button.visibility=View.GONE
            }
        }

        if (imagePath != null){
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
            if(ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED){
                // Permission granted --> get current location
                openMap()
            } else {
                // Permission is denied
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 44)
            }
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
        if(requestCode == 44){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                openMap()
            } else {
                Toast.makeText(mContext, "Location permission has been denied", Toast.LENGTH_SHORT).show()
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
        //userViewModel.user.image.value = (user_photo.drawable as BitmapDrawable).bitmap
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
        if(savedImagePath == null && imagePath != null){
            savedImagePath = imagePath
        }else if (savedImagePath != null && imagePath != savedImagePath && imagePath != null){
            File(savedImagePath!!).delete()
            savedImagePath = imagePath
        }
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
        if (location.toString().isNullOrBlank()){
            //location.toString() = "Insert location name"
            //dataInserted = false
        }

        if (!dataInserted){
            return
        }

        if (userViewModel.error) {
            Toast.makeText(context, "Error on loading your profile, is not possible to proceed.", Toast.LENGTH_LONG).show()
            return
        }

        val name = full_name.text.toString()
        val nickname = nickname.text.toString()
        val email = email.text.toString()
        val location = location.toString()
        val path = savedImagePath

        val user = User(
            name,
            name,
            nickname,
            email,
            location,
            path
        )

        var localImage: Bitmap? = null
        if (user.photoProfilePath.isNotBlank())
            localImage = (user_photo.drawable as BitmapDrawable).bitmap

        // Save file in the Cloud DB
        userViewModel.saveUser(user)
            .addOnCompleteListener {
                if (!it.isSuccessful) {
                    Toast.makeText(mContext, "Error during user update info", Toast.LENGTH_SHORT).show()
                } else {
                    if (localImage != null)
                        userViewModel.user.image.value = localImage
                }
                findNavController().popBackStack()
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
        if (activity?.isFinishing!! && imagePath!=null && imagePath!=savedImagePath){
            //it's NOT an orientation change
            File(imagePath!!).delete()
        }
        hideKeyboard()
    }

    private fun openGallery(){
        val galleryIntent = Intent()
        galleryIntent.type = "image/*"
        galleryIntent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(galleryIntent, "Select an image from Gallery"), selectImage)
    }

    // Managing Modal Map

    private fun openMap(){

        val dialogView = LayoutInflater.from(context).inflate(R.layout.map, null)
        val mapView = dialogView.findViewById<CustomMapView>(R.id.map)

        searchEditText = dialogView.findViewById(R.id.search_loc)

        val task: Task<Location> = LocationServices.getFusedLocationProviderClient(requireActivity()).lastLocation

        task.addOnSuccessListener(object : OnSuccessListener<Location>{
            override fun onSuccess(locationSuccess: Location?) {
                if(locationSuccess != null && mapView != null){
                    mapView.onCreate(null)
                    mapView.onResume()
                    mapView.getMapAsync(object : OnMapReadyCallback{

                        override fun onMapReady(gMap: GoogleMap?) {

                            gMap?.let {
                                googleMap = it
                            }

                            gMap?.uiSettings?.isZoomControlsEnabled = true
                            gMap?.uiSettings?.isMapToolbarEnabled = true
                            gMap?.uiSettings?.isMyLocationButtonEnabled = true
                            gMap?.uiSettings?.isCompassEnabled = true

                            var position = LatLng(locationSuccess.latitude, locationSuccess.longitude)
                            gMap?.moveCamera(CameraUpdateFactory.newLatLng(position))
                            gMap?.animateCamera(CameraUpdateFactory.zoomTo(4.8F))
                            gMap?.addMarker(
                                MarkerOptions().position(position).title("Your Current Position")
                            )

                            geocode = Geocoder(context?.applicationContext, Locale.getDefault())

                            try {
                                var addr = geocode.getFromLocationName(
                                    location.text.toString(), 1)
                                if(addr.size > 0){
                                    var address : Address = addr.get(0)
                                    val cameraPos = LatLng(address.latitude, address.longitude)
                                    gMap?.addMarker(
                                        MarkerOptions()
                                            .position(LatLng(address.latitude, address.longitude))
                                            .title("User Last Location")
                                    )
                                }
                            } catch (e: IOException){
                                e.printStackTrace()
                            }

                            searchEditText.setOnEditorActionListener { _, actionId, event ->
                                if(actionId == EditorInfo.IME_ACTION_SEARCH || event?.action == KeyEvent.ACTION_DOWN ||
                                    event?.action == KeyEvent.KEYCODE_ENTER){
                                    Toast.makeText(context, "Giusto così", Toast.LENGTH_SHORT).show()
                                    geoLocate()
                                }
                                false
                            }

                            gMap?.setOnMapClickListener {
                                val clickPosition = LatLng(it.latitude, it.longitude)
                                val markerOpt = MarkerOptions()
                                markerOpt.position(clickPosition)
                                gMap.clear()
                                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 7.2F))
                                gMap.addMarker(markerOpt)
                            }

                            if(MarkerOptions().position != null){
                                lastPositionToSave = MarkerOptions().title
                            }
                        }
                    })
                }
            }

        })

        val builder= AlertDialog.Builder(context).setView(dialogView)
            .setPositiveButton("Set Location",
                DialogInterface.OnClickListener{ dialog, _ ->
                    Toast.makeText(context, "Last saved position is " + lastPositionToSave, Toast.LENGTH_SHORT).show()
                    dialog.cancel()
                })
            .setNegativeButton("Close Map",
                DialogInterface.OnClickListener { dialog, _ ->
                    dialog.cancel()
                })
        builder.show()
    }

    private fun geoLocate(){
        // This to manage the search of location from the upper bar
        val searchString = searchEditText.text.toString()
        var addressList : List<Address> = ArrayList()

        try {
            addressList = geocode.getFromLocationName(searchString, 1)
        } catch (e: IOException){
            e.printStackTrace()
        }

        if (addressList.isNotEmpty()){
            address = addressList.get(0)
            Toast.makeText(context, "We are here: $address", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hideKeyboard(){
        val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = activity?.currentFocus
        if(view != null){
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}
