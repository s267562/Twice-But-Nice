package it.polito.mad.mad_project

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_edit_profile.*
import java.io.File
import java.io.IOException
import java.net.Authenticator
import java.util.*

class EditProfileActivity : AppCompatActivity() {

    val REQUEST_IMAGE_CAPTURE = 1
    val REQUEST_TAKE_PHOTO = 1
    lateinit var currentPhotoPath: String

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

        if (user != null) {
            full_name.setText(user.name)
            nickname.setText(user.nickname)
            email.setText(user.email)
            location.setText(user.location)
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

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.select_image -> {
                Toast.makeText(this, "select image...", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.take_pic -> {
                Toast.makeText(this, "take picture...", Toast.LENGTH_SHORT).show()
                dispatchTakePictureIntent()
                true
            }
            else -> super.onContextItemSelected(item)
        }
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

    private fun saveProfile() {
        val name = full_name.text.toString()
        val nickname = nickname.text.toString()
        val email = email.text.toString()
        val location = location.text.toString()
        val user = User(name, "", nickname, email, location)

        Log.d ("MAD_LOG", "SEND-USER: $user")

        val intent = Intent()
        intent.putExtra(IntentRequest.UserData.NAME, user)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    // Punto 6

    @RequiresApi(Build.VERSION_CODES.N)
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { cameraIntent ->
            cameraIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException){
                    // Error
                    null
                }
                photoFile?.also {
                    val photoUri: Uri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", it)
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    startActivityForResult(cameraIntent, REQUEST_TAKE_PHOTO)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK){
            val imgBitmap = data?.extras?.get("data") as Bitmap
            user_photo.setImageBitmap(imgBitmap)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timestamp: String = SimpleDateFormat("yyyyMMDD_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile("JPEG_${timestamp}", ".jpg", storageDir).apply {
            // Save a file
            currentPhotoPath = absolutePath
        }
    }
}
