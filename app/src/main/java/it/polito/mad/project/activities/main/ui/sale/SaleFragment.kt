package it.polito.mad.project.activities.main.ui.sale

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import it.polito.mad.project.R
import it.polito.mad.project.adapters.ItemAdapter
import it.polito.mad.project.enums.StoreFileKey
import it.polito.mad.project.models.Item
import kotlinx.android.synthetic.main.fragment_sale.*

class SaleFragment : Fragment() {
    private val layoutManager: LinearLayoutManager = LinearLayoutManager(this.activity)
    private var items: MutableList<Item> = mutableListOf()
    private var itemsIndex : MutableLiveData<Int> = MutableLiveData(0)
    private lateinit var itemsAdapter: ItemAdapter
//    private lateinit var saleViewModel: SaleViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        saleViewModel =
//            ViewModelProvider(this).get(SaleViewModel::class.java)
//        saleViewModel.text.observe(viewLifecycleOwner, Observer {
//            emptyMessage.text = it
//        })

        // Load store file of our app from shared preferences
        val sharedPreferences = this.activity?.getSharedPreferences(getString(R.string.app_store_file_name), Context.MODE_PRIVATE)
        // Load from the store file
        val itemsJson: String? = sharedPreferences?.getString(StoreFileKey.ITEMS, "")
        if (itemsJson != null && itemsJson.isNotEmpty()) {
            // Assign the stored list of items
            items = Gson().fromJson(itemsJson, Array<Item>::class.java).toMutableList()
        }

        if (items == null || items.size == 0) {
            items = mutableListOf()
            itemsAdapter = ItemAdapter(items)
        } else {
            itemsIndex.value = items.size
            itemsAdapter = ItemAdapter(items)
        }

        itemsIndex.observe(this.viewLifecycleOwner, Observer {
            if (it == 0) {
                emptyMessage.text = "Non ci sono elementi in vendita"
            } else {
                emptyMessage.text = ""
            }
        })

        Log.d("TEAMSVIK", "TESTTT")
        return inflater.inflate(R.layout.fragment_sale, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemRecyclerView.adapter = itemsAdapter
        itemRecyclerView.layoutManager = layoutManager
        setFabButton()
    }

    override fun onDestroy() {
        super.onDestroy()
        val sharedPref = this.activity?.getSharedPreferences(getString(R.string.app_store_file_name), Context.MODE_PRIVATE)
        val prefsEditor = sharedPref?.edit()
        prefsEditor?.putString(StoreFileKey.ITEMS, Gson().toJson(items.toTypedArray()))
        prefsEditor?.commit()
    }

    private fun setFabButton() {
        saleFab.show()
        saleFab.setOnClickListener {
            items.add(Item("Item ${itemsIndex.value}"))
            itemsAdapter.notifyItemInserted(itemsIndex.value?:0)
            itemsIndex.value = (itemsIndex.value?:0) + 1

        }
    }
}
