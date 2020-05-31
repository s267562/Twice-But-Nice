package it.polito.mad.project.repositories

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import it.polito.mad.project.models.user.User

class AuthRepository {

    private var database = FirebaseFirestore.getInstance()
    private var auth = FirebaseAuth.getInstance()
    private var iid = FirebaseInstanceId.getInstance()

    fun getFirebaseUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun signUpWithEmailPassword(email: String, password: String): Task<AuthResult> {
        return auth.createUserWithEmailAndPassword(email, password)
    }

    fun signInWithEmailPassword(email: String, password: String): Task<AuthResult> {
        return auth.signInWithEmailAndPassword(email, password)
    }

    fun signInWithCredential(credential: AuthCredential): Task<AuthResult> {
        return auth.signInWithCredential(credential)
    }

    fun getLoggedUser(): Task<DocumentSnapshot> {
        return database.collection("users").document(auth.currentUser!!.uid).get()
    }

    fun getNotificationId(): Task<InstanceIdResult> {
        return iid.instanceId
    }

    fun updateUser(user: User): Task<Void> {
        return database.collection("users").document(user.id).set(user)
    }

    fun signOut() {
        return auth.signOut()
    }
}
