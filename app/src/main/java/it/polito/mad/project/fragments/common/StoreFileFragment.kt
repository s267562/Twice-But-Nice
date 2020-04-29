package it.polito.mad.project.fragments.common

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import it.polito.mad.project.R

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

    open fun removeFromStoreFile(key: String) {
        val sharedPref = this.activity?.getSharedPreferences(getString(R.string.app_store_file_name), Context.MODE_PRIVATE)
        val prefsEditor = sharedPref?.edit()
        prefsEditor?.remove(key)
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