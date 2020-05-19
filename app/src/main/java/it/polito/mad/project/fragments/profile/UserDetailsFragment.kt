package it.polito.mad.project.fragments.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import it.polito.mad.project.R
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_show_profile.*
import java.io.File


class UserDetailsFragment : Fragment() {

    private lateinit var userViewModel: UserViewModel
    private var isAuthUser = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userViewModel = ViewModelProvider(activity?:this).get(UserViewModel::class.java)
    }

    override fun onStart() {
        super.onStart()
        isAuthUser = arguments?.getBoolean("IsAuthUser")?:isAuthUser

        userViewModel.user.observe(viewLifecycleOwner, Observer{
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

        userViewModel.loader.observe(viewLifecycleOwner, Observer {
            if (userViewModel.isNotLoading()) {
                loadingLayout.visibility = View.GONE
                if (userViewModel.error) {
                    Toast.makeText(context, "Error on item loading", Toast.LENGTH_SHORT).show()
                }
            } else {
                loadingLayout.visibility = View.VISIBLE
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_show_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userViewModel.loadUser(arguments?.getString("UserId"))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if (isAuthUser)
            inflater.inflate(R.menu.edit_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.pencil_option -> {
                this.findNavController().navigate(R.id.action_showProfileFragment_to_editProfileFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("ShowProf - onDestroy", userViewModel.user.value.toString())
    }

}