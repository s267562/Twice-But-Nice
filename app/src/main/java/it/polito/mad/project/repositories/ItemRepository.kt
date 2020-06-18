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
import it.polito.mad.project.enums.items.ItemStatus
import it.polito.mad.project.models.item.Item
import it.polito.mad.project.models.item.ItemInterest
import it.polito.mad.project.models.user.User
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
            val localFile = File.createTempFile(item.id.toString(),".jpg")
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

    // get the interest of the user for the item
    fun getItemInterest(userId: String, id: String): Task<DocumentSnapshot> {
        return database.collection("items").document(id).collection("users").document(userId).get()
    }

    // get interested user ids list by item id
    fun getInterestedUserIds(id: String): Task<QuerySnapshot> {
        return database.collection("items").document(id).collection("users").whereEqualTo("interested", true).get()
    }

    // get user list from userIds list
    fun getUsersByUserIds(userIds: List<String>): Task<QuerySnapshot> {
        return database.collection("users").whereIn("id", userIds).get()
    }

    // get list of items of a single user
    fun getItemsByUserId(userId: String): Task<QuerySnapshot> {
        return database.collection("items").whereEqualTo("ownerId", userId).get()
    }

    // Get only available items
    fun getAvailableItems(): Task<QuerySnapshot> {
        return database.collection("items").whereEqualTo("status", ItemStatus.Available.toString()).get()
    }

    // get the authenticated user id
    fun getAuthUserId(): String? {
        return auth.currentUser?.uid
    }

    fun getBoughtItems(userId: String): Task<QuerySnapshot> {
        return database.collection("items").whereEqualTo("status", ItemStatus.Sold.toString()).whereEqualTo("buyerId",userId).get()
    }

    fun getSoldItems(userId: String): Task<QuerySnapshot> {
        return database.collection("items").whereEqualTo("ownerId", userId).whereEqualTo("status", ItemStatus.Sold.toString()).get()
    }
    // set the interest of the user for the item
    fun saveItemInterest(userId: String, id: String, itemInterest: ItemInterest): Task<Void> {
        database.collection("users").document(userId).collection("interestedItems").document(id).set(itemInterest)
        return database.collection("items").document(id).collection("users").document(userId).set(itemInterest)
    }

    fun getInterestedItemsIDs (): Task<QuerySnapshot> {
        val userId = auth.currentUser!!.uid
        return database.collection("users").document(userId).collection("interestedItems").whereEqualTo("interested", true).get()
    }

    fun getItemsByItemsIds(itemIds: List<String>): Task<QuerySnapshot>  {
        return database.collection("items").whereIn("id", itemIds).get()
    }

    // sell item to firebase
    fun sellItem(
        item: Item,
        buyerId: String,
        usersInterested: List<User>
    ): Task<Void> {
        //saveItemImage(item) //non dovrebbe essere necessaria
        database.collection("users").document(buyerId).collection("interestedItems").document(item.id!!).delete()
        for (user:User in usersInterested){
            database.collection("items").document(item.id!!).collection("users").document(user.id).delete()
        }
        return database.collection("items").document(item.id!!).set(item)
    }


}