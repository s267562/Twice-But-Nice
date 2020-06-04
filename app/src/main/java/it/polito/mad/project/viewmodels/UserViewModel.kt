package it.polito.mad.project.viewmodels

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import it.polito.mad.project.adapters.ReviewAdapter
import it.polito.mad.project.commons.viewmodels.LoadingViewModel
import it.polito.mad.project.models.item.Item
import it.polito.mad.project.models.item.ItemList
import it.polito.mad.project.models.user.User
import it.polito.mad.project.models.user.UserDetail
import it.polito.mad.project.repositories.ItemRepository
import it.polito.mad.project.repositories.UserRepository
import java.io.File

class UserViewModel : LoadingViewModel() {
    private val userRepository = UserRepository()
    private val itemRepository = ItemRepository()

    private lateinit var authUser: User
    private var authPhotoProfile: Bitmap? = null

    val user = UserDetail()
    // Reviews (sold items with review)
    val reviews = ItemList(ReviewAdapter(mutableListOf()))

    init {
        loadUser()
    }

    fun saveUser(user: User): Task<Void> {
        pushLoader()
        user.notificationId = this.user.data.value!!.notificationId
        return userRepository.saveUser(user)
            .addOnSuccessListener {
                this.user.data.value = user
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
        if (verifiedId != user.data.value?.id) {
            pushLoader()
            user.image.value = null
            userRepository.getUserById(verifiedId)
                ?.addOnSuccessListener {
                    user.data.value = it.toObject(User::class.java)
                    if (id == null)
                        authUser = user.data.value!!
                    popLoader()
                    loadUserPhotoProfile(user.data.value!!.id, user.data.value!!.photoProfilePath)
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
                user.image.value = image
                if (id == authUser.id) {
                    authPhotoProfile = image
                }
            } else {
                val localFile = File.createTempFile(id,".jpg")
                userRepository.getUserPhoto(id, localFile).addOnSuccessListener {
                    user.image.value =  BitmapFactory.decodeFile(localFile.path)
                    user.data.value!!.photoProfilePath = localFile.path
                    if (id == authUser.id) {
                        authPhotoProfile = user.image.value!!
                    }
                }
            }
        }
    }

    fun getUserId(): String {
        return user.data.value!!.id
    }

    fun resetUser() {
        user.data.value = authUser
        if (authPhotoProfile != null)
            user.image.value = authPhotoProfile
    }

    fun loadReviews( userId: String?) {
        val id = userId ?: userRepository.getFirebaseUser()!!.uid

        /* load all sold items with review */
        pushLoader()
        itemRepository.getSoldItems(id)
            .addOnSuccessListener { it ->
                reviews.items.clear()
                reviews.items.addAll(it.toObjects(Item::class.java)
                    .filter { it -> it.review != null })
                popLoader()
                error = false
            }.addOnFailureListener {
                popLoader()
                error = true
            }
    }

}