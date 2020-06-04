package it.polito.mad.project.repositories

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import it.polito.mad.project.models.review.Review

class ReviewRepository {
    private var database = FirebaseFirestore.getInstance()
    private var auth = FirebaseAuth.getInstance()

    fun saveReview(review: Review): Task<Void> {
        return database.collection("reviews").document(review.itemId!!).set(review)
    }

    fun getReviewById(reviewId: String): Task<DocumentSnapshot> {
        return database.collection("reviews").document(reviewId).get()
    }

}