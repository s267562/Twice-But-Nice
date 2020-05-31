package it.polito.mad.project.repositories

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import it.polito.mad.project.models.user.User
import java.io.File
import java.io.FileOutputStream


class UserRepository {
    private var storage = FirebaseStorage.getInstance()
    private var database = FirebaseFirestore.getInstance()
    private var auth = FirebaseAuth.getInstance()

    // save user to firebase
    fun saveUser(user: User): Task<Void> {
        user.id = auth.currentUser!!.uid
        saveUserPhoto(user)
        return database.collection("users").document(user.id).set(user)
    }

    // get the user by id. if null return logged user
    fun getUserById(id: String? = null): Task<DocumentSnapshot>? {
        val userId = id ?: auth.currentUser!!.uid
        return database.collection("users").document(userId).get()
    }
    
    private fun saveUserPhoto(user: User) {
        if(user.photoProfilePath.isNotBlank() && File(user.photoProfilePath).isFile ) {
            val userId = auth.currentUser!!.uid
            val photoRef = storage.reference.child("user/$userId")
            val file = Uri.fromFile(File(user.photoProfilePath))
            val bitmap = BitmapFactory.decodeFile(user.photoProfilePath)
            val localFile = File.createTempFile(userId,".jpg")
            val fOut = FileOutputStream(localFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100, fOut);
            user.photoProfilePath = localFile.path
            photoRef.putFile(file)
        }
    }

    fun getUserPhoto(id: String, localFile: File): FileDownloadTask {
        val photoRef = storage.reference.child("user/$id")
        return photoRef.getFile(localFile)
    }

    fun getFirebaseUser(): FirebaseUser? {
        return auth.currentUser
    }
}