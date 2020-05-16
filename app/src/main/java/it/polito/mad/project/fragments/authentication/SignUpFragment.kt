package it.polito.mad.project.fragments.authentication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import it.polito.mad.project.R
import it.polito.mad.project.enums.StoreFileKey
import it.polito.mad.project.fragments.common.StoreFileFragment
import it.polito.mad.project.models.User
import kotlinx.android.synthetic.main.fragment_signup.*

// Sign Up with email and password

class SignUpFragment: StoreFileFragment() {
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()

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
            user.isCreated = true

            firebaseAuth.createUserWithEmailAndPassword(user.email, user.password)
                .addOnCompleteListener { it1 ->
                    if (it1.isSuccessful) {
                        user.id = it1.result?.user?.uid?:""
                        Toast.makeText(activity, "Successfully Registered", Toast.LENGTH_LONG).show()
                        saveToStoreFile(StoreFileKey.USER, user)
                        findNavController().popBackStack()
                    } else {
                        Toast.makeText(activity, it1.exception?.message, Toast.LENGTH_LONG).show()
                    }
                }
        }

        regToLog.setOnClickListener {
            findNavController().popBackStack()
        }
    }

}