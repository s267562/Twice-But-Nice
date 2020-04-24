package it.polito.mad.project.utils

import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.google.gson.Gson
import it.polito.mad.project.R
import it.polito.mad.project.enums.StoreFileKey
import it.polito.mad.project.models.User

class Util {
    companion object {
        fun displayMessage(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }

        fun <T> saveToStoreFile(activity: Activity?, key: String, value: T) {
            val sharedPref = activity?.getSharedPreferences(activity?.resources.getString(R.string.app_store_file_name), Context.MODE_PRIVATE)
            val prefsEditor = sharedPref?.edit()
            prefsEditor?.putString(key, Gson().toJson(value));
            prefsEditor?.commit();
        }

        fun loadJsonFromStoreFile(activity: Activity?, key: String): String? {
            // Load store file of our app from shared preferences
            val sharedPreferences = activity?.getSharedPreferences(activity?.resources.getString(R.string.app_store_file_name), Context.MODE_PRIVATE)

            // Load from the store file the user object. For the first time we load empty string.
            return sharedPreferences?.getString(key, "")
        }
    }
}