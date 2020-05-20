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
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.mad.project.R
import it.polito.mad.project.fragments.profile.UserViewModel
import it.polito.mad.project.models.User
import kotlinx.android.synthetic.main.fragment_login.*

class SignInFragment : Fragment() {
    private lateinit var firebaseStore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var userViewModel: UserViewModel

    private val rcSignIn: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity?)?.supportActionBar?.hide()
    }

    override fun onStart() {
        super.onStart()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        firebaseStore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        updateUI(firebaseAuth.currentUser)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        signInGoogleBtn.setOnClickListener {
            Toast.makeText(activity, "Button clicked", Toast.LENGTH_SHORT).show()
            signInWithGoogle()
        }

        signInBtn.setOnClickListener {
            signInWithEmail()
        }

        logToReg.setOnClickListener {
            findNavController().navigate(R.id.action_navHome_to_signUpFragment)
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, rcSignIn)
    }

    private fun signInWithEmail() {
        firebaseAuth.signInWithEmailAndPassword(log_email.text.toString(), log_password.text.toString()).addOnCompleteListener{ it ->
            if (it.isSuccessful) {
                updateUI(it.result?.user)
            } else {
                Toast.makeText(activity, it.exception?.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
            bindUserWithNavView()
            findNavController().navigate(R.id.action_navHome_to_onSaleListFragment)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == rcSignIn) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                // [START_EXCLUDE]
                updateUI(null)
                // [END_EXCLUDE]
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(requireActivity()) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "signInWithCredential:success")
                var firebaseUser = firebaseAuth.currentUser
                var user = User(firebaseUser!!.displayName?:"")
                user.id = firebaseUser!!.uid
                user.email = firebaseUser!!.email?:""

                var doc = firebaseStore.collection("users").document(user.id)
                doc.get().addOnCompleteListener{
                    task ->  if(task.result?.exists()!!){
                        updateUI(firebaseUser)
                    }
                    else{
                        doc.set(user).addOnCompleteListener{
                        updateUI(firebaseUser)
                        }
                }
                }

            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "signInWithCredential:failure", task.exception)
                // [START_EXCLUDE]
                //val view = binding.mainLayout
                // [END_EXCLUDE]
                view?.let { Snackbar.make(it, "Authentication Failed.", Snackbar.LENGTH_SHORT).show() }
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
            if (it != null) {
                if (it.name.isNotEmpty())
                    fullName.text = it.name
            }
        })

        userViewModel.userPhotoProfile.observe(requireActivity(), Observer {
            if (it != null) {
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