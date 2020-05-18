package it.polito.mad.project.repositories

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import it.polito.mad.project.models.Item
import it.polito.mad.project.models.User

class ItemRepository {
    private var database = FirebaseFirestore.getInstance()
    private var auth = FirebaseAuth.getInstance()

    // save user to firebase
    fun saveItem(item: Item): Task<Void> {
        val userId = auth.currentUser!!.uid
        item.itemId = "$userId-${item.id}"
        item.user = "$userId"
        return database.collection("items").document(item.itemId).set(item)
    }

    //save user interested to an item
    fun saveUserToItem(user: User, item: Item ): Task<Void> {
        val userId = auth.currentUser!!.uid
        return database.collection("items").document(item.itemId).collection("users").document().set(user)
    }

    // get saved addresses from firebase
    fun getUserItem(id: Int): Task<DocumentSnapshot> {
        val userId = auth.currentUser!!.uid
        //return database.collection("users").document(userId).collection("items").document("$userId-$id").get()
        //return database.collection("items").whereEqualTo("user", userId).get()
        return  database.collection("items").document("$userId-$id").get()
    }

    // get saved addresses from firebase
    // get list of items of a single user
    fun getUserItems(): Task<QuerySnapshot> {
        val userId = auth.currentUser!!.uid
        return database.collection("items").whereEqualTo("user",userId).get()
    }

    //get all items
    fun getAllItems(): Task<QuerySnapshot> {
        val userId = auth.currentUser!!.uid
        return database.collection("items").get()
    }

    //get list of users interested to an item
    fun getUsersItem(item: Item): Task<QuerySnapshot> {
        return database.collection("items").document(item.itemId).collection("users").get()
    }
    fun isCurrentUserAuth(): Boolean {
        return auth.currentUser != null
    }
}