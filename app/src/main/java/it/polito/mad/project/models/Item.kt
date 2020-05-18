package it.polito.mad.project.models

import java.io.Serializable

data class Item (var id: String?) : Serializable {
    var title: String = ""
    var category: String = ""
    var subcategory : String = " "
    var categoryPos: Int = -1
    var price: String = ""
    var description: String = ""
    var expiryDate: String = ""
    var location: String = ""
    var imagePath: String? = ""
    var user: String=""

    constructor(id: String, title: String, category: String, subcategory: String, price: Double,
                description: String, expiryDate: String, location: String, imagePath: String? = "") : this(id){
        this.title = title
        this.category = category
        this.subcategory = subcategory
        this.price = price.toString()
        this.description = description
        this.expiryDate = expiryDate
        this.location = location
        this.imagePath = imagePath?: ""
    }

    constructor(): this(null)
}