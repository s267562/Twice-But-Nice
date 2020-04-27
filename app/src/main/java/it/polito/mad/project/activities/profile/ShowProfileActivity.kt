package it.polito.mad.project.activities.profile

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.gson.Gson
import it.polito.mad.project.*
import it.polito.mad.project.enums.IntentRequest
import it.polito.mad.project.enums.StoreFileKey
import it.polito.mad.project.models.User
import it.polito.mad.project.activities.profile.ui.UserViewModel
import kotlinx.android.synthetic.main.fragment_show_profile.*
import java.io.File

class ShowProfileActivity : AppCompatActivity() {
    private val userViewModel: UserViewModel = UserViewModel()
    private val gsonMapper: Gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_show_profile)

        supportActionBar!!.elevation = 0f

        // Load store file of our app from shared preferences
        val sharedPreferences = this?.getSharedPreferences(getString(R.string.app_store_file_name), Context.MODE_PRIVATE)

        // Load from the store file the user object. For the first time we load empty string.
        val userJson: String? = sharedPreferences?.getString(StoreFileKey.USER, "")

        if (userJson != null && userJson.isNotEmpty()) {
            // Assign the stored user to our view model if it is not empty
            userViewModel.user.value = gsonMapper.fromJson(userJson, User::class.java)
        }

        // Observe the user changes
        userViewModel.user.observe(this, Observer{
            if (it.name.isNotEmpty())
                full_name.text = it.name
            if (it.nickname.isNotEmpty())
                nickname.text = it.nickname
            if (it.email.isNotEmpty())
                email.text = it.email
            if (it.location.isNotEmpty())
                location.text = it.location
            if (it.photoProfilePath != null && it.photoProfilePath.isNotEmpty()) {
                if (File(it.photoProfilePath).isFile){
                    val image: Bitmap = BitmapFactory.decodeFile(it.photoProfilePath)
                    if (image != null) user_photo.setImageBitmap(image)
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.edit_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.pencil_option -> {
                //displayMessage(baseContext, "pencil clicked")
                editProfile()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IntentRequest.UserData.CODE) {
            val user = data?.getSerializableExtra(IntentRequest.UserData.NAME) as User?
            if (user != null) {
                val sharedPref = this?.getSharedPreferences(getString(R.string.app_store_file_name), Context.MODE_PRIVATE)
                val prefsEditor = sharedPref?.edit()
                prefsEditor?.putString(StoreFileKey.USER, gsonMapper.toJson(user));
                prefsEditor?.commit();
                userViewModel.user.value = user
            }
        }
    }

    private fun editProfile() {
        val intent = Intent(this, EditProfileActivity::class.java)
        intent.putExtra(IntentRequest.UserData.NAME, userViewModel.user.value)
        startActivityForResult(intent,
            IntentRequest.UserData.CODE
        )
    }

}
