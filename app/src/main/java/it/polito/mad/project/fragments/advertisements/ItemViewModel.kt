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
import it.polito.mad.project.models.UserInterest
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
    private var userInterest = UserInterest(false)

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

    fun isInterest(): Boolean {
        return userInterest.interest
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
            userInterest = UserInterest(false)
            pushLoader()
            itemRepository.getItem(id)
                .addOnSuccessListener { item ->
                    val localItem = item.toObject(Item::class.java) as Item
                    this.item.value = localItem

                    itemRepository.getUserInterest(itemRepository.getAuthUserId(), localItem.id!!)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                userInterest = it.result?.toObject(UserInterest::class.java)?:userInterest
                            }
                            popLoader()
                        }

                    if (localItem.imagePath.isNotBlank()) {
                        val localFile = File.createTempFile(localItem.id!!,".jpg")
                        itemRepository.getItemPhoto(localFile, localItem).addOnSuccessListener {
                            itemPhoto.value =  BitmapFactory.decodeFile(localFile.path)
                            this.item.value!!.imagePath = localFile.path
                        }
                    }
                    loadInterestedUsers()
                    error = false
                }.addOnFailureListener {
                    popLoader()
                    error = true
                }
        }
    }

    fun loadItemsOnSale() {
        pushLoader()
        itemRepository.getAvailableItems()
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

    fun addUserToItem(userInterest: UserInterest): Task<Void> {
        pushLoader()
        userInterest.userId = itemRepository.getAuthUserId()
        return itemRepository.saveUserInterest(userInterest.userId, item.value!!.id!!, userInterest).addOnSuccessListener {
            popLoader()
            error = false
        }
        .addOnFailureListener {
            popLoader()
            error = true
        }
    }

    private fun loadInterestedUsers() {
        //if (id != item.value?.id){
            pushLoader()
            itemRepository.getItemInterestedUserIds(item.value!!.id!!).addOnSuccessListener { userIdsSnap ->
                val itemInterests = userIdsSnap.toObjects(UserInterest::class.java).toMutableList()
                if (itemInterests.size > 0) {
                    val userIds = itemInterests.map { interest -> interest.userId }.toMutableList()
                    itemRepository.getItemInterestedUsers(userIds).addOnSuccessListener {
                        users = it.toObjects(User::class.java).toMutableList()
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
    //}
}