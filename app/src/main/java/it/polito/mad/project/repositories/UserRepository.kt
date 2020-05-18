package it.polito.mad.project.repositories

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import it.polito.mad.project.models.User
import java.io.File
import java.io.FileOutputStream


class UserRepository {
    private var database = FirebaseFirestore.getInstance()
    private var auth = FirebaseAuth.getInstance()
    private var mStorageRef: StorageReference = FirebaseStorage.getInstance().reference

    // save user to firebase
    fun saveUser(user: User): Task<Void> {
        return database.collection("users").document(auth.currentUser!!.uid).set(user)
    }

    // get saved addresses from firebase
    fun getUser(): Task<DocumentSnapshot>? {
        val userId = auth.currentUser!!.uid

        return database.collection("users").document(userId).get()

    }
    fun loadUserPhoto(user:User){
        val userId = auth.currentUser!!.uid
        val photoRef = mStorageRef.child("user/$userId")
        var file = Uri.fromFile(File(user.photoProfilePath))
        val bitmap = BitmapFactory.decodeFile(user.photoProfilePath)
        val localFile = File.createTempFile(userId,".jpg")
        val fOut = FileOutputStream(localFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG,100, fOut);
        user.photoProfilePath=localFile.path
        photoRef.putFile(file)
    }

    fun getUserPhoto(user:User) {
        val userId = auth.currentUser!!.uid
        val photoRef = mStorageRef.child("user/$userId")
        val localFile = File.createTempFile(userId,".jpg")
        user.photoProfilePath = localFile.absolutePath
        photoRef.getFile(localFile)

    }

    fun isCurrentUserAuth(): Boolean {
        return auth.currentUser != null
    }
}