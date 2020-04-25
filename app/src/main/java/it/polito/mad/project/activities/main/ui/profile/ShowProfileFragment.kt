package it.polito.mad.project.activities.main.ui.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import it.polito.mad.project.R
import it.polito.mad.project.activities.main.ui.common.StoreFileFragment
import it.polito.mad.project.enums.StoreFileKey
import it.polito.mad.project.models.User
import kotlinx.android.synthetic.main.activity_show_profile.*
import java.io.File

class ShowProfileFragment : StoreFileFragment() {

    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        profileViewModel.user.value = loadFromStoreFile(StoreFileKey.USER, User::class.java)?:profileViewModel.user.value
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.activity_show_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (profileViewModel.user == null){
            Log.d("errore", "errore")
        }

        profileViewModel.user.observe(this.viewLifecycleOwner, Observer{
            if (it != null) {
                if (it.name != null && it.name.isNotEmpty())
                    full_name.text = it.name
                if (it.nickname != null && it.nickname.isNotEmpty())
                    nickname.text = it.nickname
                if (it.email != null && it.email.isNotEmpty())
                    email.text = it.email
                if (it.location != null && it.location.isNotEmpty())
                    location.text = it.location
                if (it.photoProfilePath != null && it.photoProfilePath.isNotEmpty()) {
                    if (File(it.photoProfilePath).isFile){
                        val image: Bitmap = BitmapFactory.decodeFile(it.photoProfilePath)
                        if (image != null) user_photo.setImageBitmap(image)
                    }
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.edit_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.pencil_option -> {
                saveToStoreFile(StoreFileKey.USER, profileViewModel.user.value)
                this.findNavController().navigate(R.id.action_showProfileFragment_to_editProfileFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        saveToStoreFile(StoreFileKey.USER, profileViewModel.user.value)
    }
}