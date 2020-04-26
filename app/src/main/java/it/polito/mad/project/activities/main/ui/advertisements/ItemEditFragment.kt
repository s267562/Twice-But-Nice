package it.polito.mad.project.activities.main.ui.advertisements

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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import it.polito.mad.project.R
import it.polito.mad.project.activities.main.ui.common.StoreFileFragment
import it.polito.mad.project.enums.ArgumentKey
import it.polito.mad.project.enums.IntentRequest
import it.polito.mad.project.enums.StoreFileKey
import it.polito.mad.project.models.Item
import kotlinx.android.synthetic.main.fragment_edit_advertisement.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ItemEditFragment : StoreFileFragment(), AdapterView.OnItemSelectedListener {

    private lateinit var item: Item
    private var imageFile: File? = null
    private var imagePath: String? = null
    private var savedImagePath: String? =null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        item = loadFromStoreFile(StoreFileKey.TEMP_ITEM, Item::class.java)?:item
        return inflater.inflate(R.layout.fragment_edit_advertisement, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerForContextMenu(camera_button_adv)
        setCameraButtons()
        setDatePicker()
        setCategorySpinner()
        // TODO: impostare le sub categories

        if (item != null) {
            item_title.setText(item.title)
            if (item.categoryPos >= 0)
                item_category_spinner.setSelection(item.categoryPos)
            item_descr.setText(item.description)
            item_location.setText(item.location)
            item_price.setText(item.price.toString())
            item_exp.setText(item.expiryDate)
            if (item.imagePath != null && item.imagePath!!.isNotEmpty()) {
                savedImagePath = item.imagePath
                val image: Bitmap = BitmapFactory.decodeFile(item.imagePath)
                if (image != null) item_photo.setImageBitmap(image)
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

    override fun onOptionsItemSelected(option: MenuItem): Boolean {
        // Handle item selection
        return when (option.itemId) {
            R.id.save_option -> {
                saveItem()
                this.findNavController().popBackStack()
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

    override fun onDestroyView() {
        super.onDestroyView()
        saveToStoreFile(StoreFileKey.TEMP_ITEM, item)
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        var category: String = parent?.getItemAtPosition(pos) as String
        item.category = category
        item.categoryPos = pos
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")

    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(activity?.baseContext!!, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity!!, arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
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
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        rotation_button_adv.visibility=View.VISIBLE

        // Open Camera
        if (requestCode == IntentRequest.UserImage.CODE && resultCode == Activity.RESULT_OK){
            val file = File(this.imagePath)
            val uri: Uri = Uri.fromFile(file)
            item_photo.setImageURI(uri)
        }

        // Open Gallery
        else if (requestCode == SELECT_IMAGE && resultCode == Activity.RESULT_OK){
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
        camera_button_adv.isLongClickable = false
        camera_button_adv.setOnTouchListener { v, event ->
            if (v is ImageButton && event.action == MotionEvent.ACTION_DOWN) {
                v.showContextMenu(event.x, event.y)
            }
            true
        }

        var itemImage: Bitmap?=null
        rotation_button_adv.setOnClickListener{
            item_photo.setDrawingCacheEnabled(true)
            itemImage = item_photo.getDrawingCache(true).copy(Bitmap.Config.ARGB_8888, false)
            item_photo.destroyDrawingCache()
            var rotateBitmap = rotateImage(itemImage!!, 90)
            itemImage = rotateBitmap
            item_photo.setImageBitmap(itemImage)
        }

        if (itemImage == null){
            rotation_button_adv.visibility = View.GONE
        } else {
            rotation_button_adv.visibility = View.VISIBLE
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
            //storageDir      /* directory */
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
            // date picker dialog
            var picker = context?.let {
                DatePickerDialog(
                    it,
                    DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth -> item_exp.setText(dayOfMonth.toString() + "/" + (monthOfYear + 1) + "/" + year) }, year, month, day
                )
            }
            picker?.show()
        }
    }

    private fun setCategorySpinner() {
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

    private fun saveItem() {
        var items: MutableList<Item> = loadFromStoreFile(StoreFileKey.ITEMS, Array<Item>::class.java)?.toMutableList()?: mutableListOf()

        if(savedImagePath == null && imagePath != null){
            savedImagePath = imagePath
        }else if (savedImagePath != null && imagePath != savedImagePath && imagePath != null){
            File(savedImagePath).delete()
            savedImagePath = imagePath
        }
        item.title = item_title.text.toString()
        item.location = item_location.text.toString()
        item.description = item_descr.text.toString()
        item.expiryDate = item_exp.text.toString()
        item.price = item_price.text.toString().toDouble()
        item.imagePath = savedImagePath
        item.category = item.category
        item.categoryPos = item.categoryPos
        if (items.size > item.id)  items[item.id] = item else items.add(item)
        saveToStoreFile(StoreFileKey.ITEM, item)
        saveToStoreFile(StoreFileKey.ITEMS, items.toTypedArray())
    }
}
