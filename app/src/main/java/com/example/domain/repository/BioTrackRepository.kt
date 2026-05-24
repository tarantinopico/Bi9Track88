package com.example.domain.repository

import com.example.data.local.dao.*
import com.example.data.local.entity.*
import com.example.data.local.prefs.PreferencesManager
import com.example.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class BioTrackRepository(
    private val substanceDao: SubstanceDao,
    private val compoundDao: CompoundDao,
    private val variantDao: VariantDao,
    private val doseDao: DoseDao,
    private val quickDoseDao: QuickDoseDao,
    private val prefsManager: PreferencesManager
) {
    // Export database to JSON string
    suspend fun exportDatabaseToJson(): String {
        val root = JSONObject()
        root.put("version", 1)
        
        val substances = substanceDao.getAllSubstances().first() // Export everything
        val compounds = compoundDao.getAllCompounds().first()
        val variants = variantDao.getAllVariants().first()
        val doses = doseDao.getAllDoses().first()
        
        val subsArray = JSONArray()
        substances.forEach { s ->
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("name", s.name)
            obj.put("alias", s.alias)
            obj.put("category", s.category)
            obj.put("iconKey", s.iconKey)
            obj.put("defaultUnit", s.defaultUnit)
            obj.put("active", s.active)
            obj.put("notes", s.notes)
            obj.put("createdAt", s.createdAt)
            obj.put("updatedAt", s.updatedAt)
            s.archivedAt?.let { obj.put("archivedAt", it) }
            subsArray.put(obj)
        }
        root.put("substances", subsArray)
        
        val compsArray = JSONArray()
        compounds.forEach { c ->
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("substanceId", c.substanceId)
            obj.put("name", c.name)
            obj.put("halfLifeHours", c.halfLifeHours)
            obj.put("onsetMin", c.onsetMin)
            obj.put("peakMin", c.peakMin)
            obj.put("durationHours", c.durationHours)
            obj.put("thresholdDose", c.thresholdDose)
            obj.put("commonDose", c.commonDose)
            obj.put("strongDose", c.strongDose)
            obj.put("molecularWeight", c.molecularWeight)
            obj.put("active", c.active)
            obj.put("createdAt", c.createdAt)
            obj.put("updatedAt", c.updatedAt)
            compsArray.put(obj)
        }
        root.put("compounds", compsArray)
        
        val variantsArray = JSONArray()
        variants.forEach { v ->
            val obj = JSONObject()
            obj.put("id", v.id)
            obj.put("substanceId", v.substanceId)
            obj.put("name", v.name)
            obj.put("colorHex", v.colorHex)
            obj.put("pricePerUnit", v.pricePerUnit)
            obj.put("unitLabel", v.unitLabel)
            obj.put("ratioJson", v.ratioJson)
            obj.put("roaDefault", v.roaDefault)
            obj.put("active", v.active)
            obj.put("createdAt", v.createdAt)
            obj.put("updatedAt", v.updatedAt)
            variantsArray.put(obj)
        }
        root.put("variants", variantsArray)
        
        val dosesArray = JSONArray()
        doses.forEach { d ->
            val obj = JSONObject()
            obj.put("id", d.id)
            obj.put("substanceId", d.substanceId)
            d.variantId?.let { obj.put("variantId", it) }
            obj.put("doseAmount", d.doseAmount)
            obj.put("unit", d.unit)
            obj.put("route", d.route)
            obj.put("price", d.price)
            obj.put("timestamp", d.timestamp)
            obj.put("notes", d.notes)
            obj.put("createdAt", d.createdAt)
            obj.put("updatedAt", d.updatedAt)
            dosesArray.put(obj)
        }
        root.put("doses", dosesArray)

        return root.toString()
    }

    suspend fun importDatabaseFromJson(jsonString: String) {
        val root = JSONObject(jsonString)
        
        val subsArray = root.optJSONArray("substances")
        if (subsArray != null) {
            for (i in 0 until subsArray.length()) {
                val obj = subsArray.getJSONObject(i)
                substanceDao.insertSubstance(SubstanceEntity(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    alias = obj.getString("alias"),
                    category = obj.getString("category"),
                    iconKey = obj.getString("iconKey"),
                    defaultUnit = obj.getString("defaultUnit"),
                    active = obj.getBoolean("active"),
                    notes = obj.optString("notes", ""),
                    createdAt = obj.getLong("createdAt"),
                    updatedAt = obj.getLong("updatedAt"),
                    archivedAt = if (obj.has("archivedAt")) obj.getLong("archivedAt") else null
                ))
            }
        }

        val compsArray = root.optJSONArray("compounds")
        if (compsArray != null) {
            for (i in 0 until compsArray.length()) {
                val obj = compsArray.getJSONObject(i)
                compoundDao.insertCompound(CompoundEntity(
                    id = obj.getString("id"),
                    substanceId = obj.getString("substanceId"),
                    name = obj.getString("name"),
                    halfLifeHours = obj.getDouble("halfLifeHours"),
                    onsetMin = obj.getInt("onsetMin"),
                    peakMin = obj.getInt("peakMin"),
                    durationHours = obj.getDouble("durationHours"),
                    thresholdDose = obj.getDouble("thresholdDose"),
                    commonDose = obj.getDouble("commonDose"),
                    strongDose = obj.getDouble("strongDose"),
                    molecularWeight = obj.getDouble("molecularWeight"),
                    active = obj.getBoolean("active"),
                    createdAt = obj.getLong("createdAt"),
                    updatedAt = obj.getLong("updatedAt")
                ))
            }
        }
        
        val varsArray = root.optJSONArray("variants")
        if (varsArray != null) {
            for (i in 0 until varsArray.length()) {
                val obj = varsArray.getJSONObject(i)
                variantDao.insertVariant(VariantEntity(
                    id = obj.getString("id"),
                    substanceId = obj.getString("substanceId"),
                    name = obj.getString("name"),
                    colorHex = obj.getString("colorHex"),
                    pricePerUnit = obj.getDouble("pricePerUnit"),
                    unitLabel = obj.getString("unitLabel"),
                    ratioJson = obj.getString("ratioJson"),
                    roaDefault = obj.getString("roaDefault"),
                    active = obj.getBoolean("active"),
                    createdAt = obj.getLong("createdAt"),
                    updatedAt = obj.getLong("updatedAt")
                ))
            }
        }
        
        val dosesArray = root.optJSONArray("doses")
        if (dosesArray != null) {
            for (i in 0 until dosesArray.length()) {
                val obj = dosesArray.getJSONObject(i)
                doseDao.insertDose(DoseEntity(
                    id = obj.getString("id"),
                    substanceId = obj.getString("substanceId"),
                    variantId = if (obj.has("variantId")) obj.getString("variantId") else null,
                    doseAmount = obj.getDouble("doseAmount"),
                    unit = obj.getString("unit"),
                    route = obj.getString("route"),
                    price = obj.getDouble("price"),
                    timestamp = obj.getLong("timestamp"),
                    notes = obj.optString("notes", ""),
                    createdAt = obj.getLong("createdAt"),
                    updatedAt = obj.getLong("updatedAt")
                ))
            }
        }
    }

    // Settings
    val userSettings: Flow<UserSettings> = prefsManager.userSettingsFlow
    suspend fun updateSettings(settings: UserSettings) = prefsManager.updateSettings(settings)

    // Substances
    fun getActiveSubstances(): Flow<List<Substance>> {
        return substanceDao.getActiveSubstances().map { list -> list.map { it.toDomain() } }
    }
    suspend fun insertSubstance(substance: Substance) = substanceDao.insertSubstance(substance.toEntity())
    suspend fun updateSubstance(substance: Substance) = substanceDao.updateSubstance(substance.toEntity())
    suspend fun archiveSubstance(id: String, timestamp: Long) = substanceDao.archiveSubstance(id, timestamp)

    // Compounds
    fun getAllCompounds(): Flow<List<Compound>> = compoundDao.getAllCompounds().map { it.map { c -> c.toDomain() } }
    fun getCompoundsForSubstance(substanceId: String): Flow<List<Compound>> {
        return compoundDao.getCompoundsForSubstance(substanceId).map { it.map { c -> c.toDomain() } }
    }
    suspend fun insertCompound(compound: Compound) = compoundDao.insertCompound(compound.toEntity())
    suspend fun updateCompound(compound: Compound) = compoundDao.updateCompound(compound.toEntity())
    suspend fun deleteCompound(id: String) = compoundDao.deleteCompound(id)

    // Variants
    fun getAllVariants(): Flow<List<Variant>> = variantDao.getAllVariants().map { it.map { v -> v.toDomain() } }
    fun getVariantsForSubstance(substanceId: String): Flow<List<Variant>> {
        return variantDao.getVariantsForSubstance(substanceId).map { it.map { v -> v.toDomain() } }
    }
    suspend fun insertVariant(variant: Variant) = variantDao.insertVariant(variant.toEntity())
    suspend fun updateVariant(variant: Variant) = variantDao.updateVariant(variant.toEntity())
    suspend fun deleteVariant(id: String) = variantDao.deleteVariant(id)

    // Doses
    fun getAllDoses(): Flow<List<Dose>> {
        return doseDao.getAllDoses().map { it.map { d -> d.toDomain() } }
    }
    suspend fun insertDose(dose: Dose) = doseDao.insertDose(dose.toEntity())
    suspend fun deleteDose(id: String) = doseDao.deleteDose(id)

    // QuickDoses
    fun getQuickDoses(): Flow<List<QuickDose>> {
        return quickDoseDao.getQuickDoses().map { it.map { q -> q.toDomain() } }
    }
    suspend fun insertQuickDose(quickDose: QuickDose) = quickDoseDao.insertQuickDose(quickDose.toEntity())
}
