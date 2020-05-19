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
        pushLoader()
        return userRepository.saveUser(user)
            .addOnSuccessListener {
                this.user.value = user
                popLoader()
                error = false
            }
            .addOnFailureListener {
                popLoader()
                error = true
            }
    }

    fun loadUser(id: String? = null) {
        val verifiedId = id ?: userRepository.getAuthUserId()
        if (verifiedId != user.value?.id) {
            pushLoader()
            userRepository.getUserById(verifiedId)
                ?.addOnSuccessListener {
                    user.value = it.toObject(User::class.java)
                    loadPhoto()
                    userLoaded = false
                    error = false
                    popLoader()
                }
                ?.addOnFailureListener {
                    error = true
                    popLoader()
                }
        }
    }

    fun isAuthUser(): Boolean {
        if (userLoaded)
            return user.value?.id == userRepository.getAuthUserId()
        return true
    }

    private fun loadPhoto(){
            userRepository.getUserPhoto(user.value!!)
    }


}