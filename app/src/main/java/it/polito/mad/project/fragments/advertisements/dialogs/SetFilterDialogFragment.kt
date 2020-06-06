package it.polito.mad.project.fragments.advertisements.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import it.polito.mad.project.R
import it.polito.mad.project.enums.items.ItemFilter
import it.polito.mad.project.viewmodels.ItemViewModel
import java.util.*

class SetFilterDialogFragment : DialogFragment() {

    private lateinit var itemViewModel: ItemViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemViewModel = ViewModelProvider(requireActivity()).get(ItemViewModel::class.java)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater: LayoutInflater = requireActivity().layoutInflater
            val viewToInflate: View = inflater.inflate(R.layout.modal_filter, null)

            val spinner = viewToInflate.findViewById<Spinner>(R.id.filter_spinner)

            context?.let { context ->
                ArrayAdapter.createFromResource(context, R.array.item_filters, android.R.layout.simple_spinner_item)
                    .also {
                        adapter ->
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinner.adapter = adapter
                    }
            }
            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(viewToInflate)
                // Add action buttons
                .setPositiveButton("Set"
                ) { dialog, _ ->
                    // set up the spinner and pass the filter string
                    val filter = spinner.selectedItem.toString().toLowerCase(Locale.ROOT)
                    for (itemFilter in ItemFilter.values())
                         if (itemFilter.toString().toLowerCase(Locale.ROOT) == filter)
                             itemViewModel.onSaleItems.filter = itemFilter

                    Toast.makeText(requireContext(), "You set the filter to: $filter", Toast.LENGTH_SHORT).show()
                    dialog.cancel()
                }
                .setNegativeButton("Cancel"
                ) { dialog, _ ->
                    dialog.cancel()
                }

            if(spinner.selectedItem.toString().toLowerCase(Locale.ROOT) == "price"){
                Toast.makeText(requireContext(),
                    spinner.selectedItem.toString().toLowerCase(Locale.ROOT),
                    Toast.LENGTH_SHORT).show()
                val linearLayoutPrice = viewToInflate.findViewById<LinearLayout>(R.id.price_range)
                linearLayoutPrice.visibility = View.VISIBLE
            }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}

