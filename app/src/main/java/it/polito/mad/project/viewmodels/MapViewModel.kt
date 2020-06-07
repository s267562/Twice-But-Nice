package it.polito.mad.project.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MapViewModel : ViewModel() {
    var updateLocation = MutableLiveData<Boolean>()
    var location: String? = null
}