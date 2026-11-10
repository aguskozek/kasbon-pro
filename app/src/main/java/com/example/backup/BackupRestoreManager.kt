package com.example.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.dao.CustomerDao
import com.example.data.dao.KasbonDao
import com.example.data.dao.PaymentDao
import com.example.data.entity.CustomerEntity
import com.example.data.entity.KasbonEntity
import com.example.data.entity.PaymentEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupRestoreManager(
  private val context: Context,
  private val customerDao: CustomerDao,
  private val kasbonDao: KasbonDao,
  private val paymentDao: PaymentDao
) {
  suspend fun exportBackupToJson(): Result<File> = withContext(Dispatchers.IO) {
    try {
      val customers = customerDao.getAllCustomers().first()
      val kasbons = kasbonDao.getAllKasbons().first()
      val payments = paymentDao.getAllPayments().first()

      val rootJson = JSONObject().apply {
        put("app", "KHM KASBON PRO")
        put("version", 1)
        put("exportedAt", System.currentTimeMillis())
        put("exportedDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))

        val customerArray = JSONArray()
        customers.forEach { c ->
          customerArray.put(JSONObject().apply {
            put("id", c.id)
            put("name", c.name)
            put("phone", c.phone)
            put("address", c.address)
            put("notes", c.notes)
            put("createdAt", c.createdAt)
          })
        }
        put("customers", customerArray)

        val kasbonArray = JSONArray()
        kasbons.forEach { k ->
          kasbonArray.put(JSONObject().apply {
            put("id", k.id)
            put("customerId", k.customerId)
            put("invoiceNumber", k.invoiceNumber)
            put("amount", k.amount)
            put("remainingAmount", k.remainingAmount)
            put("note", k.note)
            put("date", k.date)
            put("dueDate", k.dueDate)
            put("status", k.status)
          })
        }
        put("kasbons", kasbonArray)

        val paymentArray = JSONArray()
        payments.forEach { p ->
          paymentArray.put(JSONObject().apply {
            put("id", p.id)
            put("customerId", p.customerId)
            put("kasbonId", p.kasbonId ?: JSONObject.NULL)
            put("receiptNumber", p.receiptNumber)
            put("amountPaid", p.amountPaid)
            put("remainingDebtAfter", p.remainingDebtAfter)
            put("paymentDate", p.paymentDate)
            put("paymentMethod", p.paymentMethod)
            put("notes", p.notes)
          })
        }
        put("payments", paymentArray)
      }

      val backupDir = File(context.cacheDir, "backups").apply { mkdirs() }
      val fileName = "KHM_Kasbon_Backup_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.json"
      val backupFile = File(backupDir, fileName)

      FileOutputStream(backupFile).use { fos ->
        fos.write(rootJson.toString(2).toByteArray(Charsets.UTF_8))
      }

      Result.success(backupFile)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun importBackupFromJson(jsonString: String): Result<String> = withContext(Dispatchers.IO) {
    try {
      val root = JSONObject(jsonString)
      val customersJson = root.optJSONArray("customers") ?: JSONArray()
      val kasbonsJson = root.optJSONArray("kasbons") ?: JSONArray()
      val paymentsJson = root.optJSONArray("payments") ?: JSONArray()

      val newCustomers = mutableListOf<CustomerEntity>()
      for (i in 0 until customersJson.length()) {
        val obj = customersJson.getJSONObject(i)
        newCustomers.add(
          CustomerEntity(
            id = obj.optLong("id", 0L),
            name = obj.getString("name"),
            phone = obj.optString("phone", ""),
            address = obj.optString("address", ""),
            notes = obj.optString("notes", ""),
            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
          )
        )
      }

      val newKasbons = mutableListOf<KasbonEntity>()
      for (i in 0 until kasbonsJson.length()) {
        val obj = kasbonsJson.getJSONObject(i)
        newKasbons.add(
          KasbonEntity(
            id = obj.optLong("id", 0L),
            customerId = obj.getLong("customerId"),
            invoiceNumber = obj.getString("invoiceNumber"),
            amount = obj.getLong("amount"),
            remainingAmount = obj.getLong("remainingAmount"),
            note = obj.optString("note", ""),
            date = obj.optLong("date", System.currentTimeMillis()),
            dueDate = obj.optLong("dueDate", System.currentTimeMillis()),
            status = obj.optString("status", KasbonEntity.STATUS_UNPAID)
          )
        )
      }

      val newPayments = mutableListOf<PaymentEntity>()
      for (i in 0 until paymentsJson.length()) {
        val obj = paymentsJson.getJSONObject(i)
        val kId = if (obj.isNull("kasbonId")) null else obj.optLong("kasbonId")
        newPayments.add(
          PaymentEntity(
            id = obj.optLong("id", 0L),
            customerId = obj.getLong("customerId"),
            kasbonId = kId,
            receiptNumber = obj.getString("receiptNumber"),
            amountPaid = obj.getLong("amountPaid"),
            remainingDebtAfter = obj.optLong("remainingDebtAfter", 0L),
            paymentDate = obj.optLong("paymentDate", System.currentTimeMillis()),
            paymentMethod = obj.optString("paymentMethod", "Tunai"),
            notes = obj.optString("notes", "")
          )
        )
      }

      if (newCustomers.isNotEmpty()) {
        customerDao.insertCustomers(newCustomers)
      }
      if (newKasbons.isNotEmpty()) {
        kasbonDao.insertKasbons(newKasbons)
      }
      if (newPayments.isNotEmpty()) {
        paymentDao.insertPayments(newPayments)
      }

      Result.success("Berhasil memulihkan ${newCustomers.size} pelanggan, ${newKasbons.size} kasbon, dan ${newPayments.size} pembayaran.")
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  fun shareBackupToGoogleDriveOrApps(file: File) {
    try {
      val uri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
      )
      val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "Backup Database KHM Kasbon Pro")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      val chooser = Intent.createChooser(shareIntent, "Simpan ke Google Drive / Bagikan").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(chooser)
    } catch (e: Exception) {
      // If FileProvider is not yet declared, fallback to simple chooser or toast
      val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, file.readText())
        putExtra(Intent.EXTRA_SUBJECT, "Backup Database KHM Kasbon Pro (JSON)")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(Intent.createChooser(fallbackIntent, "Simpan Backup").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      })
    }
  }
}
