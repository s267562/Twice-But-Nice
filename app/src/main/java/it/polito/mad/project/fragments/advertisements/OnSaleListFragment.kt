package it.polito.mad.project.fragments.advertisements

import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import android.widget.SearchView.OnQueryTextListener
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import it.polito.mad.project.R
import kotlinx.android.synthetic.main.fragment_on_sale_list.*


class OnSaleListFragment : Fragment() {
    // Nome del layout del fragment: fragment_on_sale_list
    // Nome della recycler view: rvItems
    // Nome del menu da iniettare: search_menu.xml
    private var searchView: SearchView? = null
    private var queryTextListener: OnQueryTextListener? = null

    private lateinit var itemViewModel: ItemViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemViewModel = ViewModelProvider(activity?:this).get(ItemViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        //setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_on_sale_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemRecyclerView.layoutManager = LinearLayoutManager(this.activity)
        itemRecyclerView.adapter = itemViewModel.adapter
    }

    override fun onStart() {
        super.onStart()
        (activity as AppCompatActivity?)?.supportActionBar?.show()
        itemViewModel.loader.observe(viewLifecycleOwner, Observer {
            if (it == false) {
                // loader ended
                itemViewModel.adapter.setItems(itemViewModel.items)
                if(itemViewModel.items.size == 0) {
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

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        /*inflater.inflate(R.menu.search_menu, menu)
        val searchItem: MenuItem = menu.findItem(R.id.action_search)
        val searchManager =
            requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        if (searchItem != null) {
            searchView = searchItem.getActionView() as SearchView?
        }
        if (searchView != null) {
            searchView!!.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
            queryTextListener = object : OnQueryTextListener {
                override fun onQueryTextChange(newText: String): Boolean {
                    Log.i("onQueryTextChange", newText)
                    return true
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    Log.i("onQueryTextSubmit", query)
                    return true
                }
            }
            searchView!!.setOnQueryTextListener(queryTextListener)
        }*/
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search ->
                // Not implemented here
                return false
            else -> {
            }
        }
        //searchView!!.setOnQueryTextListener(queryTextListener)
        return super.onOptionsItemSelected(item)
    }
}