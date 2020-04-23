package it.polito.mad.project.activities.main.ui.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import it.polito.mad.project.R
import it.polito.mad.project.enums.StoreFileKey
import it.polito.mad.project.models.User
import kotlinx.android.synthetic.main.activity_show_profile.*
import java.io.File

class EditProfileFragment : Fragment() {

    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        profileViewModel =
            ViewModelProvider(this).get(ProfileViewModel::class.java)
        loadUserFromStoreFile()
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.activity_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileViewModel.user.observe(this.viewLifecycleOwner, Observer{
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
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.option_menu, menu)
    }

    override fun onDetach() {
        super.onDetach()
        saveUserToStoreFile()
    }

    private fun loadUserFromStoreFile() {
        // Load store file of our app from shared preferences
        val sharedPreferences = this.activity?.getSharedPreferences(getString(R.string.app_store_file_name), Context.MODE_PRIVATE)

        // Load from the store file the user object. For the first time we load empty string.
        val userJson: String? = sharedPreferences?.getString(StoreFileKey.USER, "")

        if (userJson != null && userJson.isNotEmpty()) {
            // Assign the stored user to our view model if it is not empty
            profileViewModel.user.value = Gson().fromJson(userJson, User::class.java)
        }
    }

    private fun saveUserToStoreFile() {
        val sharedPref = this.activity?.getSharedPreferences(getString(R.string.app_store_file_name), Context.MODE_PRIVATE)
        val prefsEditor = sharedPref?.edit()
        prefsEditor?.putString(StoreFileKey.USER, Gson().toJson(profileViewModel));
        prefsEditor?.commit();
    }
}