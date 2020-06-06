package it.polito.mad.project.models.item

import it.polito.mad.project.enums.items.ItemFilter

class ItemList<T> (val adapter: T) {
    val items: MutableList<Item> = mutableListOf()
    var filter: ItemFilter = ItemFilter.Title
}