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
import it.polito.mad.project.enums.items.ItemStatus
import it.polito.mad.project.models.user.User
import it.polito.mad.project.viewmodels.ItemViewModel

class SetBuyerDialogFragment() : DialogFragment(), AdapterView.OnItemSelectedListener {
    private lateinit var itemViewModel: ItemViewModel
    private var buyer: User? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemViewModel = ViewModelProvider(requireActivity()).get(ItemViewModel::class.java)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { it ->
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater: LayoutInflater = requireActivity().layoutInflater
            val viewToInflate: View = inflater.inflate(R.layout.modal_seller, null)

            val spinner = viewToInflate.findViewById<Spinner>(R.id.users_spinner)

            context?.let { context ->
                ArrayAdapter(context, android.R.layout.simple_spinner_item, itemViewModel.interestedUsers.users.map { user -> user.nickname })
                    .also {
                        adapter ->
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinner.adapter = adapter
                        if (itemViewModel.interestedUsers.users.size > 0)
                            buyer = itemViewModel.interestedUsers.users[0]
                    }
            }
            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(viewToInflate)
                // Add action buttons
                .setPositiveButton("SAVE"
                ) { dialog, _ ->
                    if (buyer != null) {
                        itemViewModel.item.localData?.buyerId = buyer!!.id
                        itemViewModel.item.localData?.buyerNickname = buyer!!.nickname
                    }
                    dialog.cancel()
                }
                .setNegativeButton("Cancel"
                ) { dialog, _ ->
                    itemViewModel.item.localData?.status = ItemStatus.Available.toString()
                    itemViewModel.item.localData?.statusPos = 0
                    dialog.cancel()
                }
            builder.create()

        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        // Nothing to do
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        buyer = itemViewModel.interestedUsers.users[pos]
    }
}

