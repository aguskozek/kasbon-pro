package com.example.printer

import com.example.data.entity.CustomerEntity
import com.example.data.entity.KasbonEntity
import com.example.data.entity.PaymentEntity
import com.example.data.pref.StoreProfile
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object EscPosReceiptFormatter {
  // ESC/POS Command Constants
  private val CMD_INIT = byteArrayOf(0x1B, 0x40) // ESC @
  private val CMD_ALIGN_LEFT = byteArrayOf(0x1B, 0x61, 0x00) // ESC a 0
  private val CMD_ALIGN_CENTER = byteArrayOf(0x1B, 0x61, 0x01) // ESC a 1
  private val CMD_ALIGN_RIGHT = byteArrayOf(0x1B, 0x61, 0x02) // ESC a 2
  private val CMD_BOLD_ON = byteArrayOf(0x1B, 0x45, 0x01) // ESC E 1
  private val CMD_BOLD_OFF = byteArrayOf(0x1B, 0x45, 0x00) // ESC E 0
  private val CMD_DOUBLE_SIZE = byteArrayOf(0x1D, 0x21, 0x11) // GS ! 0x11
  private val CMD_NORMAL_SIZE = byteArrayOf(0x1D, 0x21, 0x00) // GS ! 0x00
  private val CMD_FEED_6 = byteArrayOf(0x1B, 0x64, 0x06) // Feed 6 lines past cutter bar

  private val CHARSET_ASCII: Charset = Charset.forName("US-ASCII")

  fun formatRupiah(amount: Long): String {
    val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
    return "Rp " + formatter.format(amount)
  }

  fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
  }

  fun formatSeparator(char: Char = '-', width: Int = 32): String {
    return char.toString().repeat(maxOf(1, width))
  }

  /**
   * Formats a row with left and right text aligned precisely to totalWidth (32 for 58mm, 48 for 80mm).
   * If both fit on one line with at least 1 space, puts right at the exact right margin.
   * If they exceed totalWidth, places left on the first line and right aligned to the right on the next line.
   */
  fun formatRow(left: String, right: String, totalWidth: Int = 32): String {
    val trimmedLeft = left.trim()
    val trimmedRight = right.trim()
    val spaceNeeded = totalWidth - trimmedLeft.length - trimmedRight.length

    return if (spaceNeeded >= 1) {
      trimmedLeft + " ".repeat(spaceNeeded) + trimmedRight
    } else {
      // Put left on line 1, right aligned on line 2
      val rightPadding = maxOf(0, totalWidth - trimmedRight.length)
      trimmedLeft + "\n" + " ".repeat(rightPadding) + trimmedRight
    }
  }

  fun centerText(text: String, width: Int = 32): String {
    val trimmed = text.trim()
    if (trimmed.length >= width) return trimmed
    val leftPadding = (width - trimmed.length) / 2
    return " ".repeat(leftPadding) + trimmed
  }

  // --- ESC/POS BINARY GENERATORS ---

  fun generatePaymentReceiptBytes(
    store: StoreProfile,
    customer: CustomerEntity,
    payment: PaymentEntity,
    totalKasbon: Long,
    totalPaidBefore: Long,
    remainingDebt: Long,
    width: Int = store.charsPerLine
  ): ByteArray {
    val stream = ByteArrayOutputStream()
    val charset = CHARSET_ASCII
    val effectiveWidth = if (width > 0) width else 32

    stream.write(CMD_INIT)

    // 1. Header Toko & Keterangan Bukti Transaksi
    stream.write(CMD_ALIGN_CENTER)
    stream.write(CMD_BOLD_ON)
    stream.write(CMD_DOUBLE_SIZE)
    stream.write((store.storeName + "\n").toByteArray(charset))
    stream.write(CMD_NORMAL_SIZE)
    stream.write(CMD_BOLD_OFF)

    if (store.storeAddress.isNotEmpty()) {
      stream.write((store.storeAddress + "\n").toByteArray(charset))
    }
    if (store.storePhone.isNotEmpty()) {
      stream.write(("Telp/WA: ${store.storePhone}\n").toByteArray(charset))
    }

    stream.write((formatSeparator('=', effectiveWidth) + "\n").toByteArray(charset))
    stream.write(CMD_BOLD_ON)
    stream.write("BUKTI PEMBAYARAN KASBON\n".toByteArray(charset))
    stream.write(CMD_BOLD_OFF)
    stream.write((formatSeparator('-', effectiveWidth) + "\n").toByteArray(charset))

    // 2. Info Transaksi (Rata Kiri Kanan Rapi)
    stream.write(CMD_ALIGN_LEFT)
    stream.write((formatRow("No. Transaksi", payment.receiptNumber, effectiveWidth) + "\n").toByteArray(charset))
    stream.write((formatRow("Tanggal", formatDate(payment.paymentDate), effectiveWidth) + "\n").toByteArray(charset))
    stream.write((formatRow("Pelanggan", customer.name, effectiveWidth) + "\n").toByteArray(charset))
    if (customer.phone.isNotEmpty()) {
      stream.write((formatRow("No. HP", customer.phone, effectiveWidth) + "\n").toByteArray(charset))
    }
    stream.write((formatRow("Metode Bayar", payment.paymentMethod, effectiveWidth) + "\n").toByteArray(charset))
    if (payment.notes.isNotEmpty()) {
      stream.write((formatRow("Keterangan", payment.notes, effectiveWidth) + "\n").toByteArray(charset))
    }
    stream.write((formatSeparator('-', effectiveWidth) + "\n").toByteArray(charset))

    // 3. Rincian Nominal (Rata Kiri Kanan Presisi)
    stream.write((formatRow("Total Kasbon", formatRupiah(totalKasbon), effectiveWidth) + "\n").toByteArray(charset))
    stream.write((formatRow("Sudah Dibayar", formatRupiah(totalPaidBefore), effectiveWidth) + "\n").toByteArray(charset))
    stream.write(CMD_BOLD_ON)
    stream.write((formatRow("Bayar Sekarang", formatRupiah(payment.amountPaid), effectiveWidth) + "\n").toByteArray(charset))
    stream.write((formatSeparator('-', effectiveWidth) + "\n").toByteArray(charset))
    stream.write((formatRow("SISA UTANG", formatRupiah(remainingDebt), effectiveWidth) + "\n").toByteArray(charset))
    stream.write(CMD_BOLD_OFF)
    stream.write((formatSeparator('-', effectiveWidth) + "\n").toByteArray(charset))

    // 4. Status Pembayaran
    val statusText = if (remainingDebt <= 0) "*** LUNAS ***" else "BELUM LUNAS"
    stream.write(CMD_ALIGN_CENTER)
    stream.write(CMD_BOLD_ON)
    stream.write(("STATUS: $statusText\n\n").toByteArray(charset))
    stream.write(CMD_BOLD_OFF)

    // 5. Footer & Catatan Terima Kasih
    if (store.receiptFooter.isNotEmpty()) {
      stream.write((store.receiptFooter + "\n").toByteArray(charset))
    }
    stream.write(("Simpan struk ini sebagai bukti sah\n").toByteArray(charset))

    // 6. Safe Tear Space (Jarak sobek agar tulisan terima kasih tidak tersobek)
    stream.write((formatSeparator('-', effectiveWidth) + "\n").toByteArray(charset))
    stream.write(("- - - SOBEK DI SINI - - -\n").toByteArray(charset))
    stream.write(CMD_FEED_6)
    stream.write("\n\n\n\n\n".toByteArray(charset))

    return stream.toByteArray()
  }

  fun generateNewKasbonReceiptBytes(
    store: StoreProfile,
    customer: CustomerEntity,
    kasbon: KasbonEntity,
    totalCustomerDebt: Long,
    width: Int = store.charsPerLine
  ): ByteArray {
    val stream = ByteArrayOutputStream()
    val charset = CHARSET_ASCII
    val effectiveWidth = if (width > 0) width else 32

    stream.write(CMD_INIT)

    // 1. Header Toko & Keterangan Bukti Transaksi
    stream.write(CMD_ALIGN_CENTER)
    stream.write(CMD_BOLD_ON)
    stream.write(CMD_DOUBLE_SIZE)
    stream.write((store.storeName + "\n").toByteArray(charset))
    stream.write(CMD_NORMAL_SIZE)
    stream.write(CMD_BOLD_OFF)

    if (store.storeAddress.isNotEmpty()) {
      stream.write((store.storeAddress + "\n").toByteArray(charset))
    }
    if (store.storePhone.isNotEmpty()) {
      stream.write(("Telp/WA: ${store.storePhone}\n").toByteArray(charset))
    }

    stream.write((formatSeparator('=', effectiveWidth) + "\n").toByteArray(charset))
    stream.write(CMD_BOLD_ON)
    stream.write("BUKTI CATATAN KASBON BARU\n".toByteArray(charset))
    stream.write(CMD_BOLD_OFF)
    stream.write((formatSeparator('-', effectiveWidth) + "\n").toByteArray(charset))

    // 2. Info Kasbon (Rata Kiri Kanan Rapi)
    stream.write(CMD_ALIGN_LEFT)
    stream.write((formatRow("No. Kasbon", kasbon.invoiceNumber, effectiveWidth) + "\n").toByteArray(charset))
    stream.write((formatRow("Tanggal", formatDate(kasbon.date), effectiveWidth) + "\n").toByteArray(charset))
    stream.write((formatRow("Jatuh Tempo", formatDate(kasbon.dueDate), effectiveWidth) + "\n").toByteArray(charset))
    stream.write((formatRow("Pelanggan", customer.name, effectiveWidth) + "\n").toByteArray(charset))
    if (kasbon.note.isNotEmpty()) {
      stream.write((formatRow("Rincian", kasbon.note, effectiveWidth) + "\n").toByteArray(charset))
    }
    stream.write((formatSeparator('-', effectiveWidth) + "\n").toByteArray(charset))

    // 3. Nominal
    stream.write(CMD_BOLD_ON)
    stream.write((formatRow("Nominal Kasbon", formatRupiah(kasbon.amount), effectiveWidth) + "\n").toByteArray(charset))
    stream.write((formatRow("Total Utang", formatRupiah(totalCustomerDebt), effectiveWidth) + "\n").toByteArray(charset))
    stream.write(CMD_BOLD_OFF)
    stream.write((formatSeparator('-', effectiveWidth) + "\n").toByteArray(charset))

    // 4. Catatan & Safe Tear Space
    stream.write(CMD_ALIGN_CENTER)
    stream.write("Mohon lakukan pembayaran sebelum\ntanggal jatuh tempo.\n".toByteArray(charset))
    if (store.storePhone.isNotEmpty()) {
      stream.write(("Konfirmasi WA: ${store.storePhone}\n").toByteArray(charset))
    }
    stream.write("\n".toByteArray(charset))
    stream.write((formatSeparator('-', effectiveWidth) + "\n").toByteArray(charset))
    stream.write(("- - - SOBEK DI SINI - - -\n").toByteArray(charset))
    stream.write(CMD_FEED_6)
    stream.write("\n\n\n\n\n".toByteArray(charset))

    return stream.toByteArray()
  }

  fun generateTestReceiptBytes(store: StoreProfile, width: Int = store.charsPerLine): ByteArray {
    val stream = ByteArrayOutputStream()
    val charset = CHARSET_ASCII
    val effectiveWidth = if (width > 0) width else 32

    stream.write(CMD_INIT)
    stream.write(CMD_ALIGN_CENTER)
    stream.write(CMD_BOLD_ON)
    stream.write(CMD_DOUBLE_SIZE)
    stream.write((store.storeName + "\n").toByteArray(charset))
    stream.write(CMD_NORMAL_SIZE)
    stream.write(CMD_BOLD_OFF)

    if (store.storeAddress.isNotEmpty()) {
      stream.write((store.storeAddress + "\n").toByteArray(charset))
    }
    if (store.storePhone.isNotEmpty()) {
      stream.write(("Telp/WA: ${store.storePhone}\n").toByteArray(charset))
    }

    stream.write((formatSeparator('=', effectiveWidth) + "\n").toByteArray(charset))
    stream.write(CMD_BOLD_ON)
    stream.write("BUKTI PENGUJIAN CETAK STRUK\n".toByteArray(charset))
    stream.write(CMD_BOLD_OFF)
    stream.write((formatSeparator('-', effectiveWidth) + "\n").toByteArray(charset))

    stream.write(CMD_ALIGN_LEFT)
    stream.write((formatRow("Ukuran Kertas", if (effectiveWidth <= 32) "58 mm" else "80 mm", effectiveWidth) + "\n").toByteArray(charset))
    stream.write((formatRow("Lebar Karakter", "$effectiveWidth char/line", effectiveWidth) + "\n").toByteArray(charset))
    stream.write((formatRow("Waktu Pengujian", formatDate(System.currentTimeMillis()), effectiveWidth) + "\n").toByteArray(charset))
    stream.write((formatRow("Protokol", "ESC/POS Thermal", effectiveWidth) + "\n").toByteArray(charset))
    stream.write((formatSeparator('-', effectiveWidth) + "\n").toByteArray(charset))

    stream.write(CMD_ALIGN_CENTER)
    stream.write(CMD_BOLD_ON)
    stream.write("PENGUJIAN BERHASIL!\n".toByteArray(charset))
    stream.write(CMD_BOLD_OFF)
    stream.write("Printer siap digunakan transaksi.\n\n".toByteArray(charset))

    stream.write((formatSeparator('-', effectiveWidth) + "\n").toByteArray(charset))
    stream.write(("- - - SOBEK DI SINI - - -\n").toByteArray(charset))
    stream.write(CMD_FEED_6)
    stream.write("\n\n\n\n\n".toByteArray(charset))

    return stream.toByteArray()
  }

  // --- TEXT REPRESENTATION FOR SCREEN PREVIEW & WHATSAPP SHARING ---

  fun generatePaymentReceiptText(
    store: StoreProfile,
    customer: CustomerEntity,
    payment: PaymentEntity,
    totalKasbon: Long,
    totalPaidBefore: Long,
    remainingDebt: Long,
    width: Int = store.charsPerLine
  ): String {
    val effectiveWidth = if (width > 0) width else 32
    val sep = formatSeparator('-', effectiveWidth)
    val doubleSep = formatSeparator('=', effectiveWidth)
    val statusText = if (remainingDebt <= 0) "*** LUNAS ***" else "BELUM LUNAS"

    return buildString {
      // 1. Header Toko
      appendLine(centerText(store.storeName, effectiveWidth))
      if (store.storeAddress.isNotEmpty()) appendLine(centerText(store.storeAddress, effectiveWidth))
      if (store.storePhone.isNotEmpty()) appendLine(centerText("Telp/WA: ${store.storePhone}", effectiveWidth))
      appendLine(doubleSep)

      // 2. Keterangan Bukti Transaksi
      appendLine(centerText("BUKTI PEMBAYARAN KASBON", effectiveWidth))
      appendLine(sep)

      // 3. Info Transaksi (Rata Kiri Kanan Rapi)
      appendLine(formatRow("No. Transaksi", payment.receiptNumber, effectiveWidth))
      appendLine(formatRow("Tanggal", formatDate(payment.paymentDate), effectiveWidth))
      appendLine(formatRow("Pelanggan", customer.name, effectiveWidth))
      if (customer.phone.isNotEmpty()) appendLine(formatRow("No. HP", customer.phone, effectiveWidth))
      appendLine(formatRow("Metode Bayar", payment.paymentMethod, effectiveWidth))
      if (payment.notes.isNotEmpty()) appendLine(formatRow("Keterangan", payment.notes, effectiveWidth))
      appendLine(sep)

      // 4. Rincian Nominal (Rata Kiri Kanan Presisi)
      appendLine(formatRow("Total Kasbon", formatRupiah(totalKasbon), effectiveWidth))
      appendLine(formatRow("Sudah Dibayar", formatRupiah(totalPaidBefore), effectiveWidth))
      appendLine(formatRow("Bayar Sekarang", formatRupiah(payment.amountPaid), effectiveWidth))
      appendLine(sep)
      appendLine(formatRow("SISA UTANG", formatRupiah(remainingDebt), effectiveWidth))
      appendLine(sep)

      // 5. Status
      appendLine(centerText("STATUS: $statusText", effectiveWidth))
      appendLine()

      // 6. Footer Toko & Catatan
      if (store.receiptFooter.isNotEmpty()) appendLine(centerText(store.receiptFooter, effectiveWidth))
      appendLine(centerText("Simpan struk ini sebagai bukti sah.", effectiveWidth))
      appendLine()

      // 7. Safe Tear Space
      appendLine(sep)
      appendLine(centerText("- - - SOBEK DI SINI - - -", effectiveWidth))
      appendLine()
      appendLine()
      appendLine()
    }
  }

  fun generateNewKasbonReceiptText(
    store: StoreProfile,
    customer: CustomerEntity,
    kasbon: KasbonEntity,
    totalCustomerDebt: Long,
    width: Int = store.charsPerLine
  ): String {
    val effectiveWidth = if (width > 0) width else 32
    val sep = formatSeparator('-', effectiveWidth)
    val doubleSep = formatSeparator('=', effectiveWidth)

    return buildString {
      // 1. Header Toko
      appendLine(centerText(store.storeName, effectiveWidth))
      if (store.storeAddress.isNotEmpty()) appendLine(centerText(store.storeAddress, effectiveWidth))
      if (store.storePhone.isNotEmpty()) appendLine(centerText("Telp/WA: ${store.storePhone}", effectiveWidth))
      appendLine(doubleSep)

      // 2. Keterangan Bukti Transaksi
      appendLine(centerText("BUKTI CATATAN KASBON BARU", effectiveWidth))
      appendLine(sep)

      // 3. Info Kasbon (Rata Kiri Kanan Rapi)
      appendLine(formatRow("No. Kasbon", kasbon.invoiceNumber, effectiveWidth))
      appendLine(formatRow("Tanggal", formatDate(kasbon.date), effectiveWidth))
      appendLine(formatRow("Jatuh Tempo", formatDate(kasbon.dueDate), effectiveWidth))
      appendLine(formatRow("Pelanggan", customer.name, effectiveWidth))
      if (kasbon.note.isNotEmpty()) appendLine(formatRow("Rincian", kasbon.note, effectiveWidth))
      appendLine(sep)

      // 4. Nominal (Rata Kiri Kanan Presisi)
      appendLine(formatRow("Nominal Kasbon", formatRupiah(kasbon.amount), effectiveWidth))
      appendLine(formatRow("Total Utang", formatRupiah(totalCustomerDebt), effectiveWidth))
      appendLine(sep)

      // 5. Footer & Safe Tear Space
      appendLine(centerText("Mohon lakukan pembayaran sebelum", effectiveWidth))
      appendLine(centerText("tanggal jatuh tempo.", effectiveWidth))
      if (store.storePhone.isNotEmpty()) appendLine(centerText("Konfirmasi WA: ${store.storePhone}", effectiveWidth))
      appendLine()
      appendLine(sep)
      appendLine(centerText("- - - SOBEK DI SINI - - -", effectiveWidth))
      appendLine()
      appendLine()
      appendLine()
    }
  }

  fun generateCustomerSummaryText(
    store: StoreProfile,
    customer: CustomerEntity,
    kasbons: List<KasbonEntity>,
    totalDebt: Long,
    width: Int = store.charsPerLine
  ): String {
    val effectiveWidth = if (width > 0) width else 32
    val sep = formatSeparator('-', effectiveWidth)
    val doubleSep = formatSeparator('=', effectiveWidth)

    return buildString {
      appendLine(centerText(store.storeName, effectiveWidth))
      if (store.storeAddress.isNotEmpty()) appendLine(centerText(store.storeAddress, effectiveWidth))
      if (store.storePhone.isNotEmpty()) appendLine(centerText("Telp/WA: ${store.storePhone}", effectiveWidth))
      appendLine(doubleSep)

      appendLine(centerText("BUKTI REKAP TAGIHAN KASBON", effectiveWidth))
      appendLine(sep)

      appendLine(formatRow("Pelanggan", customer.name, effectiveWidth))
      if (customer.phone.isNotEmpty()) appendLine(formatRow("No. HP", customer.phone, effectiveWidth))
      appendLine(formatRow("Tanggal", formatDate(System.currentTimeMillis()), effectiveWidth))
      appendLine(sep)

      val unpaidKasbons = kasbons.filter { it.status != KasbonEntity.STATUS_PAID }
      if (unpaidKasbons.isEmpty()) {
        appendLine(centerText("*** TIDAK ADA UTANG AKTIF ***", effectiveWidth))
      } else {
        unpaidKasbons.forEachIndexed { index, k ->
          appendLine("${index + 1}. ${k.note.take(24)}")
          appendLine("   Tempo: ${SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(k.dueDate))}")
          appendLine(formatRow("   Sisa Kasbon", formatRupiah(k.remainingAmount), effectiveWidth))
        }
      }
      appendLine(sep)
      appendLine(formatRow("TOTAL TAGIHAN", formatRupiah(totalDebt), effectiveWidth))
      appendLine(sep)

      appendLine(centerText("Terima kasih atas kerjasamanya.", effectiveWidth))
      if (store.storePhone.isNotEmpty()) appendLine(centerText("Hubungi: ${store.storePhone}", effectiveWidth))
      appendLine()
      appendLine(sep)
      appendLine(centerText("- - - SOBEK DI SINI - - -", effectiveWidth))
      appendLine()
      appendLine()
      appendLine()
    }
  }
}
