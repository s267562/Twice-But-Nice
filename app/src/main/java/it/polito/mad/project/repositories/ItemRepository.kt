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
    var database = FirebaseFirestore.getInstance()
    private var auth = FirebaseAuth.getInstance()

    // save user to firebase
    fun saveItem(item: Item): Task<Void> {
        return database.collection("items").document(item.id!!).set(item)
    }
    // get item from firebase
    fun getItem(id: String): Task<DocumentSnapshot> {
        return  database.collection("items").document(id).get()
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

    fun getAuthUserId(): String {
        return auth.currentUser?.uid!!
    }
}