package it.polito.mad.project.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.project.R
import it.polito.mad.project.models.user.User
import it.polito.mad.project.viewmodels.ItemViewModel


class UserAdapter(private var itemViewModel: ItemViewModel , private var users: MutableList<User>): RecyclerView.Adapter<UserAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val userItemView = LayoutInflater.from(parent.context).inflate(R.layout.user, parent, false)
        return ViewHolder(userItemView)
    }

    override fun getItemCount() = users.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(users[position],
         {
            val bundle = bundleOf("UserId" to users[position].id, "IsAuthUser" to false)
            holder.itemView.findNavController().navigate(R.id.action_usersInterestedFragment_to_showProfileFragment, bundle)
        }, {
                itemViewModel.item.data.value!!.status = "Sold"
                itemViewModel.item.data.value!!.buyerId = users[position].id
                itemViewModel.item.data.value!!.buyerNickname = users[position].nickname
                itemViewModel.saveItem(itemViewModel.item.data.value!!).addOnSuccessListener {
                    holder.itemView.findNavController().popBackStack()
                }
        })
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.unbind()
    }

    fun setUsers(newUsers: MutableList<User>) {
        users = newUsers
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val nickname = view.findViewById<TextView>(R.id.nickname)
        private val location = view.findViewById<TextView>(R.id.location)
        private val container: CardView = view.findViewById(R.id.user_container)
        private val button: Button = view.findViewById(R.id.sell_button)

        fun bind (user: User, callback: (Int) -> Unit, sellCallback: (Int) -> Unit) {
            nickname.text = user.nickname
            location.text = user.location
            container.setOnClickListener{callback(adapterPosition)}
            button.setOnClickListener{sellCallback(adapterPosition)}
        }

        fun unbind() {
            container.setOnClickListener(null)
        }
    }
}