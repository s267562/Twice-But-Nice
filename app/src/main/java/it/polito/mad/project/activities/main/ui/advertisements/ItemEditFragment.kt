package it.polito.mad.project.activities.main.ui.advertisements

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import it.polito.mad.project.R
import it.polito.mad.project.enums.ArgumentKey
import it.polito.mad.project.enums.StoreFileKey
import it.polito.mad.project.models.Item
import kotlinx.android.synthetic.main.fragment_edit_advertisement.*

class ItemEditFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private lateinit var item: Item

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        loadTempItemFromStoreFile()
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
        inflater.inflate(R.menu.edit_menu, menu)
    }

    override fun onOptionsItemSelected(option: MenuItem): Boolean {
        // Handle item selection
        return when (option.itemId) {
            R.id.save_option -> {
                var bundle = bundleOf(ArgumentKey.ITEM to Gson().toJson(item))
                this.findNavController().navigate(R.id.action_itemEditFragment_to_showItemFragment, bundle)
                true
            }
            else -> super.onOptionsItemSelected(option)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        saveTempItemToStoreFile()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        var category: String = parent?.getItemAtPosition(pos) as String
        item.category = category
        item.categoryPos = pos
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    private fun loadTempItemFromStoreFile() {
        // Load store file of our app from shared preferences
        val sharedPreferences = this.activity?.getSharedPreferences(getString(R.string.app_store_file_name), Context.MODE_PRIVATE)
        // Load from the store file
        val itemJson: String? = sharedPreferences?.getString(StoreFileKey.TEMP_ITEM, "")
        if (itemJson != null && itemJson.isNotEmpty()) {
            // Assign the stored list of items
            val item = Gson().fromJson(itemJson, Item::class.java)
            if (item != null) {
                this.item = item
            }
        }
    }

    private fun saveTempItemToStoreFile() {
        val sharedPref = this.activity?.getSharedPreferences(getString(R.string.app_store_file_name), Context.MODE_PRIVATE)
        val prefsEditor = sharedPref?.edit()
        prefsEditor?.putString(StoreFileKey.TEMP_ITEM, Gson().toJson(item))
        prefsEditor?.commit()
    }

}