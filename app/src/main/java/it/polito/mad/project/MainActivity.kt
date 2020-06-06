package it.polito.mad.project

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.messaging.FirebaseMessaging
import it.polito.mad.project.fragments.advertisements.OnSaleListFragment
import it.polito.mad.project.viewmodels.AuthViewModel
import it.polito.mad.project.viewmodels.UserViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var authViewModel: AuthViewModel
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        setNavView()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()
        FirebaseFirestore.getInstance().firestoreSettings = settings

        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        authViewModel.loggedIn.observe(this, Observer {
            if (it) {
                FirebaseMessaging.getInstance()
                    .subscribeToTopic("/topics/${authViewModel.getAuthUserId()}")
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
                bindUserWithNavView()
            }
        })

        authViewModel.loggedOut.observe(this, Observer {
            if (it) {
                authViewModel.loggedOut.value = false
                FirebaseMessaging.getInstance()
                    .unsubscribeFromTopic("/topics/${authViewModel.getAuthUserId()}")
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                finish()
                startActivity(intent)
            }
        })

        authViewModel.loader.observe(this, Observer {
            if (authViewModel.isNotLoading()) {
                loadingLayout.visibility = View.GONE
            } else {
                loadingLayout.visibility = View.VISIBLE
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.navMainHostFragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val fragment: Fragment = navMainHostFragment.childFragmentManager.fragments[0]
        if (fragment is OnSaleListFragment)
            finish()
    }


    private fun setNavView() {
        val navController = findNavController(R.id.navMainHostFragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.showProfileFragment,
                R.id.itemListFragment,
                R.id.onSaleListFragment,
                R.id.itemsOfInterestListFragment,
                R.id.boughtItemsListFragment
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private fun bindUserWithNavView() {
        val headerView = navView.getHeaderView(0)
        val fullName = headerView.findViewById<TextView>(R.id.full_name)
        val userPhoto = headerView.findViewById<ImageView>(R.id.user_photo)

        userViewModel.user.data.observe(this, Observer {
            if (it != null && authViewModel.getAuthUserId() == it.id) {
                if (it.name.isNotEmpty())
                    fullName.text = it.name
            }
        })
        userViewModel.user.image.observe(this, Observer {
            if (it != null && authViewModel.getAuthUserId() == userViewModel.getUserId()) {
                userPhoto.setImageBitmap(it)
            }
        })
    }
}
