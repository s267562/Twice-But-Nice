package it.polito.mad.project.viewmodels

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import it.polito.mad.project.commons.viewmodels.LoadingViewModel
import it.polito.mad.project.models.User
import it.polito.mad.project.repositories.UserRepository
import java.io.File

class UserViewModel : LoadingViewModel() {
    val user = MutableLiveData<User>()
    val userPhotoProfile = MutableLiveData<Bitmap>()

    private val userRepository = UserRepository()
    private lateinit var authUser: User
    private var authPhotoProfile: Bitmap? = null

    init {
        loadUser()
    }

    fun saveUser(user: User): Task<Void> {
        pushLoader()
        user.notificationId = this.user.value!!.notificationId
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
        val verifiedId = id ?: userRepository.getFirebaseUser()!!.uid
        if (verifiedId != user.value?.id) {
            pushLoader()
            userPhotoProfile.value = null
            userRepository.getUserById(verifiedId)
                ?.addOnSuccessListener {
                    user.value = it.toObject(User::class.java)
                    if (id == null)
                        authUser = user.value!!
                    popLoader()
                    loadUserPhotoProfile(user.value!!.id, user.value!!.photoProfilePath)
                    error = false
                }
                ?.addOnFailureListener {
                    error = true
                    popLoader()
                }
        }
    }

    private fun loadUserPhotoProfile(id: String, photoProfilePath: String) {
        if (photoProfilePath.isNotBlank()) {
            val image = BitmapFactory.decodeFile(photoProfilePath)
            if (image != null) {
                userPhotoProfile.value = image
                if (id == authUser.id) {
                    authPhotoProfile = image
                }
            } else {
                val localFile = File.createTempFile(id,".jpg")
                userRepository.getUserPhoto(id, localFile).addOnSuccessListener {
                    userPhotoProfile.value =  BitmapFactory.decodeFile(localFile.path)
                    user.value!!.photoProfilePath = localFile.path
                    if (id == authUser.id) {
                        authPhotoProfile = userPhotoProfile.value!!
                    }
                }
            }
        }
    }

    fun getUserId(): String {
        return user.value!!.id
    }

    fun resetUser() {
        user.value = authUser
        if (authPhotoProfile != null)
            userPhotoProfile.value = authPhotoProfile
    }

}