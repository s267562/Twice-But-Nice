package it.polito.mad.project.fragments.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import it.polito.mad.project.models.User
import it.polito.mad.project.repositories.UserRepository

class UserViewModel : ViewModel() {
    val user = MutableLiveData<User>()
    private val userRepository = UserRepository()

    fun isAuth(): Boolean {
        return  userRepository.isCurrentUserAuth()
    }

    fun saveUser(user: User): Task<Void> {
        return userRepository.saveUser(user)
    }

    fun loadUser() {
        userRepository.getUser().addOnSuccessListener {
            user.value = it?.toObject(User::class.java)?:null
        }
    }

}