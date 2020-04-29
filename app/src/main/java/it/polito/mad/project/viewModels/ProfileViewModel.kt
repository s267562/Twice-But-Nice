package it.polito.mad.project.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.polito.mad.project.models.User

class ProfileViewModel : ViewModel() {
    val user = MutableLiveData<User>()
}