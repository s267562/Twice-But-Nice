package it.polito.mad.project.fragments.advertisements

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.polito.mad.project.adapters.ItemAdapter
import it.polito.mad.project.models.Item

class ItemListViewModel : ViewModel() {
    var items: MutableList<Item> = mutableListOf()
    var index : MutableLiveData<Int> = MutableLiveData(0)
    var adapter: ItemAdapter = ItemAdapter(items)
}