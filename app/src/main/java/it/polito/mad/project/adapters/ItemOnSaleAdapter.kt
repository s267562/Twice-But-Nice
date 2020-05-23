package it.polito.mad.project.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.project.R
import it.polito.mad.project.fragments.advertisements.stringGlobal.Companion.globalFilter
import it.polito.mad.project.models.Item
import kotlinx.android.synthetic.main.item.view.*
import java.util.*

class ItemOnSaleAdapter(private var items: MutableList<Item>) : RecyclerView.Adapter<ItemOnSaleAdapter.ViewHolder>(), Filterable{
    private var totalItems: MutableList<Item> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val userItemView = LayoutInflater.from(parent.context).inflate(
            R.layout.item, parent, false)
        return ViewHolder(userItemView)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.item_edit_button.visibility = GONE
        holder.bind(items[position]
        ) {
            val bundle = bundleOf("ItemId" to items[position].id)
            holder.itemView.findNavController().navigate(R.id.action_onSaleListFragment_to_showItemFragment, bundle)
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.unbind()
    }

    fun setItems(newItems: MutableList<Item>) {
        val diffs = DiffUtil.calculateDiff(ItemDiffCallback(items, newItems))
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
            val priceStr = "${item.price} €"

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

            var filteredList: MutableList<Item> = mutableListOf()
            if(constraint.isNullOrEmpty()){
                filteredList = totalItems
            } else {
                val line: String = constraint.toString().toLowerCase(Locale.ROOT).trim()
                for(i: Item in totalItems){
                    val title = i.title.toLowerCase(Locale.ROOT)
                    val category = i.category.toLowerCase(Locale.ROOT)
                    val sub = i.subcategory.toLowerCase(Locale.ROOT)
                    val descri = i.description.toLowerCase(Locale.ROOT)
                    val price = i.price.toLowerCase(Locale.ROOT)
                    val loc = i.location.toLowerCase(Locale.ROOT)

                    if(globalFilter.toLowerCase().equals("title")){
                        if(title.contains(line)){
                            filteredList.add(i)
                        }
                    }
                    if(globalFilter.toLowerCase().equals("category")){
                        if(category.contains(line)){
                            filteredList.add(i)
                        }
                    }
                    if(globalFilter.toLowerCase().equals("subcategory")){
                        if(sub.contains(line)){
                            filteredList.add(i)
                        }
                    }
                    if(globalFilter.toLowerCase().equals("description")){
                        if(descri.contains(line)){
                            filteredList.add(i)
                        }
                    }
                    if(globalFilter.toLowerCase().equals("price")){
                        if(price.contains(line)){
                            filteredList.add(i)
                        }
                    }
                    if(globalFilter.toLowerCase().equals("location")){
                        if(loc.contains(line)){
                            filteredList.add(i)
                        }
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