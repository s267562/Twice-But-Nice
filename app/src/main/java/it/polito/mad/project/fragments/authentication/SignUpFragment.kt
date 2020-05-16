package it.polito.mad.project.fragments.authentication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.mad.project.R
import it.polito.mad.project.enums.StoreFileKey
import it.polito.mad.project.fragments.common.StoreFileFragment
import it.polito.mad.project.models.User
import kotlinx.android.synthetic.main.fragment_signup.*

// Sign Up with email and password

class SignUpFragment: StoreFileFragment() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseStore: FirebaseFirestore

    override fun onStart() {
        super.onStart()
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseStore = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        regBtn.setOnClickListener {
            var user = User(au_fullname.text.toString())
            user.email = au_email.text.toString()
            user.password = au_password.text.toString()
            user.nickname = au_nickname.text.toString()
            user.location = au_location.text.toString()

            firebaseAuth.createUserWithEmailAndPassword(user.email, user.password)
                .addOnSuccessListener {
                    user.id = it.user?.uid?:""
                    user.created = true

                    firebaseStore.collection("users").document(user.id).set(user)
                        .addOnSuccessListener {
                            Toast.makeText(activity, "Successfully Registered. User saved remote", Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(activity, "Successfully Registered. User saved local", Toast.LENGTH_LONG).show()
                            saveToStoreFile(StoreFileKey.USER, user)
                        }

                    findNavController().popBackStack()
                }
                .addOnFailureListener {
                    Toast.makeText(activity, it.message, Toast.LENGTH_LONG).show()
                }
        }

        regToLog.setOnClickListener {
            findNavController().popBackStack()
        }
    }

}