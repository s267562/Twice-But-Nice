package it.polito.mad.project.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.gson.Gson
import it.polito.mad.project.R
import kotlin.math.roundToInt


class Util {
    companion object {
        fun displayMessage(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }

        fun hideKeyboard(activity: Activity?) {
            val imm =
                activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            val view = activity?.currentFocus
            if (view != null) {
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }

        fun <T> saveToStoreFile(activity: Activity?, key: String, value: T) {
            val sharedPref = activity?.getSharedPreferences(activity.resources.getString(R.string.app_store_file_name), Context.MODE_PRIVATE)
            val prefsEditor = sharedPref?.edit()
            prefsEditor?.putString(key, Gson().toJson(value));
            prefsEditor?.apply()
        }

        fun loadJsonFromStoreFile(activity: Activity?, key: String): String? {
            // Load store file of our app from shared preferences
            val sharedPreferences = activity?.getSharedPreferences(activity.resources.getString(R.string.app_store_file_name), Context.MODE_PRIVATE)

            // Load from the store file the user object. For the first time we load empty string.
            return sharedPreferences?.getString(key, "")
        }

        private fun calculateInSampleSize(
            options: BitmapFactory.Options,
            reqWidth: Int,
            reqHeight: Int
        ): Int {
            // Raw height and width of image
            val height = options.outHeight
            val width = options.outWidth
            var inSampleSize = 1
            if (height > reqHeight || width > reqWidth) {

                // Calculate ratios of height and width to requested height and width
                val heightRatio =
                    (height.toFloat() / reqHeight.toFloat()).roundToInt()
                val widthRatio =
                    (width.toFloat() / reqWidth.toFloat()).roundToInt()

                // Choose the smallest ratio as inSampleSize value, this will guarantee
                // a final image with both dimensions larger than or equal to the
                // requested height and width.
                inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
            }
            return inSampleSize
        }

        fun decodeSampledBitmapFromFile(
            path: String?,
            reqWidth: Int = 320,
            reqHeight: Int = 320
        ): Bitmap? {

            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(path, options)

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inDither = true;
            return BitmapFactory.decodeFile(path, options)
        }
    }


}