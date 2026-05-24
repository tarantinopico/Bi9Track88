package com.example.domain.model

data class Substance(
    val id: String,
    val name: String,
    val alias: String,
    val category: String,
    val iconKey: String,
    val defaultUnit: String,
    val active: Boolean,
    val notes: String,
    val createdAt: Long,
    val updatedAt: Long,
    val archivedAt: Long?
)

data class Compound(
    val id: String,
    val substanceId: String,
    val name: String,
    val halfLifeHours: Double,
    val onsetMin: Int,
    val peakMin: Int,
    val durationHours: Double,
    val thresholdDose: Double,
    val commonDose: Double,
    val strongDose: Double,
    val molecularWeight: Double,
    val active: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

data class Variant(
    val id: String,
    val substanceId: String,
    val name: String,
    val colorHex: String,
    val pricePerUnit: Double,
    val unitLabel: String,
    val ratioJson: String,
    val roaDefault: String,
    val active: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

data class Dose(
    val id: String,
    val substanceId: String,
    val variantId: String?,
    val doseAmount: Double,
    val unit: String,
    val route: String,
    val price: Double,
    val timestamp: Long,
    val notes: String,
    val createdAt: Long,
    val updatedAt: Long
)

data class QuickDose(
    val id: String,
    val substanceId: String?,
    val variantId: String?,
    val label: String,
    val defaultAmount: Double,
    val defaultUnit: String,
    val defaultRoute: String,
    val defaultPrice: Double,
    val pinned: Boolean,
    val orderIndex: Int,
    val createdAt: Long,
    val updatedAt: Long
)

data class UserSettings(
    val userWeightKg: Float = 70f,
    val userAge: Int = 30,
    val metabolismFactor: Float = 1.0f,
    val themeMode: String = "SYSTEM",
    val accentPalette: String = "emerald",
    val privacyMode: Boolean = false,
    val financeMode: Boolean = true,
    val warningsEnabled: Boolean = true,
    val defaultRoute: String = "ORAL",
    val defaultUnit: String = "mg"
)
