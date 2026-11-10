package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.CustomerDao
import com.example.data.dao.KasbonDao
import com.example.data.dao.PaymentDao
import com.example.data.entity.CustomerEntity
import com.example.data.entity.KasbonEntity
import com.example.data.entity.PaymentEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [CustomerEntity::class, KasbonEntity::class, PaymentEntity::class],
  version = 1,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun customerDao(): CustomerDao
  abstract fun kasbonDao(): KasbonDao
  abstract fun paymentDao(): PaymentDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "khm_kasbon_pro.db"
        )
          .addCallback(AppDatabaseCallback(scope))
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }

  private class AppDatabaseCallback(
    private val scope: CoroutineScope
  ) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
      super.onCreate(db)
      INSTANCE?.let { database ->
        scope.launch(Dispatchers.IO) {
          populateInitialData(database.customerDao(), database.kasbonDao(), database.paymentDao())
        }
      }
    }

    suspend fun populateInitialData(
      customerDao: CustomerDao,
      kasbonDao: KasbonDao,
      paymentDao: PaymentDao
    ) {
      val now = System.currentTimeMillis()
      val oneDayMs = 24L * 3600 * 1000

      val c1 = CustomerEntity(name = "Budi Santoso", phone = "081234567890", address = "Jl. Merdeka No. 12", notes = "Langganan ganti oli")
      val c2 = CustomerEntity(name = "Pak Joko Bengkel", phone = "085678901234", address = "Dusun Krajan RT 02", notes = "Beli sparepart motor")
      val c3 = CustomerEntity(name = "Bu Ratna Warung", phone = "087890123456", address = "Pasar Tradisional Kios 5", notes = "Sembako dan minyak goreng")

      val id1 = customerDao.insertCustomer(c1)
      val id2 = customerDao.insertCustomer(c2)
      val id3 = customerDao.insertCustomer(c3)

      // Kasbon for Budi
      val k1 = KasbonEntity(
        customerId = id1,
        invoiceNumber = "KSB-1001",
        amount = 1500000,
        remainingAmount = 400000,
        note = "Servis besar & ganti ban tubeless",
        date = now - (5 * oneDayMs),
        dueDate = now + (2 * oneDayMs),
        status = KasbonEntity.STATUS_PARTIAL
      )
      val k1Id = kasbonDao.insertKasbon(k1)

      // Payments for Budi
      paymentDao.insertPayment(
        PaymentEntity(
          customerId = id1,
          kasbonId = k1Id,
          receiptNumber = "BYR-000124",
          amountPaid = 800000,
          remainingDebtAfter = 700000,
          paymentDate = now - (3 * oneDayMs),
          paymentMethod = "Tunai",
          notes = "Cicilan ke-1"
        )
      )
      paymentDao.insertPayment(
        PaymentEntity(
          customerId = id1,
          kasbonId = k1Id,
          receiptNumber = "BYR-000125",
          amountPaid = 300000,
          remainingDebtAfter = 400000,
          paymentDate = now - (1 * oneDayMs),
          paymentMethod = "Transfer",
          notes = "Cicilan ke-2 via BCA"
        )
      )

      // Kasbon for Pak Joko (Jatuh tempo hari ini)
      val k2 = KasbonEntity(
        customerId = id2,
        invoiceNumber = "KSB-1002",
        amount = 650000,
        remainingAmount = 650000,
        note = "Kampas rem & rantai motor",
        date = now - (7 * oneDayMs),
        dueDate = now, // Jatuh tempo hari ini
        status = KasbonEntity.STATUS_UNPAID
      )
      kasbonDao.insertKasbon(k2)

      // Kasbon for Bu Ratna (Lunas)
      val k3 = KasbonEntity(
        customerId = id3,
        invoiceNumber = "KSB-1003",
        amount = 500000,
        remainingAmount = 0,
        note = "Beras 2 karung & telur",
        date = now - (10 * oneDayMs),
        dueDate = now - (3 * oneDayMs),
        status = KasbonEntity.STATUS_PAID
      )
      val k3Id = kasbonDao.insertKasbon(k3)
      paymentDao.insertPayment(
        PaymentEntity(
          customerId = id3,
          kasbonId = k3Id,
          receiptNumber = "BYR-000119",
          amountPaid = 500000,
          remainingDebtAfter = 0,
          paymentDate = now - (3 * oneDayMs),
          paymentMethod = "Tunai",
          notes = "Pelunasan penuh"
        )
      )
    }
  }
}
