package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.KasbonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KasbonDao {
  @Query("SELECT * FROM kasbons ORDER BY date DESC")
  fun getAllKasbons(): Flow<List<KasbonEntity>>

  @Query("SELECT * FROM kasbons WHERE customerId = :customerId ORDER BY date DESC")
  fun getKasbonsForCustomer(customerId: Long): Flow<List<KasbonEntity>>

  @Query("SELECT * FROM kasbons WHERE customerId = :customerId ORDER BY date DESC")
  suspend fun getKasbonsForCustomerDirect(customerId: Long): List<KasbonEntity>

  @Query("SELECT * FROM kasbons WHERE status != 'PAID' ORDER BY dueDate ASC")
  fun getActiveKasbons(): Flow<List<KasbonEntity>>

  @Query("SELECT * FROM kasbons WHERE status != 'PAID' AND dueDate <= :timeThreshold ORDER BY dueDate ASC")
  fun getDueKasbons(timeThreshold: Long): Flow<List<KasbonEntity>>

  @Query("SELECT * FROM kasbons WHERE id = :id LIMIT 1")
  fun getKasbonById(id: Long): Flow<KasbonEntity?>

  @Query("SELECT * FROM kasbons WHERE id = :id LIMIT 1")
  suspend fun getKasbonByIdDirect(id: Long): KasbonEntity?

  @Query("SELECT * FROM kasbons WHERE date BETWEEN :startOfDay AND :endOfDay ORDER BY date DESC")
  fun getKasbonsBetween(startOfDay: Long, endOfDay: Long): Flow<List<KasbonEntity>>

  @Query("SELECT SUM(remainingAmount) FROM kasbons WHERE status != 'PAID'")
  fun getTotalRemainingDebt(): Flow<Long?>

  @Query("SELECT SUM(amount) FROM kasbons WHERE date BETWEEN :startOfDay AND :endOfDay")
  fun getTodayTotalKasbon(startOfDay: Long, endOfDay: Long): Flow<Long?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertKasbon(kasbon: KasbonEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertKasbons(kasbons: List<KasbonEntity>): List<Long>

  @Update
  suspend fun updateKasbon(kasbon: KasbonEntity)

  @Delete
  suspend fun deleteKasbon(kasbon: KasbonEntity)

  @Query("DELETE FROM kasbons WHERE id = :id")
  suspend fun deleteKasbonById(id: Long)
}
