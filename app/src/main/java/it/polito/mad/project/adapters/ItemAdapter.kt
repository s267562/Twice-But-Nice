package it.polito.mad.project.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import it.polito.mad.project.R
import it.polito.mad.project.enums.ArgumentKey
import it.polito.mad.project.enums.StoreFileKey
import it.polito.mad.project.models.Item
import it.polito.mad.project.utils.Util.Companion.saveToStoreFile
import java.io.File

class ItemAdapter(private var items: MutableList<Item>) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val userItemView = LayoutInflater.from(parent.context).inflate(
            R.layout.item, parent, false)
        return ViewHolder(userItemView)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position],
            {
                var bundle = bundleOf(ArgumentKey.SHOW_ITEM to Gson().toJson(items[position]))
                holder.itemView.findNavController().navigate(R.id.action_navAdvertisements_to_itemListFragment, bundle)
            },
            {
                var bundle = bundleOf(ArgumentKey.EDIT_ITEM to Gson().toJson(items[position]))
                holder.itemView.findNavController().navigate(R.id.action_navAdvertisements_to_itemEditFragment, bundle)
            })

    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.unbind()
    }

    fun setItems(newItems: MutableList<Item>) {
        val diffs = DiffUtil.calculateDiff(ItemDiffCallback(items, newItems))
        items = newItems //update data
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
}