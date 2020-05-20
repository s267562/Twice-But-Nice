package it.polito.mad.project

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.inappmessaging.FirebaseInAppMessaging
import com.google.firebase.inappmessaging.ktx.inAppMessaging
import com.google.firebase.ktx.Firebase
import it.polito.mad.project.fragments.profile.UserViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import java.io.File


class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private var userAuth = FirebaseAuth.getInstance()

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var firebaseIam: FirebaseInAppMessaging

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        setNavView()
        if(userAuth.currentUser != null){
            bindUserWithNavView()
        }
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        firebaseIam = Firebase.inAppMessaging

        firebaseIam.isAutomaticDataCollectionEnabled = true
        firebaseIam.setMessagesSuppressed(false)


        // Get and display/log the Instance ID
        FirebaseInstanceId.getInstance().instanceId
            .addOnSuccessListener { instanceIdResult ->
                val instanceId = instanceIdResult.id
                Log.d(" ", "InstanceId: $instanceId")
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.navMainHostFragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    private fun setNavView() {
        val navController = findNavController(R.id.navMainHostFragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.showProfileFragment,
                R.id.itemListFragment,
                R.id.onSaleListFragment
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private fun bindUserWithNavView() {
        val headerView = navView.getHeaderView(0)
        val fullName = headerView.findViewById<TextView>(R.id.full_name)
        val userPhoto = headerView.findViewById<ImageView>(R.id.user_photo)

        val userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        userViewModel.user.observe(this, Observer{
            if (it != null) {
                if (it.name.isNotEmpty())
                    fullName.text = it.name
            }
        })
        userViewModel.userPhotoProfile.observe(this, Observer {
            if (it != null) {
                userPhoto.setImageBitmap(it)
            }
        })
    }
}
