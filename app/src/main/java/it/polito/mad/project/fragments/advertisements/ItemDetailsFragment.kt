package it.polito.mad.project.fragments.advertisements

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.ListenerRegistration

import it.polito.mad.project.R
import kotlinx.android.synthetic.main.fragment_item_details.*
import kotlinx.android.synthetic.main.fragment_item_details.item_descr
import kotlinx.android.synthetic.main.fragment_item_details.item_exp
import kotlinx.android.synthetic.main.fragment_item_details.item_location
import kotlinx.android.synthetic.main.fragment_item_details.item_photo
import kotlinx.android.synthetic.main.fragment_item_details.item_price
import kotlinx.android.synthetic.main.fragment_item_details.item_title
import kotlinx.android.synthetic.main.fragment_item_details.loadingLayout

class ItemDetailsFragment : Fragment() {

    private lateinit var itemViewModel: ItemViewModel
    private var isMyItem: Boolean = false
    private var listenerRegistration: ListenerRegistration? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemViewModel = ViewModelProvider(activity?:this).get(ItemViewModel::class.java)
    }

    override fun onStart() {
        super.onStart()
        isMyItem = arguments?.getBoolean("IsMyItem")?:false
        itemViewModel.item.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                item_title.text = it.title
                item_descr.text = it.description
                item_location.text = it.location
                item_category.text = "${it.category} - ${it.subcategory}"
                item_price.text = "${it.price?.toString()} €"
                item_exp.text = it.expiryDate
                if (listenerRegistration == null)
                   listenerRegistration = itemViewModel.listenToChanges()
            }
        })

        itemViewModel.itemPhoto.observe(viewLifecycleOwner, Observer {
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
                if (!isMyItem){
                    interestedUsersFab.hide()
                    interestFab.show()
                }
                else{
                    interestedUsersFab.show()
                    interestFab.hide()
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
                var bundle = bundleOf("ItemId" to itemViewModel.item.value?.id)
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
            // TODO: save the user as interested for the item and send notification
            Toast.makeText(activity, "Not yer implemented", Toast.LENGTH_SHORT)
        }
        interestedUsersFab.setOnClickListener{
            this.findNavController().navigate(R.id.action_showItemFragment_to_usersInterestedFragment)
        }
    }
}