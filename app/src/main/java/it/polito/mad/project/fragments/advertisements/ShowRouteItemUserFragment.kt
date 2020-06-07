package it.polito.mad.project.fragments.advertisements

import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

import it.polito.mad.project.R
import it.polito.mad.project.customViews.CustomMapView
import it.polito.mad.project.viewmodels.ItemViewModel
import it.polito.mad.project.viewmodels.UserViewModel
import java.io.IOException
import java.util.*

class ShowRouteItemUserFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var itemViewModel: ItemViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var itemLocation: String
    private lateinit var userLocation: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemViewModel = ViewModelProvider(requireActivity()).get(ItemViewModel::class.java)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
    }

    override fun onStart() {
        super.onStart()
        itemViewModel.item.data.observe(viewLifecycleOwner, Observer {
            if(it != null){
                itemLocation = it.location
            }
        })
        userViewModel.user.data.observe(viewLifecycleOwner, Observer {
            if(it != null){
                userLocation = it.location
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_show_route_item_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapRoute = activity?.findViewById<CustomMapView>(R.id.mapRoute)

        if(mapRoute != null) {
            mapRoute.onCreate(null)
            mapRoute.onResume()
            mapRoute.getMapAsync(this)
        }
    }

    override fun onMapReady(gMap: GoogleMap?) {
        if(context != null) {
            gMap?.let {
                googleMap = it
            }

            gMap?.uiSettings?.isZoomControlsEnabled = true
            gMap?.uiSettings?.isMapToolbarEnabled = true
            gMap?.uiSettings?.isMyLocationButtonEnabled = true
            gMap?.uiSettings?.isCompassEnabled = true

            var geocode = Geocoder(context?.applicationContext, Locale.getDefault())

            try {
                var addrItem = geocode.getFromLocationName(itemLocation, 1)
                var addrUser = geocode.getFromLocationName(userLocation, 1)

                if (addrItem.size > 0 && addrUser.size > 0){
                    var addressItem : Address = addrItem.get(0)
                    var addressUser : Address = addrUser.get(0)
                    val cameraPos = LatLng(addressItem.latitude, addressItem.longitude)
                    //Toast.makeText(context, "ROUTE: " + itemLocation.toUpperCase() + " " + userLocation.toUpperCase(), Toast.LENGTH_SHORT).show()
                    gMap?.addMarker(
                        MarkerOptions().position(LatLng(addressItem.latitude, addressItem.longitude))
                            .title("Item position")
                    )
                    gMap?.addMarker(
                        MarkerOptions().position(LatLng(addressUser.latitude, addressUser.longitude))
                            .title("User position")
                    )
                    gMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(cameraPos, 12F))

                    val route = gMap?.addPolyline(
                        PolylineOptions().add(
                            LatLng(addressItem.latitude, addressItem.longitude),
                            LatLng(addressUser.latitude, addressUser.longitude)
                        ).width(4F).color(Color.BLUE).geodesic(true)
                    )
                }

            } catch (e: IOException){
                e.printStackTrace()
            }
        }
    }
}
