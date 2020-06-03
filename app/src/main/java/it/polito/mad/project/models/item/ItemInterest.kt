package it.polito.mad.project.models.item

import java.io.Serializable

data class ItemInterest(var interested: Boolean): Serializable {
    var userId: String = ""

    constructor(): this(false)
}