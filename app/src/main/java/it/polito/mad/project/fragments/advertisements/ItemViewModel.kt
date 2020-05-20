package it.polito.mad.project.fragments.advertisements

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.messaging.FirebaseMessaging
import it.polito.mad.project.adapters.ItemAdapter
import it.polito.mad.project.adapters.ItemOnSaleAdapter
import it.polito.mad.project.adapters.UserAdapter
import it.polito.mad.project.commons.LoadingViewModel
import it.polito.mad.project.models.Item
import it.polito.mad.project.models.User
import it.polito.mad.project.models.ItemInterest
import it.polito.mad.project.repositories.ItemRepository
import it.polito.mad.project.services.MessageService
import java.io.File

class ItemViewModel : LoadingViewModel() {

    private val itemRepository = ItemRepository()
    private val messagingService =  MessageService()

    // User items
    var items: MutableList<Item> = mutableListOf()
    var adapter: ItemAdapter = ItemAdapter(items)

    // All items
    var itemsOnSale: MutableList<Item> = mutableListOf()
    var adapterOnSale: ItemOnSaleAdapter = ItemOnSaleAdapter(itemsOnSale)

    // Single item detail
    var item = MutableLiveData<Item>()
    var itemPhoto = MutableLiveData<Bitmap>()
    var itemInterest = ItemInterest(false)

    //user interested to item
    var users: MutableList<User> = mutableListOf()
    var adapterUser: UserAdapter = UserAdapter(users)

    init {
        loadItems()
    }

    fun loadItems() {
        pushLoader()
        val userId = itemRepository.getAuthUserId()
        itemRepository.getItemsByUserId(userId)
            .addOnSuccessListener { it1 ->
                items.clear()
                items.addAll(it1.toObjects(Item::class.java))
                loadItemsOnSale()
                popLoader()
                error = false
            }.addOnFailureListener {
                popLoader()
                error = true
            }
    }

    fun loadItemsOnSale() {
        pushLoader()

        itemRepository.getAvailableItems()
            .addOnSuccessListener {
                // Items on sale are all items sub user items
                itemsOnSale.clear()
                itemsOnSale.addAll(it.toObjects(Item::class.java).subtract(items.toList()))
                popLoader()
                error = false
            }.addOnFailureListener {
                popLoader()
                error = true
            }
    }

    /** ---------------------------------- SINGLE ITEM METHODS -------------------------------- **/

    /**
     * Method for insert/update single item
     */
    fun saveItem(item: Item, position: Int): Task<Void> {
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
                } else {
                    items[position] = item
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
            itemInterest = ItemInterest(false)
            pushLoader()
            itemRepository.getItem(id)
                .addOnSuccessListener { it ->
                    val localItem = it.toObject(Item::class.java) as Item
                    item.value = localItem
                    loadItemInterest(localItem.id!!)
                    popLoader()

                    loadItemImage(localItem.id!!, localItem.imagePath)
                    error = false
                }.addOnFailureListener {
                    popLoader()
                    error = true
                }
        }
    }

    private fun loadItemInterest(itemId: String) {
        pushLoader()
        itemRepository.getItemInterest(itemRepository.getAuthUserId(), itemId)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    itemInterest = it.result?.toObject(ItemInterest::class.java)?:itemInterest
                }
                popLoader()
            }
    }

    private fun loadItemImage(id: String, imagePath: String) {
        if (imagePath.isNotBlank()) {
            val image = BitmapFactory.decodeFile(imagePath)
            if (image != null) {
                itemPhoto.value = image
            } else {
                val localFile = File.createTempFile(id,".jpg")
                itemRepository.getItemImage(id, localFile).addOnSuccessListener {
                    itemPhoto.value =  BitmapFactory.decodeFile(localFile.path)
                    item.value!!.imagePath = localFile.path
                }
            }

        }
    }

    /** Add listener to the current item docuement **/
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

    fun updateItemInterest():Task<Void> {
        pushLoader()
        itemInterest.interest = !itemInterest.interest
        itemInterest.userId = itemRepository.getAuthUserId()
        return itemRepository.saveItemInterest(itemInterest.userId, item.value!!.id!!, itemInterest)
            .addOnSuccessListener {
                popLoader()
                error = false
            }
            .addOnFailureListener {
                popLoader()
                error = true
            }
    }

    fun loadInterestedUsers() {
        pushLoader()
        users.clear()
        itemRepository.getInterestedUserIds(item.value!!.id!!).addOnSuccessListener { userIdsSnap ->
            val userIds = userIdsSnap.toObjects(ItemInterest::class.java).map { interest -> interest.userId }
            if (userIds.isNotEmpty()) {
                itemRepository.getUsersByUserIds(userIds).addOnSuccessListener {
                    users.addAll(it.toObjects(User::class.java))
                    popLoader()
                    error = false
                }.addOnFailureListener{
                    popLoader()
                    error=true
                }
            } else {
                popLoader()
            }
        }.addOnFailureListener {
            popLoader()
            error=true
        }
    }
}