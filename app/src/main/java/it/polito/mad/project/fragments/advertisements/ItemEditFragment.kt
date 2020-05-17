package it.polito.mad.project.fragments.advertisements

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import it.polito.mad.project.R
import it.polito.mad.project.enums.ArgumentKey
import it.polito.mad.project.enums.IntentRequest
import it.polito.mad.project.models.Item
import kotlinx.android.synthetic.main.fragment_item_edit.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

// POINT 4: Implement the ItemEditFragment

class ItemEditFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private lateinit var itemViewModel: ItemViewModel
    private var localItem: Item = Item(-1)
    private var imageFile: File? = null
    private var imagePath: String? = null
    private var savedImagePath: String? =null
    private var dateValue: String? = null
    private var subCategoriesResArray: IntArray = intArrayOf(R.array.item_sub_art, R.array.item_sub_sports, R.array.item_sub_baby,
        R.array.item_sub_women, R.array.item_sub_men, R.array.item_sub_electo, R.array.item_sub_games, R.array.item_sub_auto)

    private val selectImage = 2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemViewModel = ViewModelProvider(activity?:this).get(ItemViewModel::class.java)
    }

    override fun onStart() {
        super.onStart()
        var selectedId = arguments?.getInt(ArgumentKey.EDIT_ITEM)
        if (selectedId != null) {
            if (selectedId < itemViewModel.items.size) {
                itemViewModel.loadItem(selectedId)
            } else if (selectedId == itemViewModel.items.size) {
                itemViewModel.item.value = Item(selectedId)
            }
        }

        itemViewModel.item.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                item_title.setText(it.title)
                if (it.categoryPos >= 0)
                    item_category_spinner.setSelection(it.categoryPos)
                item_descr.setText(it.description)
                item_location.setText(it.location)
                item_price.setText(it.price)
                item_exp.text = it.expiryDate
                if (it.imagePath != null && it.imagePath!!.isNotEmpty()) {
                    savedImagePath = it.imagePath
                    var image = BitmapFactory.decodeFile(it.imagePath)
                    if (image == null){
                        item_photo_rotate.visibility = View.GONE
                    } else {
                        item_photo_rotate.visibility = View.VISIBLE
                        item_photo.setImageBitmap(image)
                    }
                }
                localItem = it
            }
        })

        itemViewModel.loader.observe(viewLifecycleOwner, Observer {
            if (it == false) {
                loadingLayout.visibility = View.GONE
                if (itemViewModel.error) {
                    Toast.makeText(context, "Error on item loading", Toast.LENGTH_SHORT).show()
                }
            } else {
                loadingLayout.visibility = View.VISIBLE
            }
        })

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_item_edit, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerForContextMenu(item_photo_add)
        setCameraButtons()
        setDatePicker()
        setCategory()

        if (savedInstanceState != null) {
            imagePath = savedInstanceState.getString("ImagePath")
            dateValue = savedInstanceState.getString("Date")
        }

        if (this.imagePath != null){
            var image = BitmapFactory.decodeFile(imagePath)
            this.item_photo.setImageBitmap(image)
            item_photo_rotate.visibility = View.VISIBLE
        }
        if (this.dateValue != null){
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

    override fun onDestroy() {
        super.onDestroy()
        if (activity?.isFinishing!! && imagePath!=null && imagePath!=savedImagePath){
            //it's NOT an orientation change
            File(imagePath).delete()
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        var category: String = parent?.getItemAtPosition(pos) as String
        localItem.category = category
        localItem.categoryPos = pos

        if(pos >= 0 && pos < subCategoriesResArray.size)
            setSubcategory(subCategoriesResArray[pos])
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun openCamera() {
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
                    var photoURI = FileProvider.getUriForFile(activity?.baseContext!!,
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
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        item_photo_rotate.visibility=View.VISIBLE

        // Open Camera
        if (requestCode == IntentRequest.UserImage.CODE && resultCode == Activity.RESULT_OK){
            val file = File(this.imagePath)
            val uri: Uri = Uri.fromFile(file)
            item_photo.setImageURI(uri)
        }

        // Open Gallery
        else if (requestCode == selectImage && resultCode == Activity.RESULT_OK){
            val uriPic = data?.data
            item_photo.setImageURI(uriPic)
            if (uriPic != null) {
                val file: File = createImageFile()
                val fOut: FileOutputStream = FileOutputStream(file)
                imageFile = file
                imagePath = file.absolutePath
                var mBitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, uriPic)
                mBitmap.compress(Bitmap.CompressFormat.JPEG,100,fOut)
            }

        } else {
            Toast.makeText(activity?.baseContext, "Something wrong", Toast.LENGTH_SHORT).show()
        }
    }

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

        var itemImage: Bitmap?=null
        item_photo_rotate.setOnClickListener{
            item_photo.setDrawingCacheEnabled(true)
            itemImage = item_photo.getDrawingCache(true).copy(Bitmap.Config.ARGB_8888, false)
            item_photo.destroyDrawingCache()
            var rotateBitmap = rotateImage(itemImage!!, 90)
            itemImage = rotateBitmap
            item_photo.setImageBitmap(itemImage)
        }
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
        )
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

    private fun setSubcategory(textArrayResId: Int){
        context?.let {
            ArrayAdapter.createFromResource(it, textArrayResId, android.R.layout.simple_spinner_item)
                .also {
                    adapter ->
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    item_subcategory_spinner.adapter = adapter
                }
        }
    }

    private fun saveItem() {
        val subSpinner: Spinner? = activity?.findViewById(R.id.item_subcategory_spinner)
        val subcategoryContent : String = subSpinner?.selectedItem.toString()
        if(savedImagePath == null && imagePath != null){
            savedImagePath = imagePath
        } else if (savedImagePath != null && imagePath != savedImagePath && imagePath != null){
            File(savedImagePath).delete()
            savedImagePath = imagePath
        }

        localItem.title = item_title.text.toString()
        localItem.location = item_location.text.toString()
        localItem.description = item_descr.text.toString()
        localItem.expiryDate = item_exp.text.toString()
        localItem.price = item_price.text.toString()
        localItem.imagePath = savedImagePath
        localItem.category = localItem.category
        localItem.subcategory = subcategoryContent
        localItem.categoryPos = localItem.categoryPos

        if (localItem.id >= 0) {
            itemViewModel.saveItem(localItem).addOnCompleteListener {
                if (it.isSuccessful) {
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(context, "Error on item updating", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Error on saving new item", Toast.LENGTH_SHORT).show()
        }
    }

    // Methods to manage the camera
    private fun openGallery(){
        val galleryIntent = Intent()
        galleryIntent.type = "image/*"
        galleryIntent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(galleryIntent, "Select an image from Gallery"), selectImage)
    }
}
