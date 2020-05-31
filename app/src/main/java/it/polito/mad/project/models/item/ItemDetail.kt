package it.polito.mad.project.models.item

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData

class ItemDetail {
    // Textual info
    val data = MutableLiveData<Item>()
    var localData: Item? = null

    // Image
    val image = MutableLiveData<Bitmap>()
    var localImage: Bitmap? = null

    // Interest info
    val interest = ItemInterest()
}