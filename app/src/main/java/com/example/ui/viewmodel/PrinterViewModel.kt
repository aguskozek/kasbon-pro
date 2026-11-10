package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entity.CustomerEntity
import com.example.data.entity.KasbonEntity
import com.example.data.entity.PaymentEntity
import com.example.data.pref.StoreProfile
import com.example.data.pref.UserPreferencesRepository
import com.example.printer.BluetoothPrinterManager
import com.example.printer.PairedBluetoothDevice
import com.example.printer.PrinterStatus
import com.example.printer.ReceiptPrintService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class PrinterViewModel(
  application: Application,
  private val preferencesRepository: UserPreferencesRepository
) : AndroidViewModel(application) {

  val printerManager = BluetoothPrinterManager(application)
  val printService = ReceiptPrintService(printerManager)

  val printerStatus: StateFlow<PrinterStatus> = printerManager.status

  private val _pairedDevices = MutableStateFlow<List<PairedBluetoothDevice>>(emptyList())
  val pairedDevices: StateFlow<List<PairedBluetoothDevice>> = _pairedDevices.asStateFlow()

  private val _message = MutableStateFlow<String?>(null)
  val message: StateFlow<String?> = _message.asStateFlow()

  init {
    refreshPairedDevices()
    autoConnectSavedPrinter()
  }

  fun refreshPairedDevices() {
    _pairedDevices.value = printerManager.getPairedDevices()
  }

  private fun autoConnectSavedPrinter() {
    viewModelScope.launch {
      val profile = preferencesRepository.storeProfile.first()
      if (profile.lastPrinterAddress.isNotEmpty() && printerManager.status.value is PrinterStatus.Disconnected) {
        printerManager.connect(profile.lastPrinterAddress)
      }
    }
  }

  fun connectPrinter(device: PairedBluetoothDevice) {
    viewModelScope.launch {
      val result = printerManager.connect(device.address)
      if (result.isSuccess) {
        preferencesRepository.saveLastPrinter(device.address, device.name)
        _message.value = "Terhubung ke printer ${device.name}"
      } else {
        _message.value = result.exceptionOrNull()?.message ?: "Gagal terhubung ke printer"
      }
    }
  }

  fun disconnectPrinter() {
    viewModelScope.launch {
      printerManager.disconnect()
      _message.value = "Printer terputus"
    }
  }

  fun testPrint(store: StoreProfile) {
    viewModelScope.launch {
      val result = printService.printTestReceipt(store)
      if (result.isSuccess) {
        _message.value = "Tes cetak terkirim ke printer!"
      } else {
        _message.value = "Gagal mencetak: ${result.exceptionOrNull()?.message}"
      }
    }
  }

  fun printPaymentReceipt(
    store: StoreProfile,
    customer: CustomerEntity,
    payment: PaymentEntity,
    totalKasbon: Long,
    totalPaidBefore: Long,
    remainingDebt: Long,
    onComplete: (Boolean, String) -> Unit = { _, _ -> }
  ) {
    viewModelScope.launch {
      val result = printService.printPaymentReceipt(
        store = store,
        customer = customer,
        payment = payment,
        totalKasbon = totalKasbon,
        totalPaidBefore = totalPaidBefore,
        remainingDebt = remainingDebt
      )
      if (result.isSuccess) {
        onComplete(true, "Struk pembayaran berhasil dicetak!")
      } else {
        onComplete(false, result.exceptionOrNull()?.message ?: "Gagal mencetak struk")
      }
    }
  }

  fun printNewKasbonReceipt(
    store: StoreProfile,
    customer: CustomerEntity,
    kasbon: KasbonEntity,
    totalCustomerDebt: Long,
    onComplete: (Boolean, String) -> Unit = { _, _ -> }
  ) {
    viewModelScope.launch {
      val result = printService.printNewKasbonReceipt(
        store = store,
        customer = customer,
        kasbon = kasbon,
        totalCustomerDebt = totalCustomerDebt
      )
      if (result.isSuccess) {
        onComplete(true, "Struk kasbon baru berhasil dicetak!")
      } else {
        onComplete(false, result.exceptionOrNull()?.message ?: "Gagal mencetak struk")
      }
    }
  }

  fun clearMessage() {
    _message.value = null
  }
}
