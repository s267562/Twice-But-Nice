package it.polito.mad.project.fragments.profile

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import it.polito.mad.project.R
import androidx.recyclerview.widget.LinearLayoutManager
import it.polito.mad.project.viewmodels.AuthViewModel
import it.polito.mad.project.viewmodels.ItemViewModel
import it.polito.mad.project.viewmodels.UserViewModel
import kotlinx.android.synthetic.main.fragment_on_sale_list.*
import kotlinx.android.synthetic.main.fragment_show_profile.*
import kotlinx.android.synthetic.main.fragment_show_profile.loadingLayout
import java.io.IOException
import java.util.*

class UserDetailsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var itemViewModel: ItemViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var authViewModel: AuthViewModel

    private lateinit var googleMap: GoogleMap

    private var isAuthUser = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemViewModel = ViewModelProvider(activity?:this).get(ItemViewModel::class.java)
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

        itemViewModel.loader.observe(viewLifecycleOwner, Observer {
            if (itemViewModel.isNotLoading()) {
                // loader ended
                itemViewModel.reviews.adapter.setItemReviews(itemViewModel.reviews.items)
            }
        })

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_show_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setFabButton()

        val mapView = activity?.findViewById<MapView>(R.id.mapViewProfile)

        if(mapView != null) {
            mapView.onCreate(null)
            mapView.onResume()
            mapView.getMapAsync(this)
        }

        userViewModel.loadUser(arguments?.getString("UserId"))

        reviewRecyclerView.setHasFixedSize(true)
        reviewRecyclerView.layoutManager = LinearLayoutManager(this.activity)
        reviewRecyclerView.adapter = itemViewModel.reviews.adapter

        itemViewModel.loadReviews(arguments?.getString("UserId"))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if (isAuthUser){
            name_container.visibility = View.VISIBLE
            full_name.visibility = View.VISIBLE
            inflater.inflate(R.menu.edit_menu, menu)
        }
        else {
            name_container.visibility = View.GONE
            full_name.visibility = View.GONE
            mapViewProfile.visibility = View.GONE
            location.visibility = View.VISIBLE
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

    override fun onMapReady(gMap: GoogleMap?) {
        gMap?.let {
            googleMap = it
        }

        var geocode = Geocoder(context?.applicationContext, Locale.getDefault())

        gMap?.uiSettings?.isZoomControlsEnabled = true
        gMap?.uiSettings?.isMapToolbarEnabled = true
        gMap?.uiSettings?.isMyLocationButtonEnabled = true
        gMap?.uiSettings?.isCompassEnabled = true

        try {
            var addr = geocode.getFromLocationName(location.text.toString(), 1)
            if(addr.size > 0){
                var address : Address = addr.get(0)
                val cameraPos = LatLng(address.latitude, address.longitude)
                gMap?.addMarker(
                    MarkerOptions()
                        .position(LatLng(address.latitude, address.longitude))
                        .title("User Current Location")
                )
                gMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(cameraPos, 7.5F))
            }
        } catch (e: IOException){
            e.printStackTrace()
        }
    }
}
