package it.polito.mad.project.fragments.authentication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import it.polito.mad.project.R
import it.polito.mad.project.models.User
import kotlinx.android.synthetic.main.fragment_signup.*

class SignUpFragment: Fragment() {
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authViewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authViewModel.loggedIn.observe(viewLifecycleOwner, Observer {
            if (it) {
                if (!authViewModel.error)
                    findNavController().popBackStack()
                else
                    Toast.makeText(context, authViewModel.errorMessage, Toast.LENGTH_LONG)
            }
        })

        regBtn.setOnClickListener {
            var dataInserted : Boolean = true

            if (au_nickname.text.isNullOrBlank()){
                au_nickname.error = "Insert Nickname"
                dataInserted = false
            }
            if (au_fullname.text.isNullOrBlank()){
                au_fullname.error = "Insert full name"
                dataInserted = false

            }
            if (au_email.text.isNullOrBlank()){
                au_email.error = "Insert email address"
                dataInserted = false

            }
            if (au_location.text.isNullOrBlank()){
                au_location.error = "Insert full name"
                dataInserted = false
            }
            if (au_password.text.isNullOrBlank()){
                au_password.error = "Insert password"
                dataInserted = false
            }


            if (dataInserted){
                val user = User(au_fullname.text.toString())
                user.email = au_email.text.toString()
                user.nickname = au_nickname.text.toString()
                user.location = au_location.text.toString()
                authViewModel.registerUser(user)
            }

        }

        regToLog.setOnClickListener {
            findNavController().popBackStack()
        }
    }

}