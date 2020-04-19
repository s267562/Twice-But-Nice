package it.polito.mad.project

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UserViewModel : ViewModel() {
    var userId: String = ""
    val user = MutableLiveData<User>()
}