package com.example.data.local.db

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Instant?): Long? {
        return date?.toEpochMilli()
    }
    
    @TypeConverter
    fun fromLocalDateTime(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneOffset.UTC) }
    }
    
    @TypeConverter
    fun dateToLocalDateTime(date: LocalDateTime?): Long? {
        return date?.toInstant(ZoneOffset.UTC)?.toEpochMilli()
    }
}
