package it.polito.mad.project.repositories

import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.mad.project.models.User
import kotlinx.android.synthetic.main.fragment_login.*

class UserRepository {
    private var database = FirebaseFirestore.getInstance()
    private var auth = FirebaseAuth.getInstance()

    // save user to firebase
    fun saveUser(user: User): Task<Void> {
        user.id = auth.currentUser!!.uid
        return database.collection("users").document(user.id).set(user)
    }

    // get the user by id. if null return logged user
    fun getUserById(id: String? = null): Task<DocumentSnapshot> {
        var id = id?:auth.currentUser!!.uid
        return database.collection("users").document(id).get()
    }

    fun getAuthUserId(): String? {
        return auth.currentUser?.uid
    }
}