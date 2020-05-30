package it.polito.mad.project.commons.fragments

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import it.polito.mad.project.R

class MapViewFragment : DialogFragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater

            val inflater = requireActivity().layoutInflater;
            val viewToInflate: View = inflater.inflate(R.layout.map, null)

            val mapView = viewToInflate.findViewById<MapView>(R.id.map)

            if(mapView != null) {
                mapView.onCreate(null)
                mapView.onResume()
                mapView.getMapAsync(this)
            }

            builder.setView(viewToInflate)
                // Add action buttons
                .setNegativeButton("Close Map",
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onMapReady(gMap: GoogleMap?) {
        gMap?.let {
            googleMap = it

        }

        gMap?.setOnMapClickListener (object : GoogleMap.OnMapClickListener {
            override fun onMapClick(latLng: LatLng?) {
                val markerOpt = MarkerOptions()
                markerOpt.position(latLng!!)
                gMap.clear()
                gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 7.2F))
                gMap.addMarker(markerOpt)
            }
        })
    }
}