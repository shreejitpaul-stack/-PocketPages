package com.example.droiddevs

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class DeletedViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NotionRepository(application)
    val deletedPages: LiveData<List<Page>> = repository.getDeletedPages()

    fun restorePage(pageId: String) {
        viewModelScope.launch { repository.restorePage(pageId) }
    }

    fun deletePagePermanently(pageId: String) {
        viewModelScope.launch { repository.deletePagePermanently(pageId) }
    }
}