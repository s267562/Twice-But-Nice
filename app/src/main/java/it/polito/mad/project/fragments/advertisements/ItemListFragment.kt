package it.polito.mad.project.fragments.advertisements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import it.polito.mad.project.R
import it.polito.mad.project.viewmodels.ItemViewModel
import kotlinx.android.synthetic.main.fragment_item_list.*

class ItemListFragment : Fragment() {

    private lateinit var itemViewModel: ItemViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemViewModel = ViewModelProvider(requireActivity()).get(ItemViewModel::class.java)
    }

    override fun onStart() {
        super.onStart()
        (activity as AppCompatActivity?)?.supportActionBar?.show()
        itemViewModel.loader.observe(viewLifecycleOwner, Observer {
            if (itemViewModel.isNotLoading()) {
                // loader ended
                if (itemViewModel.error) {
                    Toast.makeText(context, "List of items not refreshed", Toast.LENGTH_SHORT).show()
                }
                itemViewModel.myItems.adapter.setItems(itemViewModel.myItems.items)
                if(itemViewModel.myItems.items.size == 0) {
                    emptyListLayout.visibility = View.VISIBLE
                    itemRecyclerView.visibility = View.GONE
                } else {
                    emptyListLayout.visibility = View.GONE
                    itemRecyclerView.visibility = View.VISIBLE
                }
                loadingLayout.visibility = View.GONE
                saleFab.show()
            } else {
                loadingLayout.visibility = View.VISIBLE
                saleFab.hide()
            }
        })
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        itemViewModel.resetLocalData()
        return inflater.inflate(R.layout.fragment_item_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemRecyclerView.layoutManager = LinearLayoutManager(this.activity)
        itemRecyclerView.adapter = itemViewModel.myItems.adapter
        setFabButton()
    }

    private fun setFabButton() {
        saleFab.setOnClickListener {
            val bundle = bundleOf("ItemId" to null)
            this.findNavController().navigate(R.id.action_itemListFragment_to_itemEditFragment, bundle)
        }
    }
}
