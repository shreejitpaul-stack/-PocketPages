package com.example.droiddevs

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PageDao {
    // Get all pages that are NOT deleted
    @Query("SELECT * FROM pages WHERE isDeleted = 0 ORDER BY updatedAt DESC")
    fun getActivePages(): LiveData<List<Page>>

    // Get all pages that ARE deleted (for the bin)
    @Query("SELECT * FROM pages WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getDeletedPages(): LiveData<List<Page>>

    @Query("SELECT * FROM pages WHERE id = :pageId")
    suspend fun getPageById(pageId: String): Page?

    // This single function handles create, update, soft-delete, and restore
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPage(page: Page)

    // This is ONLY for permanent deletion
    @Query("DELETE FROM pages WHERE id = :pageId")
    suspend fun deletePagePermanentlyById(pageId: String)
}