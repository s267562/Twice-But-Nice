package it.polito.mad.project.models.review

import android.icu.text.CaseMap
import java.io.Serializable

class Review() : Serializable {
    var description: String = ""
    var rating: Float = 0F


    constructor(description: String, rating: Float): this(){
        this.description = description
        this.rating = rating
    }

}