package it.polito.mad.project.viewmodels

import com.google.android.gms.tasks.Task
import it.polito.mad.project.commons.viewmodels.LoadingViewModel
import it.polito.mad.project.models.review.Review
import it.polito.mad.project.repositories.ReviewRepository

class ReviewViewModel : LoadingViewModel() {

    private val reviewRepository = ReviewRepository()

    fun saveReview(review: Review): Task<Void> {
        pushLoader()
        return reviewRepository.saveReview(review)
    }

}