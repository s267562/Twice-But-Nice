package it.polito.mad.project.fragments.advertisements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback

import it.polito.mad.project.R
import it.polito.mad.project.viewmodels.ItemViewModel
import it.polito.mad.project.viewmodels.UserViewModel

class ShowRouteItemUserFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var itemViewModel: ItemViewModel
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemViewModel = ViewModelProvider(requireActivity()).get(ItemViewModel::class.java)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_show_route_item_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapRoute = activity?.findViewById<MapView>(R.id.mapRoute)

        if(mapRoute != null) {
            mapRoute.onCreate(null)
            mapRoute.onResume()
            mapRoute.getMapAsync(this)
        }
    }

    override fun onMapReady(gMap: GoogleMap?) {
        gMap?.let {
            googleMap = it
        }

        gMap?.uiSettings?.isZoomControlsEnabled = true
        gMap?.uiSettings?.isMapToolbarEnabled = true
        gMap?.uiSettings?.isMyLocationButtonEnabled = true
        gMap?.uiSettings?.isCompassEnabled = true

    }
}
