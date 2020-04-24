package it.polito.mad.project.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import it.polito.mad.project.R
import it.polito.mad.project.enums.ArgumentKey
import it.polito.mad.project.models.Item

class ItemAdapter(private val items: MutableList<Item>) : RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val userItemView = LayoutInflater.from(parent.context).inflate(
            R.layout.item, parent, false)
        return ViewHolder(userItemView)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position]) {
                var bundle = bundleOf(ArgumentKey.ITEM to Gson().toJson(items[position]))
                holder.itemView.findNavController().navigate(R.id.action_navAdvertisements_to_showItemFragment, bundle)
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.unbind()
    }


    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val title: TextView = view.findViewById(R.id.itemTitle)
        private val description: TextView = view.findViewById(R.id.itemDescription)
        private val container: ConstraintLayout = view.findViewById(R.id.itemContainer)

        fun bind(item: Item, callback: (Int) -> Unit) {
            title.text = item.title
            description.text = item.description
            container.setOnClickListener { callback(adapterPosition) }
        }

        fun unbind() {
            container.setOnClickListener(null)
        }
    }
}