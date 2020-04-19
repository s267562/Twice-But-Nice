package it.polito.mad.project.models

import java.io.Serializable

data class Item (val title: String) : Serializable {
    var description: String
    var category: String
    var price: Double = 0.0

    var expiryDate: String
    var location: String

    init {
        this.category = ""
        this.description = "Description"
        this.expiryDate = ""
        this.location = ""
    }

    constructor(title: String, category: String, price: Double) : this(title) {
        this.category = category
        this.price = price
    }
    constructor(title: String, category: String, price: Double, description: String) : this(title, category, price) {
        this.category = category
        this.price = price
        this.description = description
    }
}