package it.polito.mad.project.adapters.items

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import it.polito.mad.project.R
import it.polito.mad.project.adapters.items.utils.ItemDiffUtilCallback
import it.polito.mad.project.models.item.Item

class ReviewItemAdapter(private var itemReviews: MutableList<Item>) : RecyclerView.Adapter<ReviewItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val userReviewView = LayoutInflater.from(parent.context).inflate(
            R.layout.review, parent, false
        )
        return ViewHolder(
            userReviewView
        )
    }

    override fun getItemCount() = itemReviews.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itemReviews[position])
    }

    fun setItemReviews(newItemReviews: MutableList<Item>) {
        val diffs = DiffUtil.calculateDiff(
            ItemDiffUtilCallback(
                itemReviews,
                newItemReviews
            )
        )
        itemReviews.clear()
        itemReviews.addAll(newItemReviews)
        diffs.dispatchUpdatesTo(this) //animate UI
    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val buyerNickname: TextView = view.findViewById(R.id.buyerNickname)
        private val reviewRating: RatingBar = view.findViewById(R.id.reviewRating)
        private val reviewTitle: TextView = view.findViewById(R.id.reviewTitle)
        private val reviewDescription: TextView = view.findViewById(R.id.reviewDescription)

        fun bind(item: Item) {

            buyerNickname.text = item.buyerNickname
            reviewRating.rating = item.review!!.rating
            reviewTitle.text = item.title
            reviewDescription.text = item.review!!.description

        }
    }

}
