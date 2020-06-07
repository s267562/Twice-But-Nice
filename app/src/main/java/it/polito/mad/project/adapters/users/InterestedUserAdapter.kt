package it.polito.mad.project.adapters.users

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.Volley
import it.polito.mad.project.R
import it.polito.mad.project.commons.fragments.Notificator
import it.polito.mad.project.enums.items.ItemStatus
import it.polito.mad.project.models.item.Item
import it.polito.mad.project.models.user.User
import it.polito.mad.project.viewmodels.ItemViewModel
import org.json.JSONObject


class InterestedUserAdapter(private var users: MutableList<User>): RecyclerView.Adapter<InterestedUserAdapter.ViewHolder>() {

    private lateinit var notificator: Notificator
    private lateinit var itemViewModel: ItemViewModel

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        notificator = Notificator(Volley.newRequestQueue((parent.context as AppCompatActivity).applicationContext))
        itemViewModel = ViewModelProvider((parent.context as AppCompatActivity)).get(ItemViewModel::class.java)

        val userItemView = LayoutInflater.from(parent.context).inflate(R.layout.user, parent, false)
        return ViewHolder(
            userItemView,
            itemViewModel.item.data.value!!
        )
    }

    override fun getItemCount() = users.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(users[position],
         {
            val bundle = bundleOf("UserId" to users[position].id, "IsAuthUser" to false)
            holder.itemView.findNavController().navigate(R.id.action_usersInterestedFragment_to_showProfileFragment, bundle)
        }, {
                val updateItem = itemViewModel.item.data.value!!
                itemViewModel.sellItem(users[position]).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val body = JSONObject().put("ItemId", updateItem.id!!).put("IsMyItem", false).put("BuyerId", updateItem.buyerId)
                        notificator.sendNotification(updateItem.id!!, updateItem.title, "The item was sold", body)
                        //holder.itemView.findNavController().popBackStack()
                        val bundle = bundleOf("ItemId" to updateItem.id, "IsSoldItem" to true)
                        holder.itemView.findNavController().navigate(R.id.action_usersInterestedFragment_to_itemListFragment, bundle)
                    } else {
                        Toast.makeText((holder.itemView.context as AppCompatActivity).applicationContext, "Error on saving the buyer", Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.unbind()
    }

    fun setUsers(newUsers: MutableList<User>) {
        users = newUsers
    }

    class ViewHolder(view: View, item: Item): RecyclerView.ViewHolder(view) {
        private val nickname = view.findViewById<TextView>(R.id.nickname)
        private val location = view.findViewById<TextView>(R.id.location)
        private val container: CardView = view.findViewById(R.id.user_container)
        private val button: Button = view.findViewById(R.id.sell_button)
        private val isItemSold: Boolean = item.status == ItemStatus.Sold.toString()
        fun bind (user: User, callback: (Int) -> Unit, sellCallback: (Int) -> Unit) {
            nickname.text = user.nickname
            location.text = user.location
            container.setOnClickListener{callback(adapterPosition)}
            if (isItemSold) {
                button.visibility = View.GONE
            } else {
                button.visibility = View.VISIBLE
                button.setOnClickListener{sellCallback(adapterPosition)}
            }
        }

        fun unbind() {
            container.setOnClickListener(null)
        }
    }
}