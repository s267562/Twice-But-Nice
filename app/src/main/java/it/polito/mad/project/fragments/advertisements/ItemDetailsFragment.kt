package it.polito.mad.project.fragments.advertisements

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController

import it.polito.mad.project.R
import it.polito.mad.project.enums.ArgumentKey
import kotlinx.android.synthetic.main.fragment_item_details.*

class ItemDetailsFragment : Fragment() {

    private lateinit var adsViewModel: ItemViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adsViewModel = ViewModelProvider(activity?:this).get(ItemViewModel::class.java)
    }

    override fun onStart() {
        super.onStart()
        var selectedId = arguments?.getInt(ArgumentKey.SHOW_ITEM)?:0
        adsViewModel.loadItem(selectedId)

        adsViewModel.item.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                item_title.text = it.title
                item_descr.text = it.description
                item_location.text = it.location
                item_category.text = "${it.category} - ${it.subcategory}"
                item_price.text = "${it.price?.toString()} €"
                item_exp.text = it.expiryDate
                if (it.imagePath != null && it.imagePath!!.isNotEmpty()) {
                    val image: Bitmap = BitmapFactory.decodeFile(it.imagePath)
                    if (image != null) item_photo.setImageBitmap(image)
                }
            }
        })

        adsViewModel.loader.observe(viewLifecycleOwner, Observer {
            if (it == false) {
                loadingLayout.visibility = View.GONE
                if (adsViewModel.error) {
                    Toast.makeText(context, "Error on item loading", Toast.LENGTH_SHORT).show()
                }
            } else {
                loadingLayout.visibility = View.VISIBLE
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_item_details, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.edit_menu, menu)
    }

    override fun onOptionsItemSelected(option: MenuItem): Boolean {
        // Handle item selection
        return when (option.itemId) {
            R.id.pencil_option -> {
                var bundle = bundleOf(ArgumentKey.EDIT_ITEM to null)
                this.findNavController().navigate(R.id.action_showItemFragment_to_itemEditFragment, bundle)
                true
            }
            else -> super.onOptionsItemSelected(option)
        }
    }
}