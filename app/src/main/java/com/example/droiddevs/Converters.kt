package com.example.droiddevs

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromBlockListJson(json: String?): MutableList<Block>? {
        if (json == null) {
            return null
        }
        // This type token remains correct as Gson can deserialize into the data class
        val type = object : TypeToken<MutableList<Block>>() {}.type
        return gson.fromJson(json, type)
    }

    @TypeConverter
    fun toBlockListJson(blocks: MutableList<Block>?): String? {
        if (blocks == null) {
            return null
        }
        return gson.toJson(blocks)
    }

    @TypeConverter
    fun fromStringListJson(json: String?): List<String>? {
        if (json == null) {
            return null
        }
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type)
    }

    @TypeConverter
    fun toStringListJson(tags: List<String>?): String? {
        if (tags == null) {
            return null
        }
        return gson.toJson(tags)
    }
}