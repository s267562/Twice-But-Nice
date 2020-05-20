package it.polito.mad.project.fragments.advertisements

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.ListenerRegistration
import it.polito.mad.project.adapters.ItemAdapter
import it.polito.mad.project.adapters.ItemOnSaleAdapter
import it.polito.mad.project.adapters.UserAdapter
import it.polito.mad.project.commons.CommonViewModel
import it.polito.mad.project.models.Item
import it.polito.mad.project.models.User
import it.polito.mad.project.repositories.ItemRepository
import java.io.File

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
    var itemPhoto = MutableLiveData<Bitmap>()

    //user interested to item
    var users: MutableList<User> = mutableListOf()
    var adapterUser: UserAdapter = UserAdapter(users)

    init {
        loadItems()
    }

    fun listenToChanges(): ListenerRegistration {
        return itemRepository.getItemDocument(item.value!!.id!!)
            .addSnapshotListener { itemSnapshot, e ->
                // if there's an exception, we have to skip
                if (e != null) {
                    return@addSnapshotListener
                }
                // if we are here, this means we didn't meet any exception
                if (itemSnapshot != null) {
                    item.value = itemSnapshot.toObject(Item::class.java)
                }
            }
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
            itemPhoto.value = null
            pushLoader()
            itemRepository.getItem(id)
                .addOnSuccessListener {
                    val localItem = it.toObject(Item::class.java)
                    item.value = localItem

                    if (localItem!!.imagePath.isNotBlank()) {
                        val localFile = File.createTempFile(localItem.id!!,".jpg")
                        itemRepository.getItemPhoto(localFile, localItem).addOnSuccessListener {
                            itemPhoto.value =  BitmapFactory.decodeFile(localFile.path)
                            item.value!!.imagePath = localFile.path
                        }
                    }
                    loadInterestedUsers()
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

    private fun loadInterestedUsers() {
        //if (id != item.value?.id){
            pushLoader()
            itemRepository.getItemUsers(item.value!!.id.toString()).addOnSuccessListener {
                users = it.toObjects(User::class.java).toMutableList()
                popLoader()
                error = false
            }.addOnFailureListener{
                popLoader()
                error=true
            }
        }
    //}
}