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
        return database.collection("users").document(auth.currentUser!!.uid).set(user)
    }

    // get saved addresses from firebase
    fun getUser(): Task<DocumentSnapshot>? {
        return auth.currentUser?.uid?.let { database.collection("users").document(it).get() }
    }

    fun isCurrentUserAuth(): Boolean {
        return auth.currentUser != null
    }
}