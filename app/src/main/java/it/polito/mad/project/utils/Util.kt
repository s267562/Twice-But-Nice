package it.polito.mad.project.utils

import android.content.Context
import android.widget.Toast

class Util {
    companion object {
        fun displayMessage(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

}