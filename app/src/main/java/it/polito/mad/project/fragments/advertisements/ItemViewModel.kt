package it.polito.mad.project.fragments.advertisements

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import it.polito.mad.project.adapters.ItemAdapter
import it.polito.mad.project.adapters.ItemOnSaleAdapter
import it.polito.mad.project.commons.CommonViewModel
import it.polito.mad.project.models.Item
import it.polito.mad.project.repositories.ItemRepository

class ItemViewModel : CommonViewModel() {
    private val itemRepository = ItemRepository()

    // User items
    var items: MutableList<Item> = mutableListOf()
    var adapter: ItemAdapter = ItemAdapter(items)

    // All items
    var itemsOnSale: MutableList<Item> = mutableListOf()
    var adapterOnSale: ItemOnSaleAdapter = ItemOnSaleAdapter(itemsOnSale)

    // Single item detail loaded
    var item = MutableLiveData<Item>()

    init {
        loadItems()
    }

    private fun loadItems() {
        loader.value = true
        val userId = itemRepository.getAuthUserId()
        itemRepository.getUserItems(userId)
            .addOnSuccessListener { it1 ->
                items = it1.toObjects(Item::class.java).toMutableList()
                loadItemsOnSale()
            }.addOnFailureListener {
                loader.value = false
                error = true
            }
    }

    fun saveItem(item: Item): Task<Void> {
        val isNewItem = item.id == null
        if (isNewItem) {
            item.user = itemRepository.getAuthUserId()
            item.id = "${item.user}-${items.size}"
        }
        loader.value = true
        return itemRepository.saveItem(item)
            .addOnSuccessListener {
                this.item.value = item
                if (isNewItem) {
                    items.add(item)
                }
                loader.value = false
                error = false
            }.addOnFailureListener {
                loader.value = false
                error = true
            }
    }

    fun loadItem(id: String) {
        if (id != item.value?.id) {
            item.value = null
            loader.value = true
            itemRepository.getItem(id)
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

    fun loadItemsOnSale() {
        loader.value = true
        itemRepository.getAllItems()
            .addOnSuccessListener {
                itemsOnSale = it.toObjects(Item::class.java).subtract(items.toList()).toMutableList()
                loader.value = false
                error = false
            }.addOnFailureListener {
                loader.value = false
                error = true
            }
    }
}