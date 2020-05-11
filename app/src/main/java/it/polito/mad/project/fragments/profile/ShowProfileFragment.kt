package it.polito.mad.project.fragments.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import it.polito.mad.project.R
import it.polito.mad.project.enums.StoreFileKey
import it.polito.mad.project.fragments.common.StoreFileFragment
import it.polito.mad.project.models.User
import kotlinx.android.synthetic.main.fragment_show_profile.*
import java.io.File


class ShowProfileFragment : StoreFileFragment() {

    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        profileViewModel.user.value = loadFromStoreFile(StoreFileKey.USER, User::class.java)
        setHasOptionsMenu(true)

        return inflater.inflate(R.layout.fragment_show_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        Log.d("ShowProf - onDestroy", profileViewModel.user.value.toString())
        //saveToStoreFile(StoreFileKey.USER, profileViewModel.user.value)
    }

}