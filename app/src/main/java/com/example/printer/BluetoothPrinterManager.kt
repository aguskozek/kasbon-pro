package com.example.printer

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

sealed class PrinterStatus {
  object Disconnected : PrinterStatus()
  data class Connecting(val deviceName: String) : PrinterStatus()
  data class Connected(val deviceName: String, val deviceAddress: String) : PrinterStatus()
  data class Error(val message: String) : PrinterStatus()
}

data class PairedBluetoothDevice(
  val name: String,
  val address: String
)

class BluetoothPrinterManager(private val context: Context) {
  companion object {
    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
  }

  private val bluetoothManager: BluetoothManager? =
    context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
  private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

  private var bluetoothSocket: BluetoothSocket? = null
  private var outputStream: OutputStream? = null

  private val _status = MutableStateFlow<PrinterStatus>(PrinterStatus.Disconnected)
  val status: StateFlow<PrinterStatus> = _status.asStateFlow()

  fun hasBluetoothPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) ==
          PackageManager.PERMISSION_GRANTED
    } else {
      true
    }
  }

  fun isBluetoothSupported(): Boolean = bluetoothAdapter != null

  fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

  @SuppressLint("MissingPermission")
  fun getPairedDevices(): List<PairedBluetoothDevice> {
    if (!hasBluetoothPermission() || bluetoothAdapter == null) {
      return emptyList()
    }
    return try {
      bluetoothAdapter.bondedDevices?.map { device ->
        PairedBluetoothDevice(
          name = device.name ?: "Unknown Printer",
          address = device.address
        )
      } ?: emptyList()
    } catch (_: SecurityException) {
      emptyList()
    }
  }

  @SuppressLint("MissingPermission")
  suspend fun connect(address: String): Result<String> = withContext(Dispatchers.IO) {
    if (!isBluetoothSupported()) {
      val err = "Perangkat tidak mendukung Bluetooth."
      _status.value = PrinterStatus.Error(err)
      return@withContext Result.failure(Exception(err))
    }

    if (!isBluetoothEnabled()) {
      val err = "Bluetooth sedang nonaktif. Silakan aktifkan Bluetooth."
      _status.value = PrinterStatus.Error(err)
      return@withContext Result.failure(Exception(err))
    }

    if (!hasBluetoothPermission()) {
      val err = "Izin Bluetooth belum diberikan."
      _status.value = PrinterStatus.Error(err)
      return@withContext Result.failure(Exception(err))
    }

    disconnectInternal()

    return@withContext try {
      val device: BluetoothDevice = bluetoothAdapter!!.getRemoteDevice(address)
      val devName = try { device.name ?: "Printer 58mm" } catch (_: Exception) { "Printer 58mm" }
      _status.value = PrinterStatus.Connecting(devName)

      // Cancel discovery if running to save speed & radio bandwidth
      try {
        bluetoothAdapter.cancelDiscovery()
      } catch (_: Exception) {}

      val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
      socket.connect()

      bluetoothSocket = socket
      outputStream = socket.outputStream

      _status.value = PrinterStatus.Connected(devName, address)
      Result.success(devName)
    } catch (e: Exception) {
      disconnectInternal()
      val msg = "Gagal menghubungkan ke printer: ${e.localizedMessage ?: "Timeout / Printer mati"}"
      _status.value = PrinterStatus.Error(msg)
      Result.failure(Exception(msg))
    }
  }

  suspend fun printData(data: ByteArray): Result<Unit> = withContext(Dispatchers.IO) {
    val currentStream = outputStream
    val currentSocket = bluetoothSocket

    if (currentStream == null || currentSocket == null || !currentSocket.isConnected) {
      val err = "Printer belum terhubung. Silakan hubungkan printer di menu Pengaturan."
      _status.value = PrinterStatus.Error(err)
      return@withContext Result.failure(IllegalStateException(err))
    }

    return@withContext try {
      currentStream.write(data)
      currentStream.flush()
      Result.success(Unit)
    } catch (e: IOException) {
      disconnectInternal()
      val err = "Koneksi terputus saat mencetak: ${e.localizedMessage}"
      _status.value = PrinterStatus.Error(err)
      Result.failure(e)
    }
  }

  suspend fun disconnect() = withContext(Dispatchers.IO) {
    disconnectInternal()
    _status.value = PrinterStatus.Disconnected
  }

  private fun disconnectInternal() {
    try {
      outputStream?.close()
    } catch (_: Exception) {}
    try {
      bluetoothSocket?.close()
    } catch (_: Exception) {}
    outputStream = null
    bluetoothSocket = null
  }
}
