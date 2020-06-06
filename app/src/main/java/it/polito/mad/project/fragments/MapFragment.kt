package it.polito.mad.project.fragments

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import it.polito.mad.project.R
import it.polito.mad.project.customViews.CustomMapView
import it.polito.mad.project.viewmodels.ItemViewModel
import it.polito.mad.project.viewmodels.UserViewModel
import kotlinx.android.synthetic.main.fragment_map.*
import java.io.IOException

class MapFragment : Fragment(), OnMapReadyCallback, SearchView.OnQueryTextListener {

    private lateinit var map: GoogleMap
    private lateinit var itemViewModel: ItemViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var itemLocation: String
    private lateinit var userLocation: String

    private var googleApiClient: GoogleApiClient? = null
    private val REQUEST_LOCATION = 199
    lateinit var searchEditText: TextView
    private lateinit var geocoder: Geocoder
    private lateinit var markerOptions: MarkerOptions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemViewModel = ViewModelProvider(requireActivity()).get(ItemViewModel::class.java)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)

        geocoder = Geocoder(requireActivity())
        markerOptions = MarkerOptions()
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
        setHasOptionsMenu(true)

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapRoute = activity?.findViewById<CustomMapView>(R.id.mapRoute)

        if(mapRoute != null) {
            mapRoute.onCreate(null)
            mapRoute.onResume()
            mapRoute.getMapAsync(this)
        }

        myPositionFAB.setOnClickListener {
            enableLocation()
        }

        saveFAB.setOnClickListener {
            Toast.makeText(requireContext(), markerOptions.title, Toast.LENGTH_SHORT).show() 
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.map_search_menu, menu)

        val itemMenu: MenuItem = menu.findItem(R.id.menu_search)

        val searchView = itemMenu.actionView as SearchView

        searchEditText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.hint = "Enter location..."

        searchView.setOnQueryTextListener(this)
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {

        if(query != "") {
            val address: Address? = getAddress(query)
            if(address != null) {
                setMarker(
                    LatLng(address.latitude, address.longitude),
                    query ?: ""
                )
            } else {
                Toast.makeText(requireContext(), "Location not found, try again", Toast.LENGTH_SHORT).show()
            }
        }


        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            REQUEST_LOCATION -> {
                if(resultCode == RESULT_OK)
                    Log.d("MapFragment-DEBUG", "resultCode $resultCode")
            }
        }

    }

    override fun onMapReady(gMap: GoogleMap) {
        gMap.uiSettings?.isZoomControlsEnabled = false
        gMap.uiSettings?.isMapToolbarEnabled = true
        gMap.uiSettings?.isMyLocationButtonEnabled = true
        gMap.uiSettings?.isCompassEnabled = true

        gMap.setOnMapClickListener {
            val clickPosition = LatLng(it.latitude, it.longitude)
            setMarker(clickPosition, "TODO marker title")
        }

        map = gMap
    }

    private fun enableLocation() {

        if (googleApiClient == null) {
            googleApiClient = GoogleApiClient.Builder(requireActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                    override fun onConnected(bundle: Bundle?) {}
                    override fun onConnectionSuspended(i: Int) {
                        googleApiClient!!.connect()
                    }
                })
                .addOnConnectionFailedListener { connectionResult ->
                    Log.d("Location error", "Location error " + connectionResult.errorCode)
                }
                .build()
            googleApiClient!!.connect()
        }

        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 30 * 1000.toLong()
        locationRequest.fastestInterval = 5 * 1000.toLong()
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val result: PendingResult<LocationSettingsResult> =
            LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
        result.setResultCallback { result ->
            val status: Status = result.status
            when (status.statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                    /* GPS not enabled
                     * Show the dialog by calling startResolutionForResult(),
                     * and check the result in onActivityResult(): REQUEST_LOCATION
                     */
                    startIntentSenderForResult(
                        status.resolution.intentSender,
                        REQUEST_LOCATION,
                        null, 0, 0, 0, null)
                } catch (e: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
                LocationSettingsStatusCodes.SUCCESS -> {
                    Log.d("MapFragment-DEBUG", "LocationSettingsStatusCodes.SUCCESS")
                    /* GPS enabled */
                }
            }
        }
    }

    private fun getAddress(location: String?): Address? {
        val address: Address? = null
        var addressList: List<Address>

        if(location != "") {
            try {
                addressList = geocoder.getFromLocationName(location, 1)
                if(addressList.isNotEmpty()) {
                    return addressList[0]
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }

    private fun setMarker(latLng: LatLng, title: String) {
        markerOptions.position(latLng).title(title)
        map.clear()
        map.addMarker(markerOptions)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0F))
    }

}
