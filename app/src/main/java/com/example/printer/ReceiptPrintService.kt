package com.example.printer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.entity.CustomerEntity
import com.example.data.entity.KasbonEntity
import com.example.data.entity.PaymentEntity
import com.example.data.pref.StoreProfile
import java.net.URLEncoder

class ReceiptPrintService(
  private val printerManager: BluetoothPrinterManager
) {
  suspend fun printPaymentReceipt(
    store: StoreProfile,
    customer: CustomerEntity,
    payment: PaymentEntity,
    totalKasbon: Long,
    totalPaidBefore: Long,
    remainingDebt: Long
  ): Result<Unit> {
    val bytes = EscPosReceiptFormatter.generatePaymentReceiptBytes(
      store = store,
      customer = customer,
      payment = payment,
      totalKasbon = totalKasbon,
      totalPaidBefore = totalPaidBefore,
      remainingDebt = remainingDebt
    )
    val copies = maxOf(1, store.printCopies)
    var lastResult: Result<Unit> = Result.success(Unit)
    for (i in 1..copies) {
      lastResult = printerManager.printData(bytes)
      if (lastResult.isFailure) break
    }
    return lastResult
  }

  suspend fun printNewKasbonReceipt(
    store: StoreProfile,
    customer: CustomerEntity,
    kasbon: KasbonEntity,
    totalCustomerDebt: Long
  ): Result<Unit> {
    val bytes = EscPosReceiptFormatter.generateNewKasbonReceiptBytes(
      store = store,
      customer = customer,
      kasbon = kasbon,
      totalCustomerDebt = totalCustomerDebt
    )
    return printerManager.printData(bytes)
  }

  suspend fun printTestReceipt(store: StoreProfile): Result<Unit> {
    val bytes = EscPosReceiptFormatter.generateTestReceiptBytes(store)
    return printerManager.printData(bytes)
  }

  companion object {
    fun shareViaWhatsApp(context: Context, rawPhone: String, message: String) {
      val formattedPhone = sanitizeIndonesianPhone(rawPhone)
      try {
        val encodedMessage = URLEncoder.encode(message, "UTF-8")
        val uri = if (formattedPhone.isNotEmpty()) {
          Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=$encodedMessage")
        } else {
          Uri.parse("https://api.whatsapp.com/send?text=$encodedMessage")
        }
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
      } catch (_: Exception) {
        // Fallback to standard ACTION_SEND
        shareGeneric(context, message)
      }
    }

    fun shareGeneric(context: Context, message: String) {
      try {
        val sendIntent = Intent().apply {
          action = Intent.ACTION_SEND
          putExtra(Intent.EXTRA_TEXT, message)
          type = "text/plain"
          flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val chooser = Intent.createChooser(sendIntent, "Bagikan Bukti Transaksi").apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(chooser)
      } catch (e: Exception) {
        Toast.makeText(context, "Tidak ada aplikasi untuk membagikan: ${e.message}", Toast.LENGTH_SHORT).show()
      }
    }

    private fun sanitizeIndonesianPhone(phone: String): String {
      val digits = phone.filter { it.isDigit() }
      return when {
        digits.startsWith("0") -> "62" + digits.substring(1)
        digits.startsWith("62") -> digits
        digits.startsWith("+62") -> digits.substring(1)
        digits.isNotEmpty() -> "62$digits"
        else -> ""
      }
    }
  }
}
