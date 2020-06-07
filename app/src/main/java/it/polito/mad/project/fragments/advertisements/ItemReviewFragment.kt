package it.polito.mad.project.fragments.advertisements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import it.polito.mad.project.R
import it.polito.mad.project.models.review.Review
import it.polito.mad.project.utils.Util.Companion.hideKeyboard
import it.polito.mad.project.viewmodels.ItemViewModel
import kotlinx.android.synthetic.main.fragment_item_details.*
import kotlinx.android.synthetic.main.fragment_item_review.*

class ItemReviewFragment : Fragment() {

    private lateinit var itemViewModel: ItemViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemViewModel = ViewModelProvider(requireActivity()).get(ItemViewModel::class.java)
    }

    override fun onStart() {
        super.onStart()
        (activity as AppCompatActivity?)?.supportActionBar?.show()

        itemViewModel.loader.observe(viewLifecycleOwner, Observer {
            if (itemViewModel.isNotLoading()) {
                if (itemViewModel.error) {
                    Toast.makeText(context, "Error on item loading", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }
        })

        itemViewModel.item.data.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                reviewTitle.text = it.title
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        itemViewModel.resetLocalData()
        return inflater.inflate(R.layout.fragment_item_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val itemId = arguments?.getString("ItemId")
        itemViewModel.loadItem(itemId!!)

        publishReview.setOnClickListener {
            val review = Review(
                description.text.toString(),
                ratingBar.rating
            )
            itemViewModel.setReview(review)

            findNavController().popBackStack()
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        hideKeyboard(activity)
    }
}