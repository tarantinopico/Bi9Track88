package com.example.domain.pk

import com.example.domain.model.Compound
import com.example.domain.model.UserSettings
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max

data class PKRouteDef(val id: String, val f: Double, val kaBase: Double)

data class CompoundPKResult(
    val compoundId: String,
    val compoundName: String,
    val tmaxHours: Double,
    val peakLoadMg: Double,
    val currentLoadMg: Double,
    val durationHours: Double,
    val warnings: List<String>
)

data class OverallPKResult(
    val activeCompounds: List<CompoundPKResult>,
    val totalWarnings: List<String>,
    val estimatedTmaxHours: Double,
    val estimatedDurationHours: Double
)

object PKEngine {
    val routes = listOf(
        PKRouteDef("ORAL", 0.5, 2.0),
        PKRouteDef("SUBLINGUAL", 0.8, 4.0),
        PKRouteDef("INTRANASAL", 0.9, 6.0),
        PKRouteDef("INHALATION", 1.0, 10.0),
        PKRouteDef("INTRAVENOUS", 1.0, 100.0)
    )

    fun calculateCompoundPK(
        compound: Compound,
        doseAmountMg: Double,
        route: String,
        userSettings: UserSettings,
        timeSinceDoseHours: Double
    ): CompoundPKResult {
        val routeDef = routes.find { it.id.equals(route, ignoreCase = true) } ?: routes[0]
        
        // Age modifier: slight slowdown of metabolism for older people
        val ageModifier = if (userSettings.userAge > 50) 1.0 + (userSettings.userAge - 50) * 0.01 else 1.0
        
        // Final half-life adjusted by user metabolism factor
        val halfLifeAdjusted = compound.halfLifeHours / max(0.1f, userSettings.metabolismFactor) * ageModifier
        
        val ke = ln(2.0) / max(0.1, halfLifeAdjusted)
        val ka = routeDef.kaBase * (60.0 / max(1.0, compound.onsetMin.toDouble()))
        
        // Calculate Tmax
        val tmaxHours = if (ka == ke) {
            1.0 / ke
        } else {
            val ratio = ka / ke
            if (ratio > 0.0 && ratio != 1.0) ln(ratio) / (ka - ke) else 0.0
        }

        // Simplistic Vd proportional to user weight, for now 1L/kg
        val Vd = max(1.0, userSettings.userWeightKg.toDouble())

        fun calcLoadAt(t: Double): Double {
            if (t <= 0) return 0.0
            return if (ka == ke) {
                routeDef.f * doseAmountMg * ka * t * exp(-ke * t)
            } else {
                routeDef.f * doseAmountMg * ka / (ka - ke) * (exp(-ke * t) - exp(-ka * t))
            }
        }

        val peakLoad = calcLoadAt(max(0.0, tmaxHours))
        val currentLoad = calcLoadAt(max(0.0, timeSinceDoseHours))
        
        // Estimate duration based on dropping below threshold
        var durationEst = max(0.0, tmaxHours)
        var searchLoad = peakLoad
        val thresholdInBody = compound.thresholdDose * routeDef.f // Approximate functional threshold
        
        // Incremental search for intersection
        while (searchLoad > thresholdInBody && durationEst < 100.0) {
            durationEst += 0.5
            searchLoad = calcLoadAt(durationEst)
        }
        if (durationEst >= 100.0) durationEst = compound.durationHours // Fallback to provided basic duration

        val warnings = mutableListOf<String>()
        if (doseAmountMg > compound.strongDose) {
            warnings.add("Překročena silná dávka pro ${compound.name}!")
        }
        if (peakLoad > compound.commonDose * 2) {
            warnings.add("Vysoký odhad zátěže (peak) pro ${compound.name}!")
        }

        return CompoundPKResult(
            compoundId = compound.id,
            compoundName = compound.name,
            tmaxHours = max(0.0, tmaxHours),
            peakLoadMg = peakLoad,
            currentLoadMg = currentLoad,
            durationHours = durationEst,
            warnings = warnings
        )
    }

    fun parseRatio(json: String): Map<String, Double> {
        val map = mutableMapOf<String, Double>()
        try {
            val obj = org.json.JSONObject(json)
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                map[key] = obj.getDouble(key)
            }
        } catch (e: Exception) {
            // Silently fail and return empty map if parsing fails
        }
        return map
    }
}
