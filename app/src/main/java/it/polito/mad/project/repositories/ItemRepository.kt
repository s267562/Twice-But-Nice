package it.polito.mad.project.repositories

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import it.polito.mad.project.models.Item
import it.polito.mad.project.models.ItemInterest
import java.io.File
import java.io.FileOutputStream

class ItemRepository {
    private var database = FirebaseFirestore.getInstance()
    private var storage = FirebaseStorage.getInstance()
    private var auth = FirebaseAuth.getInstance()

    // set item to firebase
    fun saveItem(item: Item): Task<Void> {
        saveItemImage(item)
        return database.collection("items").document(item.id!!).set(item)
    }

    // set item image
    private fun saveItemImage(item: Item) {
        if(item.imagePath.isNotBlank() && File(item.imagePath).isFile ) {
            val photoRef = storage.reference.child("item/${item.id}")
            val file = Uri.fromFile(File(item.imagePath))
            val bitmap = BitmapFactory.decodeFile(item.imagePath)
            val localFile = File.createTempFile(item.id,".jpg")
            val fOut = FileOutputStream(localFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100, fOut);
            item.imagePath=localFile.path
            photoRef.putFile(file)
        }
    }

    // get item from firebase
    fun getItem(id: String): Task<DocumentSnapshot> {
        return  database.collection("items").document(id).get()
    }

    // get item document from firebase
    fun getItemDocument(id: String): DocumentReference {
        return  database.collection("items").document(id)
    }

    // get item image
    fun getItemImage(id: String, localFile: File): FileDownloadTask {
        val photoRef = storage.reference.child("item/$id")
        return photoRef.getFile(localFile)
    }

    // set the intereste of the user for the item
    fun saveItemInterest(userId: String, id: String, itemInterest: ItemInterest): Task<Void> {
        return database.collection("items").document(id).collection("users").document(userId).set(itemInterest)
    }

    // get the interest of the user for the item
    fun getItemInterest(userId: String, id: String): Task<DocumentSnapshot> {
        return database.collection("items").document(id).collection("users").document(userId).get()
    }

    // get interested user ids list by item id
    fun getInterestedUserIds(id: String): Task<QuerySnapshot> {
        return database.collection("items").document(id).collection("users").whereEqualTo("interest", true).get()
    }

    // get user list from userIds list
    fun getUsersByUserIds(userIds: List<String>): Task<QuerySnapshot> {
        return database.collection("users").whereIn("id", userIds).get()
    }

    // get list of items of a single user
    fun getItemsByUserId(userId: String): Task<QuerySnapshot> {
        return database.collection("items").whereEqualTo("user", userId).get()
    }

    // Get only available items
    fun getAvailableItems(): Task<QuerySnapshot> {
        return database.collection("items").whereEqualTo("status", "Available").get()
    }

    // get the authenticated user id
    fun getAuthUserId(): String {
        return auth.currentUser?.uid!!
    }
}