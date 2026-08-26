package com.xiao.idealistachallenge.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AppViewModelFactory<T : ViewModel>(
    private val viewModelClass: Class<T>,
    private val creator: () -> T,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <VM : ViewModel> create(modelClass: Class<VM>): VM {
        require(viewModelClass.isAssignableFrom(modelClass)) {
            "Unsupported ViewModel class: ${modelClass.name}"
        }
        return creator() as VM
    }
}
