package it.polito.mad.project.fragments.advertisements

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.*
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson

import it.polito.mad.project.R
import it.polito.mad.project.fragments.common.StoreFileFragment
import it.polito.mad.project.enums.ArgumentKey
import it.polito.mad.project.enums.StoreFileKey
import it.polito.mad.project.models.Item
import kotlinx.android.synthetic.main.fragment_item_details.*

// POINT 3: Implement the ItemDetailsFragment

class ItemDetailsFragment : StoreFileFragment() {

//    private lateinit var viewModel: ShowItemViewModel
    private lateinit var item: Item

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        viewModel = ViewModelProvider(this).get(ShowItemViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_item_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        item = Gson().fromJson(arguments?.getString(ArgumentKey.SHOW_ITEM), Item::class.java)
        item = loadFromStoreFile(StoreFileKey.ITEM, Item::class.java)?:item

        if (item != null) {
            item_title.text = item.title
            item_descr.text = item.description
            item_location.text = item.location
            item_category_spinner.text = item.category
            item_price.text = item.price?.toString()
            item_exp.text = item.expiryDate
            if (item.imagePath != null && item.imagePath!!.isNotEmpty()) {
                val image: Bitmap = BitmapFactory.decodeFile(item.imagePath)
                if (image != null) item_photo.setImageBitmap(image)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.edit_menu, menu)
    }

    override fun onOptionsItemSelected(option: MenuItem): Boolean {
        // Handle item selection
        return when (option.itemId) {
            R.id.pencil_option -> {
                saveToStoreFile(StoreFileKey.TEMP_ITEM, item)
                this.findNavController().navigate(R.id.action_showItemFragment_to_itemEditFragment)
                true
            }
            else -> super.onOptionsItemSelected(option)
        }
    }
}