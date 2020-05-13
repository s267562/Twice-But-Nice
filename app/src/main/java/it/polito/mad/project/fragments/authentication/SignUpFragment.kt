package it.polito.mad.project.fragments.authentication

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import it.polito.mad.project.R


class SignUpFragment: Fragment() {
    lateinit var auFullname: EditText
    lateinit var auNickname: EditText
    lateinit var auEmail: EditText
    lateinit var auLocation: EditText
    lateinit var auPassword: EditText
    lateinit var auRegBtn: Button

    lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auFullname = activity?.findViewById(R.id.au_fullname)!!
        auNickname = activity?.findViewById(R.id.au_nickname)!!
        auEmail = activity?.findViewById(R.id.au_email)!!
        auLocation = activity?.findViewById(R.id.au_location)!!
        auPassword = activity?.findViewById(R.id.au_password)!!

        mAuth = FirebaseAuth.getInstance()

        auRegBtn.setOnClickListener{

        }
    }

}