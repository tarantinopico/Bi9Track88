package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.local.db.BioTrackDatabase
import com.example.data.local.prefs.PreferencesManager
import com.example.domain.repository.BioTrackRepository

class AppContainer(private val context: Context) {

    private val db: BioTrackDatabase by lazy {
        Room.databaseBuilder(
            context,
            BioTrackDatabase::class.java,
            "biotrack_db"
        ).fallbackToDestructiveMigration().build()
    }

    private val prefsManager: PreferencesManager by lazy {
        PreferencesManager(context)
    }

    val repository: BioTrackRepository by lazy {
        BioTrackRepository(
            db.substanceDao(),
            db.compoundDao(),
            db.variantDao(),
            db.doseDao(),
            db.quickDoseDao(),
            prefsManager
        )
    }
}
