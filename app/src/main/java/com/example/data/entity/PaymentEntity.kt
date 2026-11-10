package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "payments",
  foreignKeys = [
    ForeignKey(
      entity = CustomerEntity::class,
      parentColumns = ["id"],
      childColumns = ["customerId"],
      onDelete = ForeignKey.CASCADE
    )
  ],
  indices = [Index("customerId"), Index("kasbonId")]
)
data class PaymentEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val customerId: Long,
  val kasbonId: Long? = null,
  val receiptNumber: String,
  val amountPaid: Long,
  val remainingDebtAfter: Long,
  val paymentDate: Long = System.currentTimeMillis(),
  val paymentMethod: String = "Tunai",
  val notes: String = ""
)
