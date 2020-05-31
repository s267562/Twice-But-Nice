package it.polito.mad.project.fragments.advertisements

import android.app.Application
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import it.polito.mad.project.R
import it.polito.mad.project.fragments.advertisements.StringGlobal.Companion.globalFilter
import java.util.*

class DialogViewFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater: LayoutInflater = requireActivity().layoutInflater
            val viewToInflate: View = inflater.inflate(R.layout.modal_filter, null)

            val spinner = viewToInflate.findViewById<Spinner>(R.id.filter_spinner)

            context?.let {
                ArrayAdapter.createFromResource(it, R.array.params, android.R.layout.simple_spinner_item)
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
                    globalFilter = spinner.selectedItem.toString().toLowerCase(Locale.ROOT)
                    Toast.makeText(requireContext(), "You set the filter to: $globalFilter", Toast.LENGTH_SHORT).show()
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

class StringGlobal: Application() {
    companion object {
        var globalFilter: String = "Title"
    }
}