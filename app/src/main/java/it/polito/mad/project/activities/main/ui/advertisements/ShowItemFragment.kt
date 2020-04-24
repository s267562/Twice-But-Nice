package it.polito.mad.project.activities.main.ui.advertisements

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson

import it.polito.mad.project.R
import it.polito.mad.project.enums.ArgumentKey
import it.polito.mad.project.enums.StoreFileKey
import it.polito.mad.project.models.Item
import kotlinx.android.synthetic.main.fragment_show_advertisement.*

class ShowItemFragment : Fragment() {

//    private lateinit var viewModel: ShowItemViewModel
    private lateinit var item: Item

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        viewModel = ViewModelProvider(this).get(ShowItemViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_show_advertisement, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        item = Gson().fromJson(arguments?.getString(ArgumentKey.ITEM), Item::class.java)
        item_title.text = item.title
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.edit_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.pencil_option -> {
                saveTempItemToStoreFile()
                this.findNavController().navigate(R.id.action_showItemFragment_to_itemEditFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveTempItemToStoreFile() {
        val sharedPref = this.activity?.getSharedPreferences(getString(R.string.app_store_file_name), Context.MODE_PRIVATE)
        val prefsEditor = sharedPref?.edit()
        prefsEditor?.putString(StoreFileKey.TEMP_ITEM, Gson().toJson(item))
        prefsEditor?.commit()
    }
}