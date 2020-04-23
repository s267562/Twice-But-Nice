package it.polito.mad.project.activities.main.ui.advertisements

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson

import it.polito.mad.project.R
import it.polito.mad.project.models.Item
import kotlinx.android.synthetic.main.fragment_show_advertisement.*

class ShowItemFragment : Fragment() {

//    private lateinit var viewModel: ShowItemViewModel
    private lateinit var item: Item

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        viewModel = ViewModelProvider(this).get(ShowItemViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_show_advertisement, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        item = Gson().fromJson(arguments?.getString("item"), Item::class.java)
        item_title.text = item.title
    }
}
