package it.polito.mad.project.fragments.advertisements

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import it.polito.mad.project.adapters.ItemAdapter
import it.polito.mad.project.commons.CommonViewModel
import it.polito.mad.project.models.Item
import it.polito.mad.project.repositories.ItemRepository

class ItemViewModel : CommonViewModel() {
    var items: MutableList<Item> = mutableListOf()
    var adapter: ItemAdapter = ItemAdapter(items)
    var item = MutableLiveData<Item>()

    private val itemRepository = ItemRepository()
    init {
        loadItems()
    }

    fun saveItem(item: Item): Task<Void> {
        loader.value = true
        return itemRepository.saveItem(item)
            .addOnSuccessListener {
                this.item.value = item
                if (item.id == items.size) {
                    items.add(item.id, item)
                }
                loader.value = false
                error = false
            }.addOnFailureListener {
                loader.value = false
                error = true
            }
    }

    fun loadItem(id: Int) {
        if (id != item.value?.id) {
            item.value = null
            loader.value = true
            itemRepository.getUserItem(id)
                .addOnSuccessListener {
                    item.value = it.toObject(Item::class.java)
                    loader.value = false
                    error = false
                }.addOnFailureListener {
                    loader.value = false
                    error = true
                }
        }
    }

    private fun loadItems() {
        loader.value = true
        itemRepository.getUserItems()
            .addOnSuccessListener {
                items = it.toObjects(Item::class.java).toMutableList()
                loader.value = false
                error = false
            }.addOnFailureListener {
                loader.value = false
                error = true
            }
    }
}