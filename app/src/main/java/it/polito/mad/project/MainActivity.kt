package it.polito.mad.project

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import it.polito.mad.project.enums.StoreFileKey
import it.polito.mad.project.fragments.profile.UserViewModel
import it.polito.mad.project.models.User
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import org.w3c.dom.Text
import java.io.File


// POINT 2: Implement the MainActivity, controller of fragments and Navigation

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    private val userViewModel: UserViewModel = UserViewModel()
    private val gsonMapper: Gson = Gson()
    private var full_name: TextView? = null
    //private var location: TextView? = null
    private var user_photo: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        setNavView()

        val hView = navView.getHeaderView(0)
        full_name = hView.findViewById<TextView>(R.id.full_name)
        //location = hView.findViewById<TextView>(R.id.location)
        user_photo = hView.findViewById<ImageView>(R.id.user_photo)


        // Load store file of our app from shared preferences
        val sharedPreferences = this?.getSharedPreferences(getString(R.string.app_store_file_name), Context.MODE_PRIVATE)
        // Load from the store file the user object. For the first time we load empty string.
        val userJson: String? = sharedPreferences?.getString(StoreFileKey.USER, "")
        if (userJson != null && userJson.isNotEmpty()) {
            // Assign the stored user to our view model if it is not empty
            userViewModel.user.value = gsonMapper.fromJson(userJson, User::class.java)
        }
        // Observe the user changes
        userViewModel.user.observe(this, Observer {
           if (it!=null){
                if (full_name != null && !it.name.isNullOrEmpty())
                   full_name!!.text = it.name
              // if (location != null && !it.location.isNullOrEmpty())
                 //  location!!.text = it.location
               if (user_photo != null &&  !it.photoProfilePath.isNullOrEmpty()) {
                   if (File(it.photoProfilePath).isFile) {
                       val image: Bitmap = BitmapFactory.decodeFile(it.photoProfilePath)
                       if (image != null) user_photo!!.setImageBitmap(image)
                   }
               }
           }
        })

    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.main, menu)
//        return true
//    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.navMainHostFragment)

        // Load store file of our app from shared preferences
        val sharedPreferences = this?.getSharedPreferences(getString(R.string.app_store_file_name), Context.MODE_PRIVATE)
        // Load from the store file the user object. For the first time we load empty string.
        val userJson: String? = sharedPreferences?.getString(StoreFileKey.USER, "")
        if (userJson != null && userJson.isNotEmpty()) {
            // Assign the stored user to our view model if it is not empty
            userViewModel.user.value = gsonMapper.fromJson(userJson, User::class.java)
        }
        var user = userViewModel.user.value
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun setNavView() {
        val navController = findNavController(R.id.navMainHostFragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.showProfileFragment, R.id.navAdvertisements
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

}
