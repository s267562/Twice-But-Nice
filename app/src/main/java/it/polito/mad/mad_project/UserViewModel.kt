package it.polito.mad.mad_project

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UserViewModel : ViewModel() {
    var userId: String = ""
    val user = MutableLiveData<User>()
}