package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
  @Query("SELECT * FROM payments ORDER BY paymentDate DESC")
  fun getAllPayments(): Flow<List<PaymentEntity>>

  @Query("SELECT * FROM payments WHERE customerId = :customerId ORDER BY paymentDate DESC")
  fun getPaymentsForCustomer(customerId: Long): Flow<List<PaymentEntity>>

  @Query("SELECT * FROM payments WHERE customerId = :customerId ORDER BY paymentDate DESC")
  suspend fun getPaymentsForCustomerDirect(customerId: Long): List<PaymentEntity>

  @Query("SELECT * FROM payments WHERE kasbonId = :kasbonId ORDER BY paymentDate DESC")
  fun getPaymentsForKasbon(kasbonId: Long): Flow<List<PaymentEntity>>

  @Query("SELECT * FROM payments WHERE paymentDate BETWEEN :startOfDay AND :endOfDay ORDER BY paymentDate DESC")
  fun getPaymentsBetween(startOfDay: Long, endOfDay: Long): Flow<List<PaymentEntity>>

  @Query("SELECT SUM(amountPaid) FROM payments WHERE paymentDate BETWEEN :startOfDay AND :endOfDay")
  fun getTodayTotalPayments(startOfDay: Long, endOfDay: Long): Flow<Long?>

  @Query("SELECT * FROM payments WHERE id = :id LIMIT 1")
  fun getPaymentById(id: Long): Flow<PaymentEntity?>

  @Query("SELECT * FROM payments WHERE id = :id LIMIT 1")
  suspend fun getPaymentByIdDirect(id: Long): PaymentEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPayment(payment: PaymentEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPayments(payments: List<PaymentEntity>): List<Long>

  @Update
  suspend fun updatePayment(payment: PaymentEntity)

  @Delete
  suspend fun deletePayment(payment: PaymentEntity)

  @Query("DELETE FROM payments WHERE id = :id")
  suspend fun deletePaymentById(id: Long)
}
