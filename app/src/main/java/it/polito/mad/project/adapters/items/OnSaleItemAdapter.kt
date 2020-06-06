package it.polito.mad.project.adapters.items

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.project.R
import it.polito.mad.project.adapters.items.utils.ItemDiffUtilCallback
import it.polito.mad.project.enums.items.ItemFilter
import it.polito.mad.project.models.item.Item
import it.polito.mad.project.viewmodels.ItemViewModel
import kotlinx.android.synthetic.main.item.view.*
import java.util.*

class OnSaleItemAdapter(private var items: MutableList<Item>, private val source: String) : RecyclerView.Adapter<OnSaleItemAdapter.ViewHolder>(), Filterable {
    private lateinit var itemViewModel: ItemViewModel

    private var totalItems: MutableList<Item> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val userItemView = LayoutInflater.from(parent.context).inflate(
            R.layout.item, parent, false)
        itemViewModel = ViewModelProvider(parent.context as AppCompatActivity).get(ItemViewModel::class.java)
        return ViewHolder(userItemView)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.item_edit_button.visibility = GONE
        holder.bind(items[position]
        ) {
            val bundle = bundleOf("ItemId" to items[position].id)
            if(source == "onSaleItems")
                holder.itemView.findNavController().navigate(R.id.action_onSaleListFragment_to_showItemFragment, bundle)
            else if(source == "interestedItems")
                holder.itemView.findNavController().navigate(R.id.action_itemsOfInterestListFragment_to_showItemFragment, bundle)
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.unbind()
    }

    fun setItems(newItems: MutableList<Item>) {
        val diffs = DiffUtil.calculateDiff(
            ItemDiffUtilCallback(
                items,
                newItems
            )
        )
        items = newItems //update data
        totalItems.clear()
        totalItems.addAll(items)
        diffs.dispatchUpdatesTo(this) //animate UI
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val category: TextView = view.findViewById(R.id.item_category)
        private val title: TextView = view.findViewById(R.id.item_title)
        private val price: TextView = view.findViewById(R.id.item_price)
        private val container: CardView = view.findViewById(R.id.item_container)

        fun bind(item: Item, callback: (Int) -> Unit) {
            val priceStr = "Price: ${item.price} €"

            category.text = item.category
            title.text = item.title
            price.text = priceStr

            container.setOnClickListener { callback(adapterPosition) }
        }

        fun unbind() {
            container.setOnClickListener(null)
        }
    }

    override fun getFilter(): Filter {
        return filter
    }

    private var filter = object : Filter() {

        override fun performFiltering(constraint: CharSequence?): FilterResults {

            val filteredList: MutableList<Item> = mutableListOf()
            if(constraint.isNullOrEmpty()){
                filteredList.clear()
                filteredList.addAll(totalItems)
            } else {
                val line: String = constraint.toString().toLowerCase(Locale.ROOT).trim()
                for(i: Item in totalItems){
                    val title = i.title.toLowerCase(Locale.ROOT)
                    val category = i.category.toLowerCase(Locale.ROOT)
                    val sub = i.subcategory.toLowerCase(Locale.ROOT)
                    val description = i.description.toLowerCase(Locale.ROOT)
                    val price = i.price.toLowerCase(Locale.ROOT)
                    val loc = i.location.toLowerCase(Locale.ROOT)

                    when(itemViewModel.onSaleItems.filter) {
                        ItemFilter.Title -> { if(title.contains(line)) filteredList.add(i) }
                        ItemFilter.Category -> { if(category.contains(line)) filteredList.add(i) }
                        ItemFilter.Subcategory -> { if(sub.contains(line)) filteredList.add(i) }
                        ItemFilter.Description -> { if(description.contains(line)) filteredList.add(i) }
                        ItemFilter.Price -> { if(price.contains(line)) filteredList.add(i) }
                        ItemFilter.Location -> { if(loc.contains(line)) filteredList.add(i) }
                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            items.clear()
            items.addAll(results?.values as Collection<Item>)
            // To refresh the adapter:
            notifyDataSetChanged()
        }
    }
}