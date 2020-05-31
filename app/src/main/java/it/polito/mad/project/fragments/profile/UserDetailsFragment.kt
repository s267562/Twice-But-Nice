package it.polito.mad.project.fragments.profile

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import it.polito.mad.project.R
import androidx.fragment.app.Fragment
import it.polito.mad.project.viewmodels.AuthViewModel
import it.polito.mad.project.viewmodels.UserViewModel
import kotlinx.android.synthetic.main.fragment_show_profile.*


class UserDetailsFragment : Fragment() {

    private lateinit var userViewModel: UserViewModel
    private lateinit var authViewModel: AuthViewModel

    private var isAuthUser = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        authViewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
    }

    override fun onStart() {
        super.onStart()
        isAuthUser = arguments?.getBoolean("IsAuthUser")?:isAuthUser

        userViewModel.user.data.observe(viewLifecycleOwner, Observer{
            if (it != null) {
                if (it.name.isNotEmpty())
                    full_name.text = it.name
                if (it.nickname.isNotEmpty())
                    nickname.text = it.nickname
                if (it.email.isNotEmpty())
                    email.text = it.email
                if (it.location.isNotEmpty())
                    location.text = it.location
            }
        })

        userViewModel.user.image.observe(this, Observer {
            if (it != null) {
                user_photo.setImageBitmap(it)
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

        if (isAuthUser) {
            logoutFab.show()
        } else {
            logoutFab.hide()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_show_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setFabButton()
        userViewModel.loadUser(arguments?.getString("UserId"))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if (isAuthUser){
            name_container.visibility = View.VISIBLE
            full_name.visibility = View.VISIBLE
            inflater.inflate(R.menu.edit_menu, menu)
        }
        else{
            name_container.visibility = View.GONE
            full_name.visibility = View.GONE
        }
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
        if (!isAuthUser)
            userViewModel.resetUser()
    }

    private fun setFabButton() {
        logoutFab.setOnClickListener {
            authViewModel.logout()
            findNavController().popBackStack()
        }
    }

}