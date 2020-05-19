package it.polito.mad.project.fragments.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FileDownloadTask
import it.polito.mad.project.commons.CommonViewModel
import it.polito.mad.project.models.User
import it.polito.mad.project.repositories.UserRepository

class UserViewModel : CommonViewModel() {
    val user = MutableLiveData<User>()
    val userPhotoProfile = MutableLiveData<Bitmap>()

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
            userPhotoProfile.value = null
            pushLoader()
            userRepository.getUserById(verifiedId)
                ?.addOnSuccessListener {
                    user.value = it.toObject(User::class.java)
                    if (user.value!!.photoProfilePath.isNotBlank() ) {
                        userRepository.getUserPhoto(user.value!!).addOnSuccessListener {
                            userPhotoProfile.value = BitmapFactory.decodeFile(user.value!!.photoProfilePath)
                        }
                    }
                    error = false
                    popLoader()
                }
                ?.addOnFailureListener {
                    error = true
                    popLoader()
                }
        }
    }

}