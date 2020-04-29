package it.polito.mad.project.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.polito.mad.project.models.User

class UserViewModel : ViewModel() {
    var userId: String = ""
    val user = MutableLiveData<User>()
}