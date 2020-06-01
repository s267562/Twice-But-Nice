package it.polito.mad.project.fragments.advertisements

import android.app.AlertDialog
import android.content.DialogInterface
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.messaging.FirebaseMessaging
import it.polito.mad.project.R
import it.polito.mad.project.commons.fragments.NotificationFragment
import it.polito.mad.project.models.item.Item
import it.polito.mad.project.viewmodels.ItemViewModel
import it.polito.mad.project.viewmodels.UserViewModel
import kotlinx.android.synthetic.main.fragment_item_details.*
import org.json.JSONObject
import java.io.IOException
import java.util.*

class ItemDetailsFragment : NotificationFragment(), OnMapReadyCallback {

    private lateinit var itemViewModel: ItemViewModel
    private lateinit var userViewModel: UserViewModel

    private var isMyItem: Boolean = false

    private var listenerRegistration: ListenerRegistration? = null

    private lateinit var googleMap: GoogleMap
    private lateinit var geocode : Geocoder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemViewModel = ViewModelProvider(requireActivity()).get(ItemViewModel::class.java)
        userViewModel = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)

    }

    override fun onStart() {
        super.onStart()
        (activity as AppCompatActivity?)?.supportActionBar?.show()

        isMyItem = arguments?.getBoolean("IsMyItem")?:false
        itemViewModel.item.data.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                item_title.text = it.title
                item_descr.text = it.description
                item_location.text = it.location
                item_category.text = it.category
                item_subcategory.text = it.subcategory
                item_price.text = "${it.price} €"
                item_exp.text = it.expiryDate
                if (listenerRegistration == null)
                   listenerRegistration = itemViewModel.listenToChanges()
            }
        })

        itemViewModel.item.image.observe(viewLifecycleOwner, Observer {
            if (it != null){
                item_photo.setImageBitmap(it)
            }
        })

        itemViewModel.loader.observe(viewLifecycleOwner, Observer {
            if (itemViewModel.isNotLoading()) {
                loadingLayout.visibility = View.GONE
                if (itemViewModel.error) {
                    Toast.makeText(context, "Error on item loading", Toast.LENGTH_SHORT).show()
                }
                if (!isMyItem) {
                    if (itemViewModel.item.data.value!!.status == "Available") {
                        var interestFabDrawableId: Int = R.drawable.ic_favorite_border_white_24dp
                        if (itemViewModel.item.interest.value)
                            interestFabDrawableId = R.drawable.ic_favorite_white_24dp
                        interestFab.setImageResource(interestFabDrawableId)
                        interestFab.show()
                    } else {
                        interestFab.hide()
                    }
                    interestedUsersFab.hide()
                } else {
                    interestFab.hide()
                    interestedUsersFab.show()
                }

            } else {
                loadingLayout.visibility = View.VISIBLE
                interestFab.hide()
                interestedUsersFab.hide()
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_item_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setFabButton()
        itemViewModel.loadItem(arguments?.getString("ItemId")!!)

        item_location.setOnClickListener {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.map, null)

            val mapView = dialogView.findViewById<MapView>(R.id.map)

            Toast.makeText(context, item_location.text.toString(), Toast.LENGTH_SHORT).show()

            if(mapView != null) {
                mapView.onCreate(null)
                mapView.onResume()
                mapView.getMapAsync(this)
            }

            val builder = AlertDialog.Builder(context).setView(dialogView)
                .setNegativeButton("Close Map",
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                    })
            val alertDialog = builder.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if (isMyItem) {
            inflater.inflate(R.menu.edit_menu, menu)
        }
    }

    override fun onOptionsItemSelected(option: MenuItem): Boolean {
        // Handle item selection
        return when (option.itemId) {
            R.id.pencil_option -> {
                val bundle = bundleOf("ItemId" to itemViewModel.item.data.value?.id)
                this.findNavController().navigate(R.id.action_showItemFragment_to_itemEditFragment, bundle)
                true
            }
            else -> super.onOptionsItemSelected(option)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listenerRegistration?.remove()
    }

    private fun setFabButton() {
        interestFab.setOnClickListener {
            itemViewModel.updateItemInterest()
                .addOnSuccessListener {
                    val item = itemViewModel.item.data.value as Item
                    val nickname = userViewModel.user.data.value!!.nickname
                    val body = JSONObject().put("ItemId", item.id).put("IsMyItem", true)
                    if (itemViewModel.item.interest.value) {
                        FirebaseMessaging.getInstance().subscribeToTopic(item.id!!)
                        sendNotification(item.user, item.title, "$nickname is interested in your item", body)
                    }
                    else {
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(item.id!!)
                        sendNotification(item.user, item.title, "$nickname is not more interested in your item", body)
                    }
                }
        }
        interestedUsersFab.setOnClickListener{
            itemViewModel.loadInterestedUsers()
            this.findNavController().navigate(R.id.action_showItemFragment_to_usersInterestedFragment)
        }
    }

    override fun onMapReady(gMap: GoogleMap?) {
        gMap?.let {
            googleMap = it
        }

        geocode = Geocoder(context?.applicationContext, Locale.getDefault())

        gMap?.uiSettings?.isZoomControlsEnabled = true
        gMap?.uiSettings?.isMapToolbarEnabled = true
        gMap?.uiSettings?.isMyLocationButtonEnabled = true
        gMap?.uiSettings?.isCompassEnabled = true

        try {
            var addr = geocode.getFromLocationName(item_location.text.toString(), 1)
            if(addr.size > 0){
                var address : Address = addr.get(0)
                gMap?.addMarker(
                    MarkerOptions()
                        .position(LatLng(address.latitude, address.longitude))
                        .title("Item Current Location")
                )
            }
        } catch (e: IOException){
            e.printStackTrace()
        }
    }
}
