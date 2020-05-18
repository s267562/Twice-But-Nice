package it.polito.mad.project.fragments.profile

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import it.polito.mad.project.commons.CommonViewModel
import it.polito.mad.project.models.User
import it.polito.mad.project.repositories.UserRepository

class UserViewModel : CommonViewModel() {
    val user = MutableLiveData<User>()
    private var userLoaded = false
    private val userRepository = UserRepository()

    init {
        loadUser()
    }

    fun saveUser(user: User): Task<Void> {
        loader.value = true
        return userRepository.saveUser(user)
            .addOnSuccessListener {
                this.user.value = user
                userRepository.loadUserPhoto(this.user.value!!)
                loader.value = false
                error = false
            }
            .addOnFailureListener {
                loader.value = false
                error = true
            }
    }

    fun loadUser(id: String? = null) {
        val verifiedId = id?:userRepository.getAuthUserId()
        if (verifiedId != user.value?.id) {
            loader.value = true
            userRepository.getUserById(verifiedId)
            ?.addOnSuccessListener {
                    user.value = it.toObject(User::class.java)
                loadPhoto()
                userLoaded=false
                    loader.value = false
                    error = false
                }
            ?.addOnFailureListener {
                    error = true
                    loader.value = false
                }
        }
     }
    }

    fun isAuthUser(): Boolean {
        return user.value?.id == userRepository.getAuthUserId()
    }

    private fun loadPhoto(){
            userRepository.getUserPhoto(user.value!!)
    }


}