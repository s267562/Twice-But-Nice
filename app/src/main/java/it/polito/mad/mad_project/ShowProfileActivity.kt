package it.polito.mad.mad_project

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_show_profile.*

class ShowProfileActivity : AppCompatActivity() {
    private val userViewModel: UserViewModel = UserViewModel()
    private val gsonMapper: Gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_profile)
        val sharedPref = this?.getSharedPreferences(getString(R.string.app_store_file), Context.MODE_PRIVATE)

        val json: String? = sharedPref?.getString("UserObject", "")
        val obj: User = gsonMapper.fromJson<User>(json, User::class.java)

        userViewModel.user.observe(this, Observer{
            Log.d("MAD_LOG", "OBSERVED-USER: $it")
            full_name.text = it.name
            nickname.text = it.nickname
            email.text = it.email
            location.text = it.location
        })
        userViewModel.user.value = obj
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
            Log.d ("MAD_LOG", "RESULT-USER: $user")
            userViewModel.user.value = user

            val sharedPref = this?.getSharedPreferences(getString(R.string.app_store_file), Context.MODE_PRIVATE)
            val prefsEditor = sharedPref?.edit()
            prefsEditor?.putString("UserObject", gsonMapper.toJson(user));
            prefsEditor?.commit();
        }
    }

    private fun editProfile() {
        val intent = Intent(this, EditProfileActivity::class.java)
        Log.d ("MAD_LOG", "SEND-USER: ${userViewModel.user.value}")

        intent.putExtra(IntentRequest.UserData.NAME, userViewModel.user.value)
        startActivityForResult(intent, IntentRequest.UserData.CODE)
    }

}
