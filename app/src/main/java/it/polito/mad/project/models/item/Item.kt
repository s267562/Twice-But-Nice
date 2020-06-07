package it.polito.mad.project.models.item

import it.polito.mad.project.models.review.Review
import java.io.Serializable

data class Item (var id: String?) : Serializable {
    var title: String = ""
    var category: String = ""
    var subcategory : String = " "
    var status : String = ""
    var statusPos: Int = -1
    var categoryPos: Int = -1
    var subcategoryPos: Int = -1
    var price: String = ""
    var description: String = ""
    var expiryDate: String = ""
    var location: String = ""
    var imagePath: String = ""
    var ownerId: String=""
    var buyerId: String=""
    var buyerNickname: String=""
    var ownerNickname: String=""
    var review: Review? = null

    constructor(id: String, title: String, category: String, subcategory: String, status: String, price: Double,
                description: String, expiryDate: String, location: String, imagePath: String? = "") : this(id){
        this.title = title
        this.category = category
        this.subcategory = subcategory
        this.status = status
        this.price = price.toString()
        this.description = description
        this.expiryDate = expiryDate
        this.location = location
        this.imagePath = imagePath?: ""
    }

    constructor(): this(null)
    constructor(itemId: Nothing?, nickname: String) : this(null){
        this.ownerNickname = nickname;
    }
}