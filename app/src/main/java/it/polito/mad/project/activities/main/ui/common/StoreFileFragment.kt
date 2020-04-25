package it.polito.mad.project.activities.main.ui.common

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import it.polito.mad.project.R
import it.polito.mad.project.enums.IntentRequest
import it.polito.mad.project.enums.StoreFileKey
import it.polito.mad.project.models.Item
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

open class StoreFileFragment : Fragment() {

    open val SELECT_IMAGE = 2

    open fun <T> loadFromStoreFile(key: String, classOf: Class<T>): T? {
        var value: T? = null
        // Load store file of our app from shared preferences
        val sharedPreferences = this.activity?.getSharedPreferences(
            getString(R.string.app_store_file_name),
            Context.MODE_PRIVATE
        )
        // Load from the store file
        val itemJson: String? = sharedPreferences?.getString(key, "")
        if (itemJson != null && itemJson.isNotEmpty()) {
            // Assign the stored list of items
            value = Gson().fromJson(itemJson, classOf)
        }
        return value
    }

    open fun <T> saveToStoreFile(key: String, value: T) {
        val sharedPref = this.activity?.getSharedPreferences(getString(R.string.app_store_file_name), Context.MODE_PRIVATE)
        val prefsEditor = sharedPref?.edit()
        prefsEditor?.putString(key, Gson().toJson(value))
        prefsEditor?.commit()
    }

    // Methods to manage the camera
    open fun openGallery(){
        val galleryIntent = Intent()
        galleryIntent.type = "image/*"
        galleryIntent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(galleryIntent, "Select an image from Gallery"), SELECT_IMAGE)
    }
}