package com.example.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.dao.*
import com.example.data.local.entity.*

@Database(
    entities = [
        SubstanceEntity::class,
        CompoundEntity::class,
        VariantEntity::class,
        DoseEntity::class,
        QuickDoseEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BioTrackDatabase : RoomDatabase() {
    abstract fun substanceDao(): SubstanceDao
    abstract fun compoundDao(): CompoundDao
    abstract fun variantDao(): VariantDao
    abstract fun doseDao(): DoseDao
    abstract fun quickDoseDao(): QuickDoseDao
}
