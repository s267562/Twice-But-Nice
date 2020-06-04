package it.polito.mad.project.models.review

import androidx.lifecycle.MutableLiveData

class ReviewDetail {
    // Textual info
    val data = MutableLiveData<Review>()
    var localData: Review? = null
}