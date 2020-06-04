package it.polito.mad.project.models.item

class ItemList<T> (val adapter: T) {
    val items: MutableList<Item> = mutableListOf()
}