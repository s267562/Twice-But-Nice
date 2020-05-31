package it.polito.mad.project.models.item

import it.polito.mad.project.adapters.ItemAdapter

class ItemList<T> (val adapter: T) {
    val items: MutableList<Item> = mutableListOf()
}