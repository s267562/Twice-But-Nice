package it.polito.mad.project.commons

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

open class CommonViewModel: ViewModel() {
    val loader = MutableLiveData<Boolean>()
    var error: Boolean = false
}