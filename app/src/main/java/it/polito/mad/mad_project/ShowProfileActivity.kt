package it.polito.mad.mad_project

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_show_profile.*
import java.io.File

class ShowProfileActivity : AppCompatActivity() {
    private val userViewModel: UserViewModel = UserViewModel()
    private val gsonMapper: Gson = Gson()
    private var openedPhotoProfile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_profile)

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
            if (it.photoFilename != null && it.photoFilename.isNotEmpty())
                user_photo.setImageBitmap(BitmapFactory.decodeFile(it.photoFilename))
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.option_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.pencil_option -> {
                Toast.makeText(this, "pencil clicked", Toast.LENGTH_SHORT).show()
                editProfile()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IntentRequest.UserData.CODE) {
            val user = data!!.getSerializableExtra(IntentRequest.UserData.NAME) as User
            userViewModel.user.value = user

            val sharedPref = this?.getSharedPreferences(getString(R.string.app_store_file_name), Context.MODE_PRIVATE)
            val prefsEditor = sharedPref?.edit()
            prefsEditor?.putString(StoreFileKey.USER, gsonMapper.toJson(user));
            prefsEditor?.commit();
        }
    }

    private fun editProfile() {
        val intent = Intent(this, EditProfileActivity::class.java)
        intent.putExtra(IntentRequest.UserData.NAME, userViewModel.user.value)
        startActivityForResult(intent, IntentRequest.UserData.CODE)
    }

}
