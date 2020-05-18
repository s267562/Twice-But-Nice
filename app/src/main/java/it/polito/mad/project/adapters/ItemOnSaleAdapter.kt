package it.polito.mad.project.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.Button
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.project.R
import it.polito.mad.project.enums.ArgumentKey
import it.polito.mad.project.models.Item
import kotlinx.android.synthetic.main.item.view.*


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
        holder.bind(items[position],
            {
                var bundle = bundleOf(ArgumentKey.SHOW_ITEM to position)
                holder.itemView.findNavController().navigate(R.id.action_itemListFragment_to_showItemFragment, bundle)
            },
            {
                var bundle = bundleOf(ArgumentKey.EDIT_ITEM to position)
                holder.itemView.findNavController().navigate(R.id.action_itemListFragment_to_itemEditFragment, bundle)
            })
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
        private val button: Button = view.findViewById(R.id.item_edit_button)

        fun bind(item: Item, callback: (Int) -> Unit, callbackEdit: (Int) -> Unit) {
            val priceStr = "${item.price} €"

            category.text = item.category
            title.text = item.title
            price.text = priceStr

            container.setOnClickListener { callback(adapterPosition) }
            button.setOnClickListener { callbackEdit(adapterPosition) }
        }

        fun unbind() {
            container.setOnClickListener(null)
        }
    }
    /*fun setFilter(newList: MutableList<Item>) {
        var arrayList = mutableListOf<Item>()
        arrayList.addAll(newList)
        notifyDataSetChanged()
    }*/

    override fun getFilter(): Filter {
        return filter;
    }

    private var filter = object : Filter() {

        override fun performFiltering(constraint: CharSequence?): FilterResults {

            var filteredList: MutableList<Item> = mutableListOf()
            if(constraint.isNullOrEmpty()){
                filteredList = totalItems
            } else {
                var line: String = constraint.toString().toLowerCase().trim()
                for(i: Item in totalItems){
                    val title = i.title.toLowerCase()
                    val category = i.category.toLowerCase()
                    val sub = i.subcategory.toLowerCase()
                    val descri = i.description.toLowerCase()
                    val price = i.price.toLowerCase()
                    val loc = i.location.toLowerCase()
                    if(line.contains(title) || line.contains(category) || line.contains(sub)
                        || line.contains(descri) || line.contains(price) || line.contains(loc)){

                        filteredList.add(i)
                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            items.clear()
            items.addAll(results?.values as MutableList<Item>)
            // To refresh the adapter:
            notifyDataSetChanged()
        }

    }
}