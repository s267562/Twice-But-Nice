package it.polito.mad.project.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.project.R
import it.polito.mad.project.models.User

class UserAdapter(private var users: MutableList<User>): RecyclerView.Adapter<UserAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val userItemView = LayoutInflater.from(parent.context).inflate(R.layout.user, parent, false)
        return ViewHolder(userItemView)
    }

    override fun getItemCount() = users.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(users[position]
        ) {
            val bundle = bundleOf("UserId" to users[position].id, "IsAuthUser" to false)
            holder.itemView.findNavController().navigate(R.id.action_usersInterestedFragment_to_showProfileFragment, bundle)
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.unbind()
    }

    fun setUsers(newUsers: MutableList<User>) {
        users = newUsers
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val fullName = view.findViewById<TextView>(R.id.full_name)
        private val location = view.findViewById<TextView>(R.id.location)
        private val container: CardView = view.findViewById(R.id.user_container)


        fun bind (user: User, callback: (Int) -> Unit) {
            fullName.text = user.name
            location.text = user.location
            container.setOnClickListener{callback(adapterPosition)}
        }

        fun unbind() {
            container.setOnClickListener(null)
        }
    }
}