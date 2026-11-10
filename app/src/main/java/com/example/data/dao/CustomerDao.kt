package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
  @Query("SELECT * FROM customers ORDER BY name ASC")
  fun getAllCustomers(): Flow<List<CustomerEntity>>

  @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
  fun getCustomerById(id: Long): Flow<CustomerEntity?>

  @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
  suspend fun getCustomerByIdDirect(id: Long): CustomerEntity?

  @Query("SELECT * FROM customers WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' ORDER BY name ASC")
  fun searchCustomers(query: String): Flow<List<CustomerEntity>>

  @Query("SELECT COUNT(*) FROM customers")
  fun getCustomerCount(): Flow<Int>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCustomer(customer: CustomerEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCustomers(customers: List<CustomerEntity>): List<Long>

  @Update
  suspend fun updateCustomer(customer: CustomerEntity)

  @Delete
  suspend fun deleteCustomer(customer: CustomerEntity)

  @Query("DELETE FROM customers WHERE id = :id")
  suspend fun deleteCustomerById(id: Long)
}
