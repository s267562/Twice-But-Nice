package it.polito.mad.project.viewmodels

import android.graphics.BitmapFactory
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import it.polito.mad.project.adapters.items.BoughtItemAdapter
import it.polito.mad.project.adapters.items.MyItemAdapter
import it.polito.mad.project.adapters.items.OnSaleItemAdapter
import it.polito.mad.project.adapters.users.InterestedUserAdapter
import it.polito.mad.project.commons.viewmodels.LoadingViewModel
import it.polito.mad.project.models.item.Item
import it.polito.mad.project.models.item.ItemDetail
import it.polito.mad.project.models.item.ItemInterest
import it.polito.mad.project.models.item.ItemList
import it.polito.mad.project.models.review.Review
import it.polito.mad.project.models.user.User
import it.polito.mad.project.models.user.UserList
import it.polito.mad.project.repositories.ItemRepository
import java.io.File

class ItemViewModel : LoadingViewModel() {

    private val itemRepository = ItemRepository()

    // User items
    val myItems = ItemList(
        MyItemAdapter(
            mutableListOf()
        )
    )

    // On Sale items
    val onSaleItems = ItemList(
        OnSaleItemAdapter(
            mutableListOf(),
            "onSaleItems"
        )
    )

    // Interested users
    val interestedUsers = UserList(
        InterestedUserAdapter(
            mutableListOf()
        )
    )

    //Bought Items
    val boughtItems = ItemList(
        BoughtItemAdapter(
            mutableListOf()
        )
    )

    //Items of Interest
    val interestedItems = ItemList(
        OnSaleItemAdapter(
            mutableListOf(),
            "interestedItems"
        )
    )

    // Single item detail loaded
    val item = ItemDetail()

    init {
        loadItems()
    }

    private fun loadItems() {
        pushLoader()
        val userId = itemRepository.getAuthUserId()
        itemRepository.getItemsByUserId(userId)
            .addOnSuccessListener { it1 ->
                myItems.items.clear()
                myItems.items.addAll(it1.toObjects(Item::class.java))
                loadItemsOnSale()
                loadItemsBought()
                loadInterestedItems()
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
                onSaleItems.items.clear()
                onSaleItems.items.addAll(it.toObjects(Item::class.java).subtract(myItems.items.toList()))
                popLoader()
                error = false
            }.addOnFailureListener {
                popLoader()
                error = true
            }
    }

    fun loadItemsBought(){
        pushLoader()
        val userId = itemRepository.getAuthUserId()
        itemRepository.getBoughtItems(userId)
            .addOnSuccessListener {
                // Items on sale are all items sub user items
                boughtItems.items.clear()
                boughtItems.items.addAll(it.toObjects(Item::class.java))
                popLoader()
                error = false
            }.addOnFailureListener {
                popLoader()
                error = true
            }
    }

    fun loadInterestedItems() {
        pushLoader()
        interestedItems.items.clear()
        itemRepository.getInterestedItemsIDs()
            .addOnSuccessListener { itemIdsSnap ->
                val itemIds = itemIdsSnap.toObjects(ItemInterest::class.java).map { interest -> interest.itemId }
                if (itemIds.isNotEmpty()) {
                    itemRepository.getItemsByItemsIds(itemIds).addOnSuccessListener {
                        interestedItems.items.clear()
                        interestedItems.items.addAll(it.toObjects(Item::class.java))
                        popLoader()
                        error = false
                    }.addOnFailureListener {
                        popLoader()
                        error = true
                    }
                } else {
                    popLoader()
                }
            }
            .addOnFailureListener{
                popLoader()
                error = true
            }
    }

    /** ---------------------------------- SINGLE ITEM METHODS -------------------------------- **/

    /**
     * Method for insert/update single item
     */
    fun saveItem(item: Item): Task<Void> {
        val isNewItem = item.id == null
        if (isNewItem) {
            item.ownerId = itemRepository.getAuthUserId()
            item.id = "${item.ownerId}-${myItems.items.size}"
        }
        pushLoader()
        return itemRepository.saveItem(item)
            .addOnSuccessListener {
                this.item.data.value = item
                if (isNewItem) {
                    myItems.items.add(item)
                } else {
                    var pos = -1
                    myItems.items.forEachIndexed {index, i ->
                        if (i.id == item.id) pos = index
                    }
                    myItems.items[pos] = item
                }
                popLoader()
                error = false
            }.addOnFailureListener {
                popLoader()
                error = true
            }
    }

    fun loadItem(id: String) {
        item.data.value = null
        pushLoader()
        itemRepository.getItem(id)
            .addOnSuccessListener { it ->
                val localItem = it.toObject(Item::class.java) as Item
                item.data.value = localItem
                loadItemInterest(localItem.id!!)
                loadInterestedUsers()
                popLoader()
                loadItemImage(localItem.id!!, localItem.imagePath)
                error = false
            }.addOnFailureListener {
                popLoader()
                error = true
            }
    }

    private fun loadItemInterest(itemId: String) {
        pushLoader()
        itemRepository.getItemInterest(itemRepository.getAuthUserId(), itemId)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val interest = it.result?.toObject(ItemInterest::class.java)
                    if (interest != null) {
                        item.interest.interested = interest.interested
                        item.interest.userId = interest.userId
                    } else {
                        item.interest.interested = false
                    }
                }
                popLoader()
            }
    }

    private fun loadItemImage(id: String, imagePath: String) {
        if (imagePath.isNotBlank()) {
            val image = BitmapFactory.decodeFile(imagePath)
            if (image != null) {
                item.image.value = image
            } else {
                val localFile = File.createTempFile(id,".jpg")
                itemRepository.getItemImage(id, localFile).addOnSuccessListener {
                    item.image.value =  BitmapFactory.decodeFile(localFile.path)
                    item.data.value!!.imagePath = localFile.path
                }
            }

        }
    }

    /** Add listener to the current item docuement **/
    fun listenToChanges(): ListenerRegistration {
        return itemRepository.getItemDocument(item.data.value!!.id!!)
            .addSnapshotListener { itemSnapshot, e ->
                // if there's an exception, we have to skip
                if (e != null) {
                    return@addSnapshotListener
                }
                // if we are here, this means we didn't meet any exception
                if (itemSnapshot != null) {
                    item.data.value = itemSnapshot.toObject(Item::class.java)!!
                }
            }
    }

    fun updateItemInterest():Task<Void> {
        pushLoader()
        val interest =  item.interest
        interest.interested = !interest.interested
        interest.userId = itemRepository.getAuthUserId()
        interest.itemId = item.data.value!!.id!!
        return itemRepository.saveItemInterest(interest.userId, item.data.value!!.id!!, interest)
            .addOnSuccessListener {
                popLoader()
                error = false
            }
            .addOnFailureListener {
                popLoader()
                error = true
            }
    }

    fun loadInterestedUsers(): Task<QuerySnapshot> {
        pushLoader()
        interestedUsers.users.clear()
        return itemRepository.getInterestedUserIds(item.data.value!!.id!!).addOnSuccessListener { userIdsSnap ->
            val userIds = userIdsSnap.toObjects(ItemInterest::class.java).map { interest -> interest.userId }
            if (userIds.isNotEmpty()) {
                itemRepository.getUsersByUserIds(userIds).addOnSuccessListener {
                    interestedUsers.users.addAll(it.toObjects(User::class.java))
                    popLoader()
                    error = false
                }.addOnFailureListener{
                    popLoader()
                    error = true
                }
            } else {
                popLoader()
            }
        }.addOnFailureListener {
            popLoader()
            error=true
        }
    }

    fun setReview(review: Review) {
        item.data.value!!.review = review
        pushLoader()
        itemRepository.saveItem(item.data.value!!)
            .addOnSuccessListener {
                popLoader()
                error = false
            }.addOnFailureListener {
                popLoader()
                error = true
            }
    }
}