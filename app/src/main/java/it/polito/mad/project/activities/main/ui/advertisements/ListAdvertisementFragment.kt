package it.polito.mad.project.activities.main.ui.advertisements

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import it.polito.mad.project.R
import it.polito.mad.project.activities.main.ui.common.StoreFileFragment
import it.polito.mad.project.adapters.ItemAdapter
import it.polito.mad.project.enums.StoreFileKey
import it.polito.mad.project.models.Item
import kotlinx.android.synthetic.main.fragment_list_advertisement.*

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
        adsViewModel.adapter = ItemAdapter(adsViewModel.items)
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
            adsViewModel.items.add(Item("Item ${adsViewModel.index.value}"))
            adsViewModel.adapter.notifyItemInserted(adsViewModel.index.value?:0)
            adsViewModel.index.value = (adsViewModel.index.value?:0) + 1
        }
    }
}
