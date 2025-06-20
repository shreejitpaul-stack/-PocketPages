package com.example.droiddevs

import android.content.Context
import androidx.lifecycle.LiveData
import java.util.Date

class NotionRepository(context: Context) {

    private val pageDao: PageDao = AppDatabase.getDatabase(context).pageDao()

    // --- Reactive Data Sources ---
    fun getActivePages(): LiveData<List<Page>> = pageDao.getActivePages()
    fun getDeletedPages(): LiveData<List<Page>> = pageDao.getDeletedPages()

    // --- Suspending Functions for one-off actions ---
    suspend fun getPage(pageId: String): Page? {
        return pageDao.getPageById(pageId)
    }

    suspend fun softDeletePage(pageId: String) {
        val page = pageDao.getPageById(pageId)
        page?.let {
            it.isDeleted = true
            it.deletedAt = Date()
            pageDao.upsertPage(it)
        }
    }

    suspend fun restorePage(pageId: String) {
        val page = pageDao.getPageById(pageId)
        page?.let {
            it.isDeleted = false
            it.deletedAt = null
            it.updatedAt = Date() // Mark as recently updated
            pageDao.upsertPage(it)
        }
    }

    suspend fun deletePagePermanently(pageId: String) {
        pageDao.deletePagePermanentlyById(pageId)
    }

    suspend fun savePage(page: Page) {
        pageDao.upsertPage(page)
    }
}