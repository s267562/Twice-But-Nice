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
        pushLoader()
        val userId = itemRepository.getAuthUserId()
        itemRepository.getUserItems(userId)
            .addOnSuccessListener { it1 ->
                items = it1.toObjects(Item::class.java).toMutableList()
                loadItemsOnSale()
                popLoader()
                error = false
            }.addOnFailureListener {
                popLoader()
                error = true
            }
    }

    fun saveItem(item: Item): Task<Void> {
        val isNewItem = item.id == null
        if (isNewItem) {
            item.user = itemRepository.getAuthUserId()
            item.id = "${item.user}-${items.size}"
        }
        pushLoader()
        return itemRepository.saveItem(item)
            .addOnSuccessListener {
                this.item.value = item
                if (isNewItem) {
                    items.add(item)
                }
                popLoader()
                error = false
            }.addOnFailureListener {
                popLoader()
                error = true
            }
    }

    fun loadItem(id: String) {
        if (id != item.value?.id) {
            item.value = null
            pushLoader()
            itemRepository.getItem(id)
                .addOnSuccessListener {
                    item.value = it.toObject(Item::class.java)
                    popLoader()
                    error = false
                }.addOnFailureListener {
                    popLoader()
                    error = true
                }
        }
    }

    fun loadItemsOnSale() {
        pushLoader()
        itemRepository.getAllItems()
            .addOnSuccessListener {
                // Items on sale are all items sub user items
                itemsOnSale = it.toObjects(Item::class.java).subtract(items.toList()).toMutableList()
                popLoader()
                error = false
            }.addOnFailureListener {
                popLoader()
                error = true
            }
    }
}