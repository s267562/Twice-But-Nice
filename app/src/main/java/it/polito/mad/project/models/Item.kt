package it.polito.mad.project.models

import java.io.Serializable

data class Item (val id: Int) : Serializable {
    var title: String = ""
    var category: String = ""
    var subcategory : String = " "
    var categoryPos: Int = -1
    var price: String = ""
    var description: String = ""
    var expiryDate: String = ""
    var location: String = ""
    var imagePath: String? = ""
}