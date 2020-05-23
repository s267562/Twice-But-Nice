package it.polito.mad.project.fragments.authentication

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import it.polito.mad.project.commons.viewmodels.LoadingViewModel
import it.polito.mad.project.models.User
import it.polito.mad.project.repositories.AuthRepository

class AuthViewModel : LoadingViewModel() {
    var loggedIn = MutableLiveData(false)
    var loggedOut = MutableLiveData(false)
    var registeredIn = MutableLiveData(false)

    var errorMessage = ""
    private var user = User("")

    private var authRepository = AuthRepository()

    init {
        error = false
        loggedIn.value = authRepository.getFirebaseUser() != null
        registeredIn.value = false
        loggedOut.value = false
    }

    fun registerUser(registerUser: User) {
        pushLoader()
        user = registerUser
        errorMessage = ""
        authRepository.signUpWithEmailPassword(user.email, user.password)
            .addOnSuccessListener { authResult ->
                user.id = authResult.user!!.uid
                authRepository.getNotificationId()
                    .addOnCompleteListener { instanceResult ->
                        if (instanceResult.isSuccessful) {
                            user.notificationId = instanceResult.result?.token ?: ""
                        }
                        authRepository.updateUser(user)
                            .addOnCompleteListener {
                                error = !it.isSuccessful
                                errorMessage = it.exception?.message ?: ""
                                popLoader()
                                registeredIn.value = true
                            }
                    }
            }
            .addOnFailureListener {
                errorMessage = it.message?:""
                error = true
                popLoader()
                registeredIn.value = false
            }
    }

    fun loginWithCredential(credential: AuthCredential) {
        pushLoader()
        errorMessage = ""
        authRepository.signInWithCredential(credential)
            .addOnSuccessListener { authResult ->
                val firebaseUser = authResult.user as FirebaseUser
                authRepository.getNotificationId()
                    .addOnCompleteListener { instanceResult ->
                        var notificationId = ""
                        if (instanceResult.isSuccessful) {
                            notificationId = instanceResult.result?.token?:""
                        }
                        authRepository.getLoggedUser()
                            .addOnCompleteListener {
                                user = User(firebaseUser.displayName?:"")
                                user.id = firebaseUser.uid
                                user.email = firebaseUser.email?:""
                                user.nickname = user.email.split("@")[0]

                                if (it.isSuccessful) {
                                    user = it.result?.toObject(User::class.java)?:user
                                }
                                user.notificationId = notificationId
                                authRepository.updateUser(user)
                                    .addOnCompleteListener {
                                        popLoader()
                                        error = false
                                        loggedIn.value = true
                                    }
                            }
                    }
            }
            .addOnFailureListener {
                popLoader()
                errorMessage = it.message?:""
                error = true
                loggedIn.value = false
            }
    }

    fun loginWithEmailPassword(email: String, password: String) {
        pushLoader()
        errorMessage = ""
        authRepository.signInWithEmailPassword(email, password)
            .addOnSuccessListener {
                popLoader()
                error = false
                loggedIn.value = true
            }
            .addOnFailureListener {
                popLoader()
                errorMessage = it.message?:""
                error = true
                loggedIn.value = false
            }
    }

    fun logout() {
        loggedIn.value = false
        registeredIn.value = false
        loggedOut.value = true
        return authRepository.signOut()
    }

}