package it.polito.mad.project.commons.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.project.R
import it.polito.mad.project.adapters.items.utils.ItemDiffUtilCallback
import it.polito.mad.project.enums.items.ItemStatus
import it.polito.mad.project.models.item.Item

class ItemAdapter(private var items: MutableList<Item>) : RecyclerView.Adapter<ItemAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val userItemView = LayoutInflater.from(parent.context).inflate(
            R.layout.item, parent, false)
        return ViewHolder(userItemView)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position],
            {
                val bundle = bundleOf("ItemId" to items[position].id, "IsSoldItem" to (items[position].status == ItemStatus.Sold.toString()))
                holder.itemView.findNavController().navigate(R.id.action_itemListFragment_to_showItemFragment, bundle)
            },
            {
                val bundle = bundleOf("ItemId" to items[position].id)
                holder.itemView.findNavController().navigate(R.id.action_itemListFragment_to_itemEditFragment, bundle)
            })
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
        items.clear()
        items.addAll(newItems)
        diffs.dispatchUpdatesTo(this) //animate UI
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val category: TextView = view.findViewById(R.id.item_category)
        private val title: TextView = view.findViewById(R.id.item_title)
        private val price: TextView = view.findViewById(R.id.item_price)
        private val container: CardView = view.findViewById(R.id.item_container)
        private val button: Button = view.findViewById(R.id.item_edit_button)

        fun bind(item: Item, callback: (Int) -> Unit, callbackEdit: (Int) -> Unit) {
            val priceStr: String

            if (item.status == ItemStatus.Sold.toString()) {
                button.visibility = View.GONE
                priceStr = "Sold to ${item.buyerNickname} for ${item.price} €"
            } else {
                button.visibility = View.VISIBLE
                priceStr = "On sale for ${item.price} €"
                button.setOnClickListener { callbackEdit(adapterPosition) }
            }
            container.setOnClickListener { callback(adapterPosition) }

            category.text = item.category
            title.text = item.title
            price.text = priceStr
        }

        fun unbind() {
            container.setOnClickListener(null)
            button.setOnClickListener(null)
        }
    }
}