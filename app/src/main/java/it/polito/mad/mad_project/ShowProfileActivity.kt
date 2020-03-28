package it.polito.mad.mad_project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_show_profile.*

class ShowProfileActivity : AppCompatActivity() {
    private val userViewModel: UserViewModel = UserViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_profile)

        userViewModel.user.observe(this, Observer{
            full_name.text = "${it.name} ${it.surname}"
            nickname.text = "${it.nickname}"
            email.text = "${it.email}"
            location.text = "${it.location}"
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
            val user = (data!!.getByteArrayExtra(IntentRequest.UserData.NAME) as? User)
            Log.d ("MAD_LOG", "USER: $user")
        }
    }

    private fun editProfile() {
        val intent = Intent(this, EditProfileActivity::class.java)
        startActivityForResult(intent, IntentRequest.UserData.CODE)
    }

}
