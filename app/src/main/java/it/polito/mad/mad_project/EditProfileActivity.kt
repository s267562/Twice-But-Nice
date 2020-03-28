package it.polito.mad.mad_project

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import android.widget.Toast

class EditProfileActivity : AppCompatActivity() {

    val cameraicon: ImageButton = findViewById(R.id.camera_icon)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        registerForContextMenu(cameraicon)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.option_menu, menu)
        return true
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

}
