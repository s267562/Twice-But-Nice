package it.polito.mad.project.fragments.authentication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import it.polito.mad.project.R
import it.polito.mad.project.models.User
import it.polito.mad.project.viewmodels.AuthViewModel
import kotlinx.android.synthetic.main.fragment_signup.*

class SignUpFragment: Fragment() {
    private lateinit var authViewModel: AuthViewModel

    private var registerEnable: BooleanArray = booleanArrayOf(false, false, false, false, false)
    private val FULL_NAME = 0
    private val NICKNAME = 1
    private val EMAIL = 2
    private val LOCATION = 3
    private val PASSWORD = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authViewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
    }

    override fun onStart() {
        super.onStart()
        authViewModel.registeredIn.observe(viewLifecycleOwner, Observer {
            if (authViewModel.error)
                Toast.makeText(context, authViewModel.errorMessage, Toast.LENGTH_LONG).show()
            else {
                if (it) {
                    findNavController().popBackStack()
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        au_fullname.doOnTextChanged { text, start, count, after ->
            if(!text.isNullOrBlank()) {
                /* full name not blank */
                if(!registerEnable[FULL_NAME]) registerEnable[FULL_NAME] = true
                if(registerEnable[NICKNAME] &&
                    registerEnable[EMAIL] &&
                    registerEnable[LOCATION] &&
                    registerEnable[PASSWORD] &&
                    !regBtn.isEnabled) {
                    /* other field not blank and button not enabled */
                    enableButton(regBtn)
                }
            } else {
                if(registerEnable[FULL_NAME]) {
                    registerEnable[FULL_NAME] = false
                    disableButton(regBtn)
                }
            }
        }

        au_nickname.doOnTextChanged { text, start, count, after ->
            if(!text.isNullOrBlank()) {
                /* nickname not blank */
                if(!registerEnable[NICKNAME]) registerEnable[NICKNAME] = true
                if(registerEnable[FULL_NAME] &&
                    registerEnable[EMAIL] &&
                    registerEnable[LOCATION] &&
                    registerEnable[PASSWORD] &&
                    !regBtn.isEnabled) {
                    /* other field not blank and button not enabled */
                    enableButton(regBtn)
                }
            } else {
                if(registerEnable[NICKNAME]) {
                    registerEnable[NICKNAME] = false
                    disableButton(regBtn)
                }
            }
        }

        au_email.doOnTextChanged { text, start, count, after ->
            if(!text.isNullOrBlank()) {
                /* email not blank */
                if(!registerEnable[EMAIL]) registerEnable[EMAIL] = true
                if(registerEnable[NICKNAME] &&
                    registerEnable[FULL_NAME] &&
                    registerEnable[LOCATION] &&
                    registerEnable[PASSWORD] &&
                    !regBtn.isEnabled) {
                    /* other field not blank and button not enabled */
                    enableButton(regBtn)
                }
            } else {
                if(registerEnable[EMAIL]) {
                    registerEnable[EMAIL] = false
                    disableButton(regBtn)
                }
            }
        }

        au_location.doOnTextChanged { text, start, count, after ->
            if(!text.isNullOrBlank()) {
                /* location not blank */
                if(!registerEnable[LOCATION]) registerEnable[LOCATION] = true
                if(registerEnable[NICKNAME] &&
                    registerEnable[EMAIL] &&
                    registerEnable[FULL_NAME] &&
                    registerEnable[PASSWORD] &&
                    !regBtn.isEnabled) {
                    /* other field not blank and button not enabled */
                    enableButton(regBtn)
                }
            } else {
                if(registerEnable[LOCATION]) {
                    registerEnable[LOCATION] = false
                    disableButton(regBtn)
                }
            }
        }

        au_password.doOnTextChanged { text, start, count, after ->
            if(!text.isNullOrBlank()) {
                /* full name not blank */
                if(!registerEnable[PASSWORD]) registerEnable[PASSWORD] = true
                if(registerEnable[NICKNAME] &&
                    registerEnable[EMAIL] &&
                    registerEnable[LOCATION] &&
                    registerEnable[FULL_NAME] &&
                    !regBtn.isEnabled) {
                    /* other field not blank and button not enabled */
                    enableButton(regBtn)
                }
            } else {
                if(registerEnable[PASSWORD]) {
                    registerEnable[PASSWORD] = false
                    disableButton(regBtn)
                }
            }
        }

        regBtn.setOnClickListener {
            var dataInserted = true

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
                user.password = au_password.text.toString()
                authViewModel.registerUser(user)
            }

        }

        regToLog.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun enableButton(button: Button) {
        button.isEnabled = true
        button.background.setTint(ContextCompat.getColor(requireContext(), R.color.colorAccent))
    }

    private fun disableButton(button: Button) {
        button.isEnabled = false
        button.background.setTint(ContextCompat.getColor(requireContext(), R.color.colorAccentLight))
    }

}