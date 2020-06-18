package it.polito.mad.project.fragments.advertisements

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import it.polito.mad.project.R
import it.polito.mad.project.commons.fragments.NotificationFragment
import it.polito.mad.project.enums.IntentRequest
import it.polito.mad.project.enums.items.ItemStatus
import it.polito.mad.project.fragments.advertisements.dialogs.SetBuyerDialogFragment
import it.polito.mad.project.models.item.Item
import it.polito.mad.project.utils.Util.Companion.hideKeyboard
import it.polito.mad.project.viewmodels.ItemViewModel
import it.polito.mad.project.viewmodels.MapViewModel
import it.polito.mad.project.viewmodels.UserViewModel
import kotlinx.android.synthetic.main.fragment_item_edit.*
import kotlinx.android.synthetic.main.fragment_item_edit.item_descr
import kotlinx.android.synthetic.main.fragment_item_edit.item_exp
import kotlinx.android.synthetic.main.fragment_item_edit.item_location
import kotlinx.android.synthetic.main.fragment_item_edit.item_photo
import kotlinx.android.synthetic.main.fragment_item_edit.item_price
import kotlinx.android.synthetic.main.fragment_item_edit.item_title
import kotlinx.android.synthetic.main.fragment_item_edit.loadingLayout
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ItemEditFragment : NotificationFragment(), AdapterView.OnItemSelectedListener {

    private lateinit var itemViewModel: ItemViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var mapViewModel: MapViewModel

    private lateinit var supFragmentManager : FragmentManager

    private var imageFile: File? = null
    private var imagePath: String? = null
    private var savedImagePath: String? =null
    private var dateValue: String? = null

    private var subCategoriesResArray: IntArray = intArrayOf(R.array.item_sub_art, R.array.item_sub_sports, R.array.item_sub_baby,
        R.array.item_sub_women, R.array.item_sub_men, R.array.item_sub_electo, R.array.item_sub_games, R.array.item_sub_auto)

    private val selectImage = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemViewModel = ViewModelProvider(requireActivity()).get(ItemViewModel::class.java)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        mapViewModel = ViewModelProvider(requireActivity()).get(MapViewModel::class.java)

    }

    override fun onStart() {
        super.onStart()

        val itemId = arguments?.getString("ItemId")

        if (itemId != null) {
            // Item already exist
            itemViewModel.loadItem(itemId)
        } else {
            // New Item
            itemViewModel.item.data.value =
                Item(itemId, userViewModel.user.data.value!!.nickname)
            itemViewModel.item.image.value = null
        }


        itemViewModel.item.data.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (itemViewModel.item.localData == null)
                    itemViewModel.item.localData = it
                val localIt = itemViewModel.item.localData!!
                item_title.setText(localIt.title)
                if (localIt.categoryPos >= 0)
                    item_category_spinner.setSelection(localIt.categoryPos)
                item_descr.setText(localIt.description)
                item_location.text = localIt.location
                item_price.setText(localIt.price)
                item_exp.text = localIt.expiryDate
                if(localIt.statusPos >= 0){
                    item_status_spinner.setSelection(localIt.statusPos)
                }
                if (localIt.imagePath.isNotEmpty()) {
                    savedImagePath = it.imagePath
                }
            }
        })

        itemViewModel.item.image.observe(viewLifecycleOwner, Observer {
            if (it == null && itemViewModel.item.localImage == null){
                item_photo_rotate.visibility = View.GONE
            } else {
                item_photo_rotate.visibility = View.VISIBLE
                item_photo.setImageBitmap(itemViewModel.item.localImage?:it)
            }
        })

        itemViewModel.loader.observe(viewLifecycleOwner, Observer {
            if (itemViewModel.isNotLoading()) {
                loadingLayout.visibility = View.GONE
                if (itemViewModel.error) {
                    Toast.makeText(context, "Error on item loading", Toast.LENGTH_SHORT).show()
                }
            } else {
                loadingLayout.visibility = View.VISIBLE
            }
        })

        mapViewModel.updateLocation.observe(viewLifecycleOwner, Observer {
            if(it) {
                itemViewModel.item.localData?.location = mapViewModel.location?:item_location.text.toString()
                item_location.text = itemViewModel.item.localData?.location
                mapViewModel.updateLocation.value = false
                mapViewModel.location = null
            }
        })

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        supFragmentManager = (context as AppCompatActivity).supportFragmentManager
        return inflater.inflate(R.layout.fragment_item_edit, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerForContextMenu(item_photo_add)
        setCameraButtons()
        setDatePicker()
        setCategory()
        setStatusSpinner()

        item_location.setOnClickListener {
            updateLocalData()
            mapViewModel.location = mapViewModel.location?:item_location.text.toString()
            this.findNavController().navigate(R.id.action_itemEditFragment_to_mapFragment)
        }

        if (savedInstanceState != null) {
            imagePath = savedInstanceState.getString("ImagePath")
            dateValue = savedInstanceState.getString("Date")
        }

        if (imagePath != null){
            itemViewModel.item.localImage = BitmapFactory.decodeFile(imagePath)
            item_photo.setImageBitmap(itemViewModel.item.localImage)
            item_photo_rotate.visibility = View.VISIBLE
        }
        if (dateValue != null) {
            item_exp.text= this.dateValue
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

    override fun onOptionsItemSelected(option: MenuItem): Boolean {
        // Handle item selection
        return when (option.itemId) {
            R.id.save_option -> {
                saveItem()
                true
            }
            else -> super.onOptionsItemSelected(option)
        }
    }

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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (this.imagePath != null) {
            outState.putString("ImagePath", this.imagePath)
        }
        if (this.dateValue != null){
            outState.putString("Date", this.dateValue)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        updateLocalData()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (activity?.isFinishing!! && imagePath != null && imagePath != savedImagePath){
            //it's NOT an orientation change
            File(imagePath!!).delete()
        }
        hideKeyboard(activity)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        // Nothing to do
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        val category: String = parent?.getItemAtPosition(pos) as String
        itemViewModel.item.localData?.category = category
        itemViewModel.item.localData?.categoryPos = pos

        if(pos >= 0 && pos < subCategoriesResArray.size)
            setSubcategory(subCategoriesResArray[pos], itemViewModel.item.localData?.subcategoryPos?:0)
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun openCamera() {
        updateLocalData()
        if (ContextCompat.checkSelfPermission(activity?.baseContext!!, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE),0)
        } else {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (cameraIntent.resolveActivity(activity?.packageManager!!) != null) {
                // Create the File where the photo should go
                imageFile = createImageFile()

                // Continue only if the File was successfully created
                if (imageFile != null) {
                    imagePath = imageFile!!.absolutePath
                    val photoURI = FileProvider.getUriForFile(activity?.baseContext!!,
                        "it.polito.mad.project",
                        imageFile!!
                    )
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(cameraIntent, IntentRequest.UserImage.CODE)
                }
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
                Toast.makeText(context, "Camera permission has been denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        item_photo_rotate.visibility=View.VISIBLE

        if (requestCode == IntentRequest.UserImage.CODE && resultCode == Activity.RESULT_OK){
            // Open Camera
            val file = File(imagePath!!)
            val uri: Uri = Uri.fromFile(file)
            item_photo.setImageURI(uri)
            itemViewModel.item.localImage = (item_photo.drawable as BitmapDrawable).bitmap
        } else if (requestCode == selectImage && resultCode == Activity.RESULT_OK) {
            // Open Gallery
            val uriPic = data?.data
            item_photo.setImageURI(uriPic)
            itemViewModel.item.localImage = (item_photo.drawable as BitmapDrawable).bitmap
            if (uriPic != null) {
                val file: File = createImageFile()
                val fOut = FileOutputStream(file)
                imageFile = file
                imagePath = file.absolutePath
                val mBitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, uriPic)
                mBitmap.compress(Bitmap.CompressFormat.JPEG,100,fOut)
            }
        } else {
            Toast.makeText(activity?.baseContext, "Something wrong", Toast.LENGTH_SHORT).show()
        }
    }

    @Suppress("DEPRECATION")
    @RequiresApi(Build.VERSION_CODES.N)
    private fun setCameraButtons() {
        item_photo_add.isLongClickable = false
        item_photo_add.setOnTouchListener { v, event ->
            Log.d("DEBUG", "TOUCH")
            if (v is Button && event.action == MotionEvent.ACTION_DOWN) {
                v.showContextMenu(event.x, event.y)
            }
            true
        }

        item_photo_rotate.setOnClickListener {
            item_photo.isDrawingCacheEnabled = true
            var itemImage = item_photo.getDrawingCache(true).copy(Bitmap.Config.ARGB_8888, false)
            item_photo.destroyDrawingCache()
            val rotateBitmap = rotateImage(itemImage!!)
            itemImage = rotateBitmap
            item_photo.setImageBitmap(itemImage)
            itemViewModel.item.localImage = itemImage
        }
    }

    private fun rotateImage(img:Bitmap, degree:Int = 90):Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        img.recycle()
        val file: File = createImageFile()
        val fOut = FileOutputStream(file)
        rotatedImg.compress(Bitmap.CompressFormat.JPEG,100,fOut)
        imagePath = file.absolutePath
        return rotatedImg
    }

    @SuppressLint("SimpleDateFormat")
    private fun createImageFile(): File {
        if (imagePath != null && imagePath != savedImagePath){
            File(imagePath!!).delete()
        }
        // Create an image file name
        val imageFileName = "JPEG_" + SimpleDateFormat("yyyyMMdd_HHmmss").format(Date()) + "_"
        val image = File.createTempFile(imageFileName,".jpg")
        this.imagePath= image.absolutePath
        return image
    }

    private fun setDatePicker() {
        item_exp.inputType = InputType.TYPE_NULL
        item_exp.setOnClickListener {
            val cldr = Calendar.getInstance()
            val day = cldr[Calendar.DAY_OF_MONTH]
            val month = cldr[Calendar.MONTH]
            val year = cldr[Calendar.YEAR]
            context?.let {
                DatePickerDialog(
                    it,
                    DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                        item_exp.text = (
                                dayOfMonth.toString() + "/" + (monthOfYear + 1) + "/" + year
                                )
                        this.dateValue = item_exp.text.toString()
                    },
                    year, month, day
                ).show()
            }
        }
    }

    private fun setStatusSpinner(){
        context?.let {
            val arrayId = if (itemViewModel.interestedUsers.users.size > 0) R.array.item_status else R.array.item_status_partial
            ArrayAdapter.createFromResource(it, arrayId, android.R.layout.simple_spinner_item)
                .also { adapter ->
                    // Specify the layout to use when the list of choices appears
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    // Apply the adapter to the spinner
                    item_status_spinner.adapter = adapter
                }
            item_status_spinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Nothing to do
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val status: String = parent?.getItemAtPosition(position) as String
                    itemViewModel.item.localData?.status = status
                    itemViewModel.item.localData?.statusPos = position
                    if (status == ItemStatus.Sold.toString())
                        SetBuyerDialogFragment().show(supFragmentManager, "Set Buyer")
                }
            }
        }
    }

    private fun setCategory() {
        context?.let {
            ArrayAdapter.createFromResource(it, R.array.item_categories, android.R.layout.simple_spinner_item)
                .also { adapter ->
                    // Specify the layout to use when the list of choices appears
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    // Apply the adapter to the spinner
                    item_category_spinner.adapter = adapter
                }
        }
        item_category_spinner.onItemSelectedListener = this
    }

    private fun setSubcategory(textArrayResId: Int, subcategoryPos: Int = 0){
        context?.let {
            ArrayAdapter.createFromResource(it, textArrayResId, android.R.layout.simple_spinner_item)
                .also {
                        adapter ->
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    item_subcategory_spinner.adapter = adapter
                    item_subcategory_spinner.setSelection(subcategoryPos)
                }
            item_subcategory_spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Nothing to do
                }
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val subcategory: String = parent?.getItemAtPosition(position) as String
                    itemViewModel.item.localData?.subcategory = subcategory
                    itemViewModel.item.localData?.subcategoryPos = position
                }
            }
        }
    }

    private fun saveItem() {
        if (!isFormCompleted()) {
            Toast.makeText(context, "Some information are missing. Complete all required fields.", Toast.LENGTH_LONG).show()
            return
        }
        if (itemViewModel.error) {
            Toast.makeText(context, "Error on item loading, is not possible to save your item.", Toast.LENGTH_LONG).show()
            return
        }

        if(savedImagePath == null && imagePath != null){
            savedImagePath = imagePath
        } else if (savedImagePath != null && imagePath != savedImagePath && imagePath != null){
            File(savedImagePath!!).delete()
            savedImagePath = imagePath
        }
        updateLocalData()

        if (itemViewModel.item.localData != null) {
            val updatedItem = itemViewModel.item.localData!!
            itemViewModel.saveItem(updatedItem)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        sendItemNotification(updatedItem)
                        if (itemViewModel.item.localImage != null)
                            itemViewModel.item.image.value = itemViewModel.item.localImage

                        itemViewModel.item.localImage = null
                        findNavController().popBackStack()
                    } else {
                        Toast.makeText(context, "Error on item updating", Toast.LENGTH_SHORT).show()
                    }
                }

        }
    }

    private fun sendItemNotification(item: Item) {
        if (item.id != null) {
            when (item.status) {
                ItemStatus.Sold.toString() -> {
                    val body = JSONObject().put("ItemId", item.id)
                        .put("IsMyItem", false)
                        .put("BuyerId", item.buyerId)
                    sendNotification(item.id!!, item.title, "The item was sold", body)
                }
                ItemStatus.Blocked.toString() -> {
                    val body = JSONObject().put("ItemId", item.id)
                        .put("IsMyItem", false)
                    sendNotification(item.id!!, item.title, "The item is blocked", body)
                }
            }
        }
    }

    private fun updateLocalData() {
        val updateItem = itemViewModel.item.localData
        if (updateItem != null) {
            updateItem.title = item_title.text.toString()
            updateItem.location = item_location.text.toString()
            updateItem.description = item_descr.text.toString()
            updateItem.expiryDate = item_exp.text.toString()
            updateItem.price = item_price.text.toString()
            updateItem.imagePath = savedImagePath?:""
        }
    }

    private fun isFormCompleted(): Boolean {
        var dataInserted = true

        if (item_title.text.isNullOrBlank()){
            item_title.error = "Insert Title"
            dataInserted = false
        }
        if (item_price.text.isNullOrBlank()){
            item_price.error = "Insert Price"
            dataInserted = false
        }
        if(item_descr.text.isNullOrBlank()){
            item_descr.error = "Insert Description"
            dataInserted = false
        }
        if(item_exp.text.isNullOrBlank()  ||   Date()>SimpleDateFormat("dd/MM/yyyy").parse(item_exp.text.toString())){
            item_exp.error = "Insert Expiring Date"
            dataInserted = false
        }
        if(item_location.text.isNullOrBlank()){
            item_location.error = "Pick a Location in Map"
            dataInserted = false
        }
        return  dataInserted
    }

    private fun openGallery(){
        updateLocalData()
        val galleryIntent = Intent()
        galleryIntent.type = "image/*"
        galleryIntent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(galleryIntent, "Select an image from Gallery"), selectImage)
    }
}
