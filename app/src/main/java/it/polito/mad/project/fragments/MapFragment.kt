package it.polito.mad.project.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.*
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import it.polito.mad.project.R
import it.polito.mad.project.customViews.CustomMapView
import it.polito.mad.project.viewmodels.MapViewModel
import kotlinx.android.synthetic.main.fragment_map.*
import java.io.IOException


class MapFragment : Fragment(), OnMapReadyCallback, SearchView.OnQueryTextListener{

    private val permissionId = 199
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var mapViewModel: MapViewModel
    private lateinit var geocoder: Geocoder
    private lateinit var markerOptions: MarkerOptions
    private lateinit var map: GoogleMap
    private lateinit var searchEditText: TextView

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            setMarker(LatLng(mLastLocation.latitude, mLastLocation.longitude))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        mapViewModel = ViewModelProvider(requireActivity()).get(MapViewModel::class.java)

        geocoder = Geocoder(requireActivity())
        markerOptions = MarkerOptions()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    @SuppressLint("MissingPermission")
    private fun getLocation(location: String? = null) {
        if (location?.isNotBlank() == true) {
            setMarkerFormLocationName(location)
        } else {
            mFusedLocationClient.lastLocation.addOnCompleteListener { task ->
                val result = task.result
                if (result == null) {
                    requestNewLocationData()
                } else {
                    setMarker(LatLng(result.latitude, result.longitude))
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
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
            if (checkPermissions())
                if (isLocationEnabled())
                    getLocation()
                else enableLocation()
            else requestPermissions()
        }

        saveFAB.setOnClickListener {
            if(markerOptions.position != null) {
                val address = getAddressFromLocation(markerOptions.position.latitude, markerOptions.position.longitude)
                if (address != null) {
                    mapViewModel.location = "${address.postalCode} ${address.locality}, ${address.countryName}"
                    mapViewModel.updateLocation.value = true
                }
            }
            findNavController().popBackStack()
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                if (isLocationEnabled())
                    getLocation()
                else enableLocation()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.uiSettings?.isZoomControlsEnabled = false
        googleMap.uiSettings?.isMapToolbarEnabled = true
        googleMap.uiSettings?.isMyLocationButtonEnabled = true
        googleMap.uiSettings?.isCompassEnabled = true

        googleMap.setOnMapLongClickListener {
            val clickPosition = LatLng(it.latitude, it.longitude)
            setMarker(clickPosition)
        }

        map = googleMap
        if (checkPermissions() && isLocationEnabled()) {
            getLocation(mapViewModel.location)
        }
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if(query != "") {
            setMarkerFormLocationName(query)
        }
        return false
    }

    private fun enableLocation() {
        AlertDialog.Builder(requireContext())
            .setTitle("GPS location required")
            .setMessage("You have to enable GPS to use this feature")
            .setPositiveButton("ok") { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            permissionId
        )
    }

    private fun setMarkerFormLocationName(location: String?) {
        val address: Address? = getAddressFromLocationName(location)
        if(address != null) {
            setMarker(LatLng(address.latitude, address.longitude))
        } else {
            Toast.makeText(requireContext(), "Location not found, try again", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getAddressFromLocationName(location: String?): Address? {
        val addressList: List<Address>

        if(location != "") {
            try {
                addressList = geocoder.getFromLocationName(location, 10)
                if(addressList.isNotEmpty()) {
                    return addressList[0]
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }

    private fun getAddressFromLocation(lat: Double, lon: Double): Address? {
        val addressList: List<Address>
        try {
            addressList = geocoder.getFromLocation(lat, lon, 1)
            if(addressList.isNotEmpty()) {
                return addressList[0]
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun setMarker(latLng: LatLng) {
        markerOptions.position(latLng)
        map.clear()
        map.addMarker(markerOptions)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0F))
    }
}
