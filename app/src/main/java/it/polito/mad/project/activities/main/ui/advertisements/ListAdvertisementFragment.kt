package it.polito.mad.project.activities.main.ui.advertisements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import it.polito.mad.project.R
import it.polito.mad.project.activities.main.ui.common.StoreFileFragment
import it.polito.mad.project.enums.StoreFileKey
import it.polito.mad.project.models.Item
import kotlinx.android.synthetic.main.fragment_list_advertisement.*

// POINT 5: Implement ItemListFragment

class ListAdvertisementFragment : StoreFileFragment() {

    private lateinit var adsViewModel: ListAdvertisementViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        adsViewModel =
            ViewModelProvider(this).get(ListAdvertisementViewModel::class.java)

        adsViewModel.index.observe(this.viewLifecycleOwner, Observer {
            emptyMessage.text = if (it == 0) "Non ci sono elementi in vendita" else ""
        })

        adsViewModel.items = loadFromStoreFile(StoreFileKey.ITEMS, Array<Item>::class.java)?.toMutableList()?: mutableListOf()
        adsViewModel.index.value = adsViewModel.items.size
        adsViewModel.adapter.setItems(adsViewModel.items)

        // RESET
        removeFromStoreFile(StoreFileKey.ITEM)
        removeFromStoreFile(StoreFileKey.TEMP_ITEM)
        return inflater.inflate(R.layout.fragment_list_advertisement, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemRecyclerView.layoutManager = LinearLayoutManager(this.activity)
        itemRecyclerView.adapter = adsViewModel.adapter
        setFabButton()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        saveToStoreFile(StoreFileKey.ITEMS, adsViewModel.items.toTypedArray())
    }

    private fun setFabButton() {
        saleFab.show()
        saleFab.setOnClickListener {

            saveToStoreFile(StoreFileKey.TEMP_ITEM, Item(adsViewModel.index.value?:0))
            this.findNavController().navigate(R.id.action_navAdvertisements_to_itemEditFragment)
        }
    }
}
