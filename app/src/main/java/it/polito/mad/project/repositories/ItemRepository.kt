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
    fun saveUserItem(item: Item): Task<Void> {
        val userId = auth.currentUser!!.uid
        return database.collection("users").document(userId).collection("items").document("$userId-${item.id}").set(item)
    }

    // get saved addresses from firebase
    fun getUserItem(id: Int): Task<DocumentSnapshot> {
        val userId = auth.currentUser!!.uid
        return database.collection("users").document(userId).collection("items").document("$userId-$id").get()
    }

    // get saved addresses from firebase
    fun getUserItems(): Task<QuerySnapshot> {
        val userId = auth.currentUser!!.uid
        return database.collection("users").document(userId).collection("items").get()
    }

    fun isCurrentUserAuth(): Boolean {
        return auth.currentUser != null
    }
}