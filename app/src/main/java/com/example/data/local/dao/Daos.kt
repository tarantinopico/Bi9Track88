package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SubstanceDao {
    @Query("SELECT * FROM substances ORDER BY name ASC")
    fun getAllSubstances(): Flow<List<SubstanceEntity>>

    @Query("SELECT * FROM substances WHERE archivedAt IS NULL ORDER BY name ASC")
    fun getActiveSubstances(): Flow<List<SubstanceEntity>>

    @Query("SELECT * FROM substances WHERE id = :id")
    suspend fun getSubstanceById(id: String): SubstanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubstance(substance: SubstanceEntity)

    @Update
    suspend fun updateSubstance(substance: SubstanceEntity)

    @Query("UPDATE substances SET archivedAt = :timestamp WHERE id = :id")
    suspend fun archiveSubstance(id: String, timestamp: Long)
}

@Dao
interface CompoundDao {
    @Query("SELECT * FROM compounds")
    fun getAllCompounds(): Flow<List<CompoundEntity>>

    @Query("SELECT * FROM compounds WHERE substanceId = :substanceId")
    fun getCompoundsForSubstance(substanceId: String): Flow<List<CompoundEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompound(compound: CompoundEntity)

    @Update
    suspend fun updateCompound(compound: CompoundEntity)

    @Query("DELETE FROM compounds WHERE id = :id")
    suspend fun deleteCompound(id: String)
}

@Dao
interface VariantDao {
    @Query("SELECT * FROM variants")
    fun getAllVariants(): Flow<List<VariantEntity>>

    @Query("SELECT * FROM variants WHERE substanceId = :substanceId")
    fun getVariantsForSubstance(substanceId: String): Flow<List<VariantEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariant(variant: VariantEntity)

    @Update
    suspend fun updateVariant(variant: VariantEntity)

    @Query("DELETE FROM variants WHERE id = :id")
    suspend fun deleteVariant(id: String)
}

@Dao
interface DoseDao {
    @Query("SELECT * FROM doses ORDER BY timestamp DESC")
    fun getAllDoses(): Flow<List<DoseEntity>>

    @Query("SELECT * FROM doses WHERE timestamp >= :since ORDER BY timestamp DESC")
    fun getDosesSince(since: Long): Flow<List<DoseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDose(dose: DoseEntity)

    @Query("DELETE FROM doses WHERE id = :id")
    suspend fun deleteDose(id: String)
}

@Dao
interface QuickDoseDao {
    @Query("SELECT * FROM quick_doses ORDER BY orderIndex ASC")
    fun getQuickDoses(): Flow<List<QuickDoseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuickDose(quickDose: QuickDoseEntity)
}
