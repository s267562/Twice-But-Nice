package it.polito.mad.project.models.review

import android.icu.text.CaseMap
import java.io.Serializable

class Review(var itemId: String?) : Serializable {
    var title: String = ""
    var description: String = ""
    var rating: Float = 0F
    var ownerId: String = ""
    var buyerId: String = ""

    constructor(itemId: String, title: String, description: String, rating: Float, ownerId: String, buyerId: String): this(itemId) {
        this.title = title
        this.description = description
        this.rating = rating
        this.ownerId = ownerId
        this.buyerId = buyerId
    }

    constructor(): this(null)
}