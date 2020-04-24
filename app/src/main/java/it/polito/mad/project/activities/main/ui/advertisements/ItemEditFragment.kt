package it.polito.mad.project.activities.main.ui.advertisements

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import it.polito.mad.project.R
import it.polito.mad.project.activities.main.ui.common.StoreFileFragment
import it.polito.mad.project.enums.ArgumentKey
import it.polito.mad.project.enums.StoreFileKey
import it.polito.mad.project.models.Item
import kotlinx.android.synthetic.main.fragment_edit_advertisement.*

class ItemEditFragment : StoreFileFragment(), AdapterView.OnItemSelectedListener {

    private lateinit var item: Item

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        item = loadFromStoreFile(StoreFileKey.TEMP_ITEM, Item::class.java)?:Item("")
        return inflater.inflate(R.layout.fragment_edit_advertisement, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context?.let {
            ArrayAdapter.createFromResource(
                it,
                R.array.item_categories,
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Apply the adapter to the spinner
                item_category_spinner.adapter = adapter
            }
        }
        item_category_spinner.onItemSelectedListener = this

        if (item != null) {
            item_title.setText(item.title)
            if (item.categoryPos >= 0) item_category_spinner.setSelection(item.categoryPos)
            item_descr.setText(item.description)
            item_location.setText(item.location)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.save_menu, menu)
    }

    override fun onOptionsItemSelected(option: MenuItem): Boolean {
        // Handle item selection
        return when (option.itemId) {
            R.id.save_option -> {
                var bundle = bundleOf(ArgumentKey.ITEM to Gson().toJson(item))
                this.findNavController().popBackStack()
                true
            }
            else -> super.onOptionsItemSelected(option)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        saveToStoreFile(StoreFileKey.TEMP_ITEM, item)
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        var category: String = parent?.getItemAtPosition(pos) as String
        item.category = category
        item.categoryPos = pos
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}