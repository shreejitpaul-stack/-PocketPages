package com.example.droiddevs

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "pages")
data class Page(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    var title: String = "",
    var blocks: MutableList<Block> = mutableListOf(),
    val createdAt: Date = Date(),
    var updatedAt: Date = Date(),
    var isDeleted: Boolean = false,
    var deletedAt: Date? = null,
    var parentId: String? = null,
    var isPublic: Boolean = false,
    var tags: List<String> = emptyList(),
    var cloudId: String? = null,
    var lastSyncedAt: Date? = null,
    var needsSync: Boolean = false,
    // Add cursor state tracking
    var titleCursorPosition: Int = -1,
    var focusedBlockId: String? = null,
    var focusedBlockCursorPosition: Int = -1
) {
    fun toFirebaseMap(): Map<String, Any?> {
        return mapOf(
            "id" to id, "title" to title, "blocks" to blocks.map { it.toFirebaseMap() },
            "createdAt" to createdAt, "updatedAt" to updatedAt, "isDeleted" to isDeleted, "deletedAt" to deletedAt,
            "parentId" to parentId, "isPublic" to isPublic, "tags" to tags
        )
    }
    companion object {
        fun fromFirebaseMap(data: Map<String, Any>): Page {
            return Page(
                id = data["id"] as String, title = data["title"] as String,
                blocks = (data["blocks"] as? List<Map<String, Any>>)?.map { Block.fromFirebaseMap(it) }?.toMutableList() ?: mutableListOf(),
                createdAt = data["createdAt"] as Date, updatedAt = data["updatedAt"] as Date,
                parentId = data["parentId"] as? String, isPublic = data["isPublic"] as Boolean,
                tags = data["tags"] as? List<String> ?: emptyList(), isDeleted = data["isDeleted"] as? Boolean ?: false,
                deletedAt = data["deletedAt"] as? Date
            )
        }
    }
}


data class Block(
    val id: String = UUID.randomUUID().toString(),
    var type: BlockType = BlockType.TEXT,
    var content: String = "",

    val properties: Map<String, Any> = mapOf(),
    val createdAt: Date = Date(),
    var updatedAt: Date = Date(),
    var children: MutableList<Block> = mutableListOf(),
    var needsSync: Boolean = false
) {
    fun toFirebaseMap(): Map<String, Any> {
        return mapOf(
            "id" to id, "type" to type.name, "content" to content, "properties" to properties,
            "createdAt" to createdAt, "updatedAt" to updatedAt, "children" to children.map { it.toFirebaseMap() }
        )
    }

    companion object {
        fun fromFirebaseMap(data: Map<String, Any>): Block {
            return Block(
                id = data["id"] as String,
                type = BlockType.valueOf(data["type"] as String),
                content = data["content"] as String,
                properties = (data["properties"] as Map<String, Any>), // No longer needs to be mutable
                createdAt = data["createdAt"] as Date,
                updatedAt = data["updatedAt"] as Date,
                children = (data["children"] as? List<Map<String, Any>>)?.map { fromFirebaseMap(it) }?.toMutableList() ?: mutableListOf()
            )
        }
    }
}

// (The rest of the file remains the same)
enum class BlockType { TEXT, HEADING_1, HEADING_2, HEADING_3, BULLET_LIST, NUMBERED_LIST, TODO, QUOTE, CODE, DIVIDER, IMAGE, TABLE, CALENDAR, DATABASE }
enum class SyncStatus { OFFLINE, SYNCING, SYNCED, ERROR }
data class User( val id: String, val email: String, val name: String, val profileImageUrl: String? = null, val createdAt: Date = Date(), var firebaseUid: String? = null ) { fun toFirebaseMap(): Map<String, Any?> { return mapOf( "id" to id, "email" to email, "name" to name, "profileImageUrl" to profileImageUrl, "createdAt" to createdAt ) } companion object { fun fromFirebaseMap(data: Map<String, Any>): User { return User( id = data["id"] as String, email = data["email"] as String, name = data["name"] as String, profileImageUrl = data["profileImageUrl"] as? String, createdAt = data["createdAt"] as Date ) } } }
data class Database( val id: String = UUID.randomUUID().toString(), var title: String = "", var columns: MutableList<DatabaseColumn> = mutableListOf(), var rows: MutableList<DatabaseRow> = mutableListOf(), val createdAt: Date = Date(), var updatedAt: Date = Date(), var needsSync: Boolean = false )
data class DatabaseColumn( val id: String = UUID.randomUUID().toString(), var name: String = "", var type: ColumnType = ColumnType.TEXT, var properties: MutableMap<String, Any> = mutableMapOf() )
data class DatabaseRow( val id: String = UUID.randomUUID().toString(), var cells: MutableMap<String, Any> = mutableMapOf(), val createdAt: Date = Date(), var updatedAt: Date = Date() )
enum class ColumnType { TEXT, NUMBER, DATE, CHECKBOX, SELECT, MULTI_SELECT, PERSON, FILES, RELATION, FORMULA }
data class Reminder( val id: String = UUID.randomUUID().toString(), val pageId: String, val blockId: String? = null, var title: String = "", var description: String = "", var reminderTime: Date, var isCompleted: Boolean = false, var repeatInterval: RepeatInterval? = null, val createdAt: Date = Date(), var needsSync: Boolean = false )
enum class RepeatInterval { DAILY, WEEKLY, MONTHLY, YEARLY }