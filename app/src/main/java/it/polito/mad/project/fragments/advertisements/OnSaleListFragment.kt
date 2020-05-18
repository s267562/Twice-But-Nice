package it.polito.mad.project.fragments.advertisements

import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import it.polito.mad.project.R
import it.polito.mad.project.adapters.ItemAdapter
import it.polito.mad.project.models.Item
import kotlinx.android.synthetic.main.fragment_on_sale_list.*


class OnSaleListFragment : Fragment(), SearchView.OnQueryTextListener {

    private lateinit var itemViewModel: ItemViewModel
    lateinit var searchView: SearchView
    lateinit var recyclerAdapter: ItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemViewModel = ViewModelProvider(activity?:this).get(ItemViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_on_sale_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        itemRecyclerView.setHasFixedSize(true)
        itemRecyclerView.layoutManager = LinearLayoutManager(this.activity)
        recyclerAdapter = itemViewModel.adapter
        itemRecyclerView.adapter = recyclerAdapter
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
        inflater.inflate(R.menu.search_menu, menu)
        var itemMenu: MenuItem = menu!!.findItem(R.id.menu_search)

        if(itemMenu != null){
            searchView = itemMenu.actionView as SearchView

            val editText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
            editText.hint = "Type title or category"

            searchView.setOnQueryTextListener(this)
        } else {
            //Toast.makeText(activity, "WRONG", Toast.LENGTH_LONG).show()
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if(newText!!.isNotEmpty()){
            // perform the filter
            val line = newText.toLowerCase()
            var newList = mutableListOf<Item>()
            for(i: Item in itemViewModel.items){
                val title = i.title.toLowerCase()
                val category = i.category.toLowerCase()
                val sub = i.subcategory.toLowerCase()
                val descri = i.description.toLowerCase()
                val price = i.price.toLowerCase()
                val loc = i.location.toLowerCase()
                if(line.contains(title) || line.contains(category) || line.contains(sub)
                    || line.contains(descri) || line.contains(price) || line.contains(loc)){
                    Toast.makeText(activity, line, Toast.LENGTH_SHORT).show()
                    newList.add(i)
                }
            }
            recyclerAdapter.setFilter(newList)
            itemRecyclerView.adapter = recyclerAdapter
        }
        return true
    }
}