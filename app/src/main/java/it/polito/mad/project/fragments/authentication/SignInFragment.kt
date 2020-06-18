package it.polito.mad.project.fragments.authentication

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import it.polito.mad.project.R
import it.polito.mad.project.utils.Util.Companion.hideKeyboard
import it.polito.mad.project.viewmodels.AuthViewModel
import kotlinx.android.synthetic.main.fragment_login.*

class SignInFragment : Fragment() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var authViewModel: AuthViewModel

    private var signiInEnable: BooleanArray = booleanArrayOf(false, false)
    private val EMAIL = 0
    private val PASSWORD = 1

    private val rcSignIn: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity?)?.supportActionBar?.hide()
        authViewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
    }

    override fun onStart() {
        super.onStart()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        authViewModel.loggedIn.observe(viewLifecycleOwner, Observer {
            if (authViewModel.error) {
                Toast.makeText(context, authViewModel.errorMessage, Toast.LENGTH_LONG).show()
                authViewModel.error = false
            }
            else {
                if (it) {
                    findNavController().navigate(R.id.onSaleListFragment)
                } else {
                    (activity as AppCompatActivity?)?.supportActionBar?.hide()
                    googleSignInClient.signOut()
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        log_email.doOnTextChanged { text, _, _, _ ->
            if(!text.isNullOrBlank()) {
                /* email not blank */
                if(!signiInEnable[EMAIL]) signiInEnable[EMAIL] = true
                if(signiInEnable[PASSWORD] && !signInBtn.isEnabled) {
                    /*, pass not blank and button not enabled */
                    enableButton(signInBtn)
                }
            } else {
                if(signiInEnable[EMAIL]) {
                    signiInEnable[EMAIL] = false
                    disableButton(signInBtn)
                }
            }
        }

        log_password.doOnTextChanged { text, _, _, _ ->
            if(!text.isNullOrBlank()) {
                /* password not blank */
                if(!signiInEnable[PASSWORD]) signiInEnable[PASSWORD] = true
                if(signiInEnable[EMAIL] && !signInBtn.isEnabled) {
                    /*, email not blank and button not enabled */
                    enableButton(signInBtn)
                }
            } else {
                if(signiInEnable[PASSWORD]) {
                    signiInEnable[PASSWORD] = false
                    disableButton(signInBtn)
                }
            }
        }

        signInGoogleBtn.setOnClickListener {
            startActivityForResult(googleSignInClient.signInIntent, rcSignIn)
        }


        signInBtn.setOnClickListener {
            authViewModel.loginWithEmailPassword(log_email.text.toString(), log_password.text.toString())
        }

        signup_button.setOnClickListener {
            findNavController().navigate(R.id.signUpFragment)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == rcSignIn) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "Try login with google, account id: " + account.id)
                val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
                authViewModel.loginWithCredential(credential)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        hideKeyboard(activity)
    }
}