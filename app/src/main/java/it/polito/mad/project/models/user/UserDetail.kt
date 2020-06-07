package it.polito.mad.project.models.user

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData

class UserDetail {
    val data = MutableLiveData<User>()
    var localData: User? = null

    val image = MutableLiveData<Bitmap>()
    var localImage: Bitmap? = null

}