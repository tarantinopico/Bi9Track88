package com.example.data.local.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {

    private object Keys {
        val USER_WEIGHT_KG = floatPreferencesKey("user_weight_kg")
        val USER_AGE = intPreferencesKey("user_age")
        val METABOLISM_FACTOR = floatPreferencesKey("metabolism_factor")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_PALETTE = stringPreferencesKey("accent_palette")
        val PRIVACY_MODE = booleanPreferencesKey("privacy_mode")
        val FINANCE_MODE = booleanPreferencesKey("finance_mode")
        val WARNINGS_ENABLED = booleanPreferencesKey("warnings_enabled")
        val DEFAULT_ROUTE = stringPreferencesKey("default_route")
        val DEFAULT_UNIT = stringPreferencesKey("default_unit")
    }

    val userSettingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            userWeightKg = prefs[Keys.USER_WEIGHT_KG] ?: 70f,
            userAge = prefs[Keys.USER_AGE] ?: 30,
            metabolismFactor = prefs[Keys.METABOLISM_FACTOR] ?: 1.0f,
            themeMode = prefs[Keys.THEME_MODE] ?: "SYSTEM",
            accentPalette = prefs[Keys.ACCENT_PALETTE] ?: "emerald",
            privacyMode = prefs[Keys.PRIVACY_MODE] ?: false,
            financeMode = prefs[Keys.FINANCE_MODE] ?: true,
            warningsEnabled = prefs[Keys.WARNINGS_ENABLED] ?: true,
            defaultRoute = prefs[Keys.DEFAULT_ROUTE] ?: "ORAL",
            defaultUnit = prefs[Keys.DEFAULT_UNIT] ?: "mg"
        )
    }

    suspend fun updateSettings(settings: UserSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USER_WEIGHT_KG] = settings.userWeightKg
            prefs[Keys.USER_AGE] = settings.userAge
            prefs[Keys.METABOLISM_FACTOR] = settings.metabolismFactor
            prefs[Keys.THEME_MODE] = settings.themeMode
            prefs[Keys.ACCENT_PALETTE] = settings.accentPalette
            prefs[Keys.PRIVACY_MODE] = settings.privacyMode
            prefs[Keys.FINANCE_MODE] = settings.financeMode
            prefs[Keys.WARNINGS_ENABLED] = settings.warningsEnabled
            prefs[Keys.DEFAULT_ROUTE] = settings.defaultRoute
            prefs[Keys.DEFAULT_UNIT] = settings.defaultUnit
        }
    }
}
