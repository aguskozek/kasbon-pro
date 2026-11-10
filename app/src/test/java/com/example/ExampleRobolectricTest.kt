package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("KHM Kasbon Pro", appName)
  }

  @Test
  fun `verify rupiah formatting helper`() {
    val formatted = com.example.printer.EscPosReceiptFormatter.formatRupiah(1500000L)
    org.junit.Assert.assertTrue(formatted.contains("1.500.000"))
  }

  @Test
  fun `verify formatRow alignment for 58mm and 80mm`() {
    // 58mm (32 chars)
    val row58 = com.example.printer.EscPosReceiptFormatter.formatRow("Total Kasbon", "Rp 500.000", 32)
    assertEquals(32, row58.length)
    org.junit.Assert.assertTrue(row58.startsWith("Total Kasbon"))
    org.junit.Assert.assertTrue(row58.endsWith("Rp 500.000"))

    // 80mm (48 chars)
    val row80 = com.example.printer.EscPosReceiptFormatter.formatRow("Total Kasbon", "Rp 500.000", 48)
    assertEquals(48, row80.length)
    org.junit.Assert.assertTrue(row80.startsWith("Total Kasbon"))
    org.junit.Assert.assertTrue(row80.endsWith("Rp 500.000"))
  }
}
