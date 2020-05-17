package it.polito.mad.project.fragments.advertisements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import it.polito.mad.project.R
import it.polito.mad.project.enums.ArgumentKey
import kotlinx.android.synthetic.main.fragment_item_list.*

// POINT 5: Implement ItemListFragment

class ItemListFragment : Fragment() {

    private lateinit var adsViewModel: ItemViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adsViewModel = ViewModelProvider(activity?:this).get(ItemViewModel::class.java)
    }

    override fun onStart() {
        super.onStart()
        (activity as AppCompatActivity?)?.supportActionBar?.show()
        adsViewModel.loader.observe(viewLifecycleOwner, Observer {
            if (it == false) {
                // loader ended
                adsViewModel.adapter.setItems(adsViewModel.items)
                if(adsViewModel.items.size == 0) {
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
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_item_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemRecyclerView.layoutManager = LinearLayoutManager(this.activity)
        itemRecyclerView.adapter = adsViewModel.adapter
        setFabButton()
    }

    private fun setFabButton() {
        saleFab.show()
        saleFab.setOnClickListener {
            var bundle = bundleOf(ArgumentKey.EDIT_ITEM to adsViewModel.items.size)
            this.findNavController().navigate(R.id.action_itemListFragment_to_itemEditFragment, bundle)
        }
    }
}
