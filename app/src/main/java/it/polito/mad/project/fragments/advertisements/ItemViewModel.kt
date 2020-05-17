package it.polito.mad.project.fragments.advertisements

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.polito.mad.project.adapters.ItemAdapter
import it.polito.mad.project.models.Item
import it.polito.mad.project.repositories.ItemRepository

class ItemViewModel : ViewModel() {
    var items: MutableList<Item> = mutableListOf()
    var index: Int = 0
    var counter : MutableLiveData<Int> = MutableLiveData(0)
    var adapter: ItemAdapter = ItemAdapter(items)

    val selected = MutableLiveData<Item>()

    private var reloadSelected = true
    private var reload = true
    private val itemRepository = ItemRepository()

    fun saveItem(item: Item) {
        reloadSelected = true
        reload = true
        itemRepository.saveUserItem(item)
    }

    fun loadItem(id: Int) {
        if (reload && reloadSelected) {
            itemRepository.getUserItem(id).addOnSuccessListener {
                selected.value = it.toObject(Item::class.java)
                reloadSelected = false
            }
        } else {
            selected.value = items[id]
        }
        index = id
    }

    fun loadItems() {
        if (reload) {
            itemRepository.getUserItems().addOnSuccessListener {
                items = it.toObjects(Item::class.java).toMutableList()
                counter.value = items.size
                adapter.setItems(items)
                reloadSelected = false
                reload = false
            }
        }
    }
}