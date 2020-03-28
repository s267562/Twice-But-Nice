package it.polito.mad.mad_project

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_edit_profile.*

class EditProfileActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        registerForContextMenu(camera_icon)
        // Load user information
        val user: User? = savedInstanceState?.getByteArray(IntentRequest.UserData.NAME) as? User
        if (user != null) {
            full_name.setText("${user.name} ${user.surname}")
            nickname.setText(user.nickname)
            email.setText(user.email)
            location.setText(user.location)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.edit_profile_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.save_option -> {
                Toast.makeText(this, "save clicked", Toast.LENGTH_SHORT).show()
                saveProfile()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu.setHeaderTitle("Context Menu")
        menu.add(0, v.getId(), 0, "Upload")
        menu.add(0, v.getId(), 0, "Search")
        menu.add(0, v.getId(), 0, "Share")
        menu.add(0, v.getId(), 0, "Bookmark")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        Toast.makeText(this, "Selected Item: " + item.title, Toast.LENGTH_SHORT)
        return true
    }

    private fun saveProfile() {
        val user = User("")
        user.name = full_name.text.toString()
        user.nickname = nickname.text.toString()
        user.email = email.text.toString()
        user.location = location.text.toString()

        val intent = Intent()
        intent.putExtra(IntentRequest.UserData.NAME, user)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }


}
