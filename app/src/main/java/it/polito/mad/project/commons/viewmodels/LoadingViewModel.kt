package it.polito.mad.project.commons.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

open class LoadingViewModel: ViewModel() {
    val loader = MutableLiveData(0)
    var error: Boolean = false

    open fun pushLoader() {
        loader.value = (loader.value?:0) + 1
    }

    open fun popLoader() {
        loader.value = (loader.value?:0) - 1
    }

    open fun isNotLoading(): Boolean {
        return loader.value?:0 <= 0
    }
}