package com.example.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "kasbons",
  foreignKeys = [
    ForeignKey(
      entity = CustomerEntity::class,
      parentColumns = ["id"],
      childColumns = ["customerId"],
      onDelete = ForeignKey.CASCADE
    )
  ],
  indices = [Index("customerId")]
)
data class KasbonEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val customerId: Long,
  val invoiceNumber: String,
  val amount: Long,
  val remainingAmount: Long,
  val note: String,
  val date: Long = System.currentTimeMillis(),
  val dueDate: Long = System.currentTimeMillis() + (7L * 24 * 3600 * 1000),
  val status: String = STATUS_UNPAID // UNPAID, PARTIAL, PAID
) {
  companion object {
    const val STATUS_UNPAID = "UNPAID"
    const val STATUS_PARTIAL = "PARTIAL"
    const val STATUS_PAID = "PAID"
  }
}
