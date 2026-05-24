package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.model.*

@Entity(tableName = "substances")
data class SubstanceEntity(
    @PrimaryKey val id: String,
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
) {
    fun toDomain() = Substance(id, name, alias, category, iconKey, defaultUnit, active, notes, createdAt, updatedAt, archivedAt)
}

@Entity(
    tableName = "compounds",
    foreignKeys = [
        ForeignKey(entity = SubstanceEntity::class, parentColumns = ["id"], childColumns = ["substanceId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("substanceId")]
)
data class CompoundEntity(
    @PrimaryKey val id: String,
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
) {
    fun toDomain() = Compound(id, substanceId, name, halfLifeHours, onsetMin, peakMin, durationHours, thresholdDose, commonDose, strongDose, molecularWeight, active, createdAt, updatedAt)
}

@Entity(
    tableName = "variants",
    foreignKeys = [
        ForeignKey(entity = SubstanceEntity::class, parentColumns = ["id"], childColumns = ["substanceId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("substanceId")]
)
data class VariantEntity(
    @PrimaryKey val id: String,
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
) {
    fun toDomain() = Variant(id, substanceId, name, colorHex, pricePerUnit, unitLabel, ratioJson, roaDefault, active, createdAt, updatedAt)
}

@Entity(
    tableName = "doses",
    foreignKeys = [
        ForeignKey(entity = SubstanceEntity::class, parentColumns = ["id"], childColumns = ["substanceId"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("substanceId"), Index("variantId"), Index("timestamp")]
)
data class DoseEntity(
    @PrimaryKey val id: String,
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
) {
    fun toDomain() = Dose(id, substanceId, variantId, doseAmount, unit, route, price, timestamp, notes, createdAt, updatedAt)
}

@Entity(tableName = "quick_doses")
data class QuickDoseEntity(
    @PrimaryKey val id: String,
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
) {
    fun toDomain() = QuickDose(id, substanceId, variantId, label, defaultAmount, defaultUnit, defaultRoute, defaultPrice, pinned, orderIndex, createdAt, updatedAt)
}

fun Substance.toEntity() = SubstanceEntity(id, name, alias, category, iconKey, defaultUnit, active, notes, createdAt, updatedAt, archivedAt)
fun Compound.toEntity() = CompoundEntity(id, substanceId, name, halfLifeHours, onsetMin, peakMin, durationHours, thresholdDose, commonDose, strongDose, molecularWeight, active, createdAt, updatedAt)
fun Variant.toEntity() = VariantEntity(id, substanceId, name, colorHex, pricePerUnit, unitLabel, ratioJson, roaDefault, active, createdAt, updatedAt)
fun Dose.toEntity() = DoseEntity(id, substanceId, variantId, doseAmount, unit, route, price, timestamp, notes, createdAt, updatedAt)
fun QuickDose.toEntity() = QuickDoseEntity(id, substanceId, variantId, label, defaultAmount, defaultUnit, defaultRoute, defaultPrice, pinned, orderIndex, createdAt, updatedAt)
