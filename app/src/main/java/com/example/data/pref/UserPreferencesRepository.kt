package com.example.data.pref

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "khm_kasbon_settings")

data class StoreProfile(
  val storeName: String = "TOKO & BENGKEL KHM",
  val storeAddress: String = "Jl. Pasar Baru No. 45",
  val storePhone: String = "081234567890",
  val receiptHeader: String = "KHM KASBON PRO",
  val receiptFooter: String = "Terima kasih atas pembayaran Anda.",
  val autoPrint: Boolean = false,
  val printCopies: Int = 1,
  val lastPrinterAddress: String = "",
  val lastPrinterName: String = "",
  val charsPerLine: Int = 32,
  val logoPath: String = "",
  val qrisImagePath: String = "",
  val bankName: String = "BCA",
  val bankAccountNumber: String = "8923456789",
  val bankAccountHolder: String = "KHM KASBON PRO"
)

class UserPreferencesRepository(private val context: Context) {
  companion object {
    val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    val STORE_NAME_KEY = stringPreferencesKey("store_name")
    val STORE_ADDRESS_KEY = stringPreferencesKey("store_address")
    val STORE_PHONE_KEY = stringPreferencesKey("store_phone")
    val RECEIPT_HEADER_KEY = stringPreferencesKey("receipt_header")
    val RECEIPT_FOOTER_KEY = stringPreferencesKey("receipt_footer")
    val AUTO_PRINT_KEY = booleanPreferencesKey("auto_print")
    val PRINT_COPIES_KEY = intPreferencesKey("print_copies")
    val LAST_PRINTER_ADDRESS_KEY = stringPreferencesKey("last_printer_address")
    val LAST_PRINTER_NAME_KEY = stringPreferencesKey("last_printer_name")
    val CHARS_PER_LINE_KEY = intPreferencesKey("chars_per_line")
    val STORE_LOGO_PATH_KEY = stringPreferencesKey("store_logo_path")
    val QRIS_IMAGE_PATH_KEY = stringPreferencesKey("qris_image_path")
    val BANK_NAME_KEY = stringPreferencesKey("bank_name")
    val BANK_ACCOUNT_NUMBER_KEY = stringPreferencesKey("bank_account_number")
    val BANK_ACCOUNT_HOLDER_KEY = stringPreferencesKey("bank_account_holder")
  }

  val themeMode: Flow<AppThemeMode> = context.dataStore.data.map { preferences ->
    val modeStr = preferences[THEME_MODE_KEY] ?: AppThemeMode.SYSTEM.name
    try {
      AppThemeMode.valueOf(modeStr)
    } catch (_: Exception) {
      AppThemeMode.SYSTEM
    }
  }

  val storeProfile: Flow<StoreProfile> = context.dataStore.data.map { preferences ->
    StoreProfile(
      storeName = preferences[STORE_NAME_KEY] ?: "TOKO & BENGKEL KHM",
      storeAddress = preferences[STORE_ADDRESS_KEY] ?: "Jl. Pasar Baru No. 45",
      storePhone = preferences[STORE_PHONE_KEY] ?: "081234567890",
      receiptHeader = preferences[RECEIPT_HEADER_KEY] ?: "KHM KASBON PRO",
      receiptFooter = preferences[RECEIPT_FOOTER_KEY] ?: "Terima kasih atas pembayaran Anda.",
      autoPrint = preferences[AUTO_PRINT_KEY] ?: false,
      printCopies = preferences[PRINT_COPIES_KEY] ?: 1,
      lastPrinterAddress = preferences[LAST_PRINTER_ADDRESS_KEY] ?: "",
      lastPrinterName = preferences[LAST_PRINTER_NAME_KEY] ?: "",
      charsPerLine = preferences[CHARS_PER_LINE_KEY] ?: 32,
      logoPath = preferences[STORE_LOGO_PATH_KEY] ?: "",
      qrisImagePath = preferences[QRIS_IMAGE_PATH_KEY] ?: "",
      bankName = preferences[BANK_NAME_KEY] ?: "BCA",
      bankAccountNumber = preferences[BANK_ACCOUNT_NUMBER_KEY] ?: "8923456789",
      bankAccountHolder = preferences[BANK_ACCOUNT_HOLDER_KEY] ?: "KHM KASBON PRO"
    )
  }

  suspend fun setThemeMode(mode: AppThemeMode) {
    context.dataStore.edit { preferences ->
      preferences[THEME_MODE_KEY] = mode.name
    }
  }

  suspend fun updateStoreProfile(
    storeName: String,
    storeAddress: String,
    storePhone: String,
    receiptHeader: String,
    receiptFooter: String,
    autoPrint: Boolean,
    printCopies: Int,
    charsPerLine: Int,
    logoPath: String = "",
    qrisImagePath: String = "",
    bankName: String = "BCA",
    bankAccountNumber: String = "",
    bankAccountHolder: String = ""
  ) {
    context.dataStore.edit { preferences ->
      preferences[STORE_NAME_KEY] = storeName
      preferences[STORE_ADDRESS_KEY] = storeAddress
      preferences[STORE_PHONE_KEY] = storePhone
      preferences[RECEIPT_HEADER_KEY] = receiptHeader
      preferences[RECEIPT_FOOTER_KEY] = receiptFooter
      preferences[AUTO_PRINT_KEY] = autoPrint
      preferences[PRINT_COPIES_KEY] = printCopies
      preferences[CHARS_PER_LINE_KEY] = charsPerLine
      preferences[STORE_LOGO_PATH_KEY] = logoPath
      preferences[QRIS_IMAGE_PATH_KEY] = qrisImagePath
      preferences[BANK_NAME_KEY] = bankName
      preferences[BANK_ACCOUNT_NUMBER_KEY] = bankAccountNumber
      preferences[BANK_ACCOUNT_HOLDER_KEY] = bankAccountHolder
    }
  }

  suspend fun saveStoreLogo(path: String) {
    context.dataStore.edit { preferences ->
      preferences[STORE_LOGO_PATH_KEY] = path
    }
  }

  suspend fun saveLastPrinter(address: String, name: String) {
    context.dataStore.edit { preferences ->
      preferences[LAST_PRINTER_ADDRESS_KEY] = address
      preferences[LAST_PRINTER_NAME_KEY] = name
    }
  }

  suspend fun updatePaperWidth(charsPerLine: Int) {
    context.dataStore.edit { preferences ->
      preferences[CHARS_PER_LINE_KEY] = charsPerLine
    }
  }
}
