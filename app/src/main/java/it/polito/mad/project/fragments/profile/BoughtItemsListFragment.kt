package it.polito.mad.project.fragments.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import it.polito.mad.project.R
import it.polito.mad.project.viewmodels.ItemViewModel
import kotlinx.android.synthetic.main.fragment_on_sale_list.*

class BoughtItemsListFragment : Fragment() {

    private lateinit var itemViewModel : ItemViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemViewModel = ViewModelProvider(activity?:this).get(ItemViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_bought_items_list, container, false)
    }

    override fun onStart() {
        super.onStart()
        (activity as AppCompatActivity?)?.supportActionBar?.show()
        itemViewModel.loader.observe(viewLifecycleOwner, Observer {
            if (itemViewModel.isNotLoading()) {
                if (itemViewModel.error) {
                    Toast.makeText(context, "List of items not refreshed", Toast.LENGTH_SHORT).show()
                }
                // loader ended
                itemViewModel.boughtItems.adapter.setItems(itemViewModel.boughtItems.items)
                if(itemViewModel.boughtItems.items.size == 0) {
                    emptyListLayout.visibility = View.VISIBLE
                    itemRecyclerView.visibility = View.GONE
                } else {
                    emptyListLayout.visibility = View.GONE
                    itemRecyclerView.visibility = View.VISIBLE
                }
                loadingLayout.visibility = View.GONE
            } else {
                loadingLayout.visibility = View.VISIBLE
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemRecyclerView.setHasFixedSize(true)
        itemRecyclerView.layoutManager = LinearLayoutManager(this.activity)
        itemRecyclerView.adapter = itemViewModel.boughtItems.adapter
        itemViewModel.loadItemsBought()
    }
}