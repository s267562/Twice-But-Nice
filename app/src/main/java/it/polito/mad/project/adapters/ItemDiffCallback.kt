package it.polito.mad.project.adapters

import androidx.recyclerview.widget.DiffUtil
import it.polito.mad.project.models.Item

class ItemDiffCallback(private val items: MutableList<Item>,
                       private val newItems: MutableList<Item>): DiffUtil.Callback() {

        override fun getOldListSize(): Int = items.size
        override fun getNewListSize(): Int = newItems.size
        override fun areItemsTheSame(oldP: Int, newP: Int): Boolean {
            return items[oldP].id === newItems[newP].id
        }
        override fun areContentsTheSame(oldPosition: Int,
                                        newPosition: Int): Boolean {
            val old = items[oldPosition]

            val actual = newItems[newPosition]
            return old.title == actual.title
                    && old.price == actual.price
                    && old.description == actual.description
                    && old.imagePath == actual.imagePath
                    && old.category == actual.category
                    && old.location == actual.location
                    && old.expiryDate == actual.expiryDate
        }
}
