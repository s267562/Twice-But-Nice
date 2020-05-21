package it.polito.mad.project.fragments.authentication

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.messaging.FirebaseMessaging
import it.polito.mad.project.R
import it.polito.mad.project.fragments.profile.UserViewModel
import kotlinx.android.synthetic.main.fragment_login.*

class SignInFragment : Fragment() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var authViewModel: AuthViewModel
    private lateinit var userViewModel: UserViewModel

    private val rcSignIn: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity?)?.supportActionBar?.hide()
        authViewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
    }

    override fun onStart() {
        super.onStart()
        authViewModel.loggedIn.observe(viewLifecycleOwner, Observer {
            if (it) {
                if (!authViewModel.error)
                    enterInApp()
                else
                    Toast.makeText(context, authViewModel.errorMessage, Toast.LENGTH_LONG)
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        signInGoogleBtn.setOnClickListener {
            startActivityForResult(googleSignInClient.signInIntent, rcSignIn)
        }

        signInBtn.setOnClickListener {
            authViewModel.loginWithEmailPassword(log_email.text.toString(), log_password.text.toString())
        }

        logToReg.setOnClickListener {
            findNavController().navigate(R.id.action_navHome_to_signUpFragment)
        }
    }

    private fun enterInApp() {
        bindUserWithNavView()
        findNavController().navigate(R.id.action_navHome_to_onSaleListFragment)
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

    private fun bindUserWithNavView() {
        val navView = requireActivity().findViewById<NavigationView>(R.id.navView)
        val headerView = navView.getHeaderView(0)
        val fullName = headerView.findViewById<TextView>(R.id.full_name)
        val userPhoto = headerView.findViewById<ImageView>(R.id.user_photo)

        val userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        userViewModel.user.observe(requireActivity(), Observer{
            if (userViewModel.isAuthUser() && it != null) {
                FirebaseMessaging.getInstance().subscribeToTopic("/topics/${it.id}")

                if (it.name.isNotEmpty())
                    fullName.text = it.name
            }
        })

        userViewModel.userPhotoProfile.observe(requireActivity(), Observer {
            if (userViewModel.isAuthUser() && it != null) {
                userPhoto.setImageBitmap(it)
            }
        })
    }


    /*private fun signOut() {
        // Firebase sign out
        auth.signOut()
        // Google sign out
        googleSignInClient.signOut().addOnCompleteListener(requireActivity()) {
            updateUI(null)
        }
    }

    private fun revokeAccess() {
        // Firebase sign out
        auth.signOut()
        // Google revoke access
        googleSignInClient.revokeAccess().addOnCompleteListener(requireActivity()) {
            updateUI(null)
        }
    }*/
}