package it.polito.mad.project.repositories

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import it.polito.mad.project.models.Item
import it.polito.mad.project.models.User
import java.io.File
import java.io.FileOutputStream

class ItemRepository {
    private var database = FirebaseFirestore.getInstance()
    private var auth = FirebaseAuth.getInstance()
    private var mStorageRef: StorageReference = FirebaseStorage.getInstance().reference

    // save item to firebase
    fun saveItem(item: Item): Task<Void> {
        storeItemPhoto(item)
        return database.collection("items").document(item.id!!).set(item)
    }

    private fun storeItemPhoto(item: Item) {
        if(item.imagePath == null || item.imagePath!!.isEmpty() || !File(item.imagePath).isFile )
            return
        val photoRef = mStorageRef.child("item/${item.id}")
        var file = Uri.fromFile(File(item.imagePath))
        val bitmap = BitmapFactory.decodeFile(item.imagePath)
        val localFile = File.createTempFile(item.id,".jpg")
        val fOut = FileOutputStream(localFile)
        bitmap.compress(Bitmap.CompressFormat.JPEG,100, fOut);
        item.imagePath=localFile.path
        photoRef.putFile(file)
    }

    // get item from firebase
    fun getItem(id: String): Task<DocumentSnapshot> {
        return  database.collection("items").document(id).get()
    }

    // get item from firebase
    fun getItemDocument(id: String): DocumentReference {
        return  database.collection("items").document(id)
    }

    fun getItemPhoto(localFile: File, item:Item): FileDownloadTask {
        val photoRef = mStorageRef.child("item/${item.id}")
        return photoRef.getFile(localFile)
    }

    //save user interested to an item
    fun saveUserToItem(user: User, id: String): Task<Void> {
        return database.collection("items").document(id).collection("users").document().set(user)
    }

    //get list of users interested to an item
    fun getItemUsers(id: String): Task<QuerySnapshot> {
        return database.collection("items").document(id).collection("users").get()
    }

    // get list of items of a single user
    fun getUserItems(userId: String): Task<QuerySnapshot> {
        return database.collection("items").whereEqualTo("user", userId).get()
    }

    //get all items
    fun getAllItems(): Task<QuerySnapshot> {
        return database.collection("items").get()
    }

    // Get only available items
    fun getAvailableItems(): Task<QuerySnapshot> {
        return database.collection("items").whereEqualTo("status", "Available").get()
    }

    fun getAuthUserId(): String {
        return auth.currentUser?.uid!!
    }
}