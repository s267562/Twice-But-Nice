package it.polito.mad.project.models

import java.io.Serializable

data class ItemInterest(var interest: Boolean): Serializable {
    var userId: String = ""

    constructor(): this(false)
}