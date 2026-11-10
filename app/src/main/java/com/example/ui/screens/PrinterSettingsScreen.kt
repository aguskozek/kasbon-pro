package com.example.ui.screens

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.printer.PrinterStatus
import com.example.ui.components.KasbonTopBar
import com.example.ui.theme.StatusColors
import com.example.ui.viewmodel.KasbonViewModel
import com.example.ui.viewmodel.PrinterViewModel

@Composable
fun PrinterSettingsScreen(
  viewModel: KasbonViewModel,
  printerViewModel: PrinterViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val printerStatus by printerViewModel.printerStatus.collectAsState()
  val pairedDevices by printerViewModel.pairedDevices.collectAsState()
  val storeProfile by viewModel.storeProfile.collectAsState()
  val colors = StatusColors

  val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    if (isGranted) {
      printerViewModel.refreshPairedDevices()
    } else {
      Toast.makeText(context, "Izin Bluetooth ditolak. Perlu izin untuk mendeteksi printer.", Toast.LENGTH_SHORT).show()
    }
  }

  BackHandler {
    viewModel.navigateBack()
  }

  Scaffold(
    topBar = {
      KasbonTopBar(
        title = "Printer Bluetooth 58mm",
        subtitle = "Konfigurasi & Pengujian Cetak",
        showBackButton = true,
        onBackClick = { viewModel.navigateBack() },
        actions = {
          IconButton(onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !printerViewModel.printerManager.hasBluetoothPermission()) {
              permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            } else {
              printerViewModel.refreshPairedDevices()
            }
          }) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Pindai Ulang")
          }
        }
      )
    },
    modifier = modifier.fillMaxSize()
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .background(MaterialTheme.colorScheme.background),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 1. Connection Status Banner
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = when (printerStatus) {
              is PrinterStatus.Connected -> colors.successContainer
              is PrinterStatus.Connecting -> colors.warningContainer
              is PrinterStatus.Error -> colors.dangerContainer
              else -> MaterialTheme.colorScheme.surface
            }
          ),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(46.dp)
                  .clip(CircleShape)
                  .background(Color.White.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = when (printerStatus) {
                    is PrinterStatus.Connected -> Icons.Default.BluetoothConnected
                    is PrinterStatus.Connecting -> Icons.Default.Bluetooth
                    is PrinterStatus.Error -> Icons.Default.Warning
                    else -> Icons.Default.BluetoothDisabled
                  },
                  contentDescription = null,
                  tint = when (printerStatus) {
                    is PrinterStatus.Connected -> colors.success
                    is PrinterStatus.Connecting -> colors.warning
                    is PrinterStatus.Error -> colors.danger
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                  },
                  modifier = Modifier.size(26.dp)
                )
              }
              Spacer(modifier = Modifier.width(14.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Status Koneksi Printer",
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = when (val s = printerStatus) {
                    is PrinterStatus.Connected -> "Terhubung: ${s.deviceName}"
                    is PrinterStatus.Connecting -> "Menghubungkan ke ${s.deviceName}..."
                    is PrinterStatus.Error -> "Koneksi Gagal: ${s.message}"
                    else -> "Belum Terhubung"
                  },
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              if (printerStatus is PrinterStatus.Connected) {
                OutlinedButton(
                  onClick = { printerViewModel.disconnectPrinter() },
                  modifier = Modifier.weight(1f)
                ) {
                  Text("Putuskan Koneksi")
                }
              }

              Button(
                onClick = {
                  printerViewModel.testPrint(storeProfile)
                },
                enabled = printerStatus is PrinterStatus.Connected,
                modifier = Modifier
                  .weight(1f)
                  .testTag("btn_test_print")
              ) {
                Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tes Cetak")
              }
            }
          }
        }
      }

      // 2. Hardware Specs Card (58mm ESC/POS)
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Spesifikasi & Konfigurasi Kertas",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text("Ukuran Kertas Thermal", style = MaterialTheme.typography.bodyMedium)
                Text("Standar kasir / printer mini POS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              Text("58 mm", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
              Text(
                text = "Ukuran Kertas & Lebar Karakter",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = "Sesuaikan dengan ukuran kertas printer thermal Anda",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(8.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                // Pilihan 58 mm
                val is58mm = storeProfile.charsPerLine <= 32
                Card(
                  modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.updatePaperWidth(32) }
                    .border(
                      width = if (is58mm) 2.dp else 1.dp,
                      color = if (is58mm) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                      shape = RoundedCornerShape(10.dp)
                    ),
                  shape = RoundedCornerShape(10.dp),
                  colors = CardDefaults.cardColors(
                    containerColor = if (is58mm) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                  )
                ) {
                  Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                  ) {
                    Text(
                      text = "58 mm",
                      fontWeight = FontWeight.Bold,
                      style = MaterialTheme.typography.titleMedium,
                      color = if (is58mm) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                      text = "32 Karakter (Standar)",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }

                // Pilihan 80 mm
                val is80mm = storeProfile.charsPerLine >= 48
                Card(
                  modifier = Modifier
                    .weight(1f)
                    .clickable { viewModel.updatePaperWidth(48) }
                    .border(
                      width = if (is80mm) 2.dp else 1.dp,
                      color = if (is80mm) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                      shape = RoundedCornerShape(10.dp)
                    ),
                  shape = RoundedCornerShape(10.dp),
                  colors = CardDefaults.cardColors(
                    containerColor = if (is80mm) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                  )
                ) {
                  Column(
                    modifier = Modifier.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                  ) {
                    Text(
                      text = "80 mm",
                      fontWeight = FontWeight.Bold,
                      style = MaterialTheme.typography.titleMedium,
                      color = if (is80mm) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                      text = "48 Karakter (Lebar)",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }
              }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text("Cetak Otomatis Setelah Pembayaran", style = MaterialTheme.typography.bodyMedium)
                Text("Langsung kirim ke printer saat simpan", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              Switch(
                checked = storeProfile.autoPrint,
                onCheckedChange = { checked ->
                  viewModel.updateStoreProfile(
                    storeProfile.storeName,
                    storeProfile.storeAddress,
                    storeProfile.storePhone,
                    storeProfile.receiptHeader,
                    storeProfile.receiptFooter,
                    checked,
                    storeProfile.printCopies,
                    storeProfile.charsPerLine
                  )
                }
              )
            }
          }
        }
      }

      // 3. Paired Devices List
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Pilih Perangkat Printer Bluetooth",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "${pairedDevices.size} Ditemukan",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      if (pairedDevices.isEmpty()) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
              )
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = "Tidak Ada Printer yang Dipasangkan",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "1. Buka Pengaturan Bluetooth HP Anda.\n2. Pasangkan (Pair) printer thermal 58mm (PIN biasanya 0000 atau 1234).\n3. Kembali ke aplikasi dan tekan tombol 'Pindai Ulang'.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(14.dp))
              Button(
                onClick = {
                  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !printerViewModel.printerManager.hasBluetoothPermission()) {
                    permissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                  } else {
                    printerViewModel.refreshPairedDevices()
                  }
                }
              ) {
                Text("Pindai Perangkat Paired")
              }
            }
          }
        }
      } else {
        items(pairedDevices) { device ->
          val isConnectedThis = when (val s = printerStatus) {
            is PrinterStatus.Connected -> s.deviceAddress == device.address
            else -> false
          }
          val isSaved = storeProfile.lastPrinterAddress == device.address

          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                printerViewModel.connectPrinter(device)
              }
              .testTag("device_${device.address}"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isConnectedThis) colors.successContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                      if (isConnectedThis) colors.success.copy(alpha = 0.2f)
                      else MaterialTheme.colorScheme.primaryContainer
                    ),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Print,
                    contentDescription = null,
                    tint = if (isConnectedThis) colors.success else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                  )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = device.name,
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold
                    )
                    if (isSaved) {
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(
                        text = "(Terakhir Digunakan)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                      )
                    }
                  }
                  Text(
                    text = device.address,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              Button(
                onClick = {
                  if (isConnectedThis) {
                    printerViewModel.testPrint(storeProfile)
                  } else {
                    printerViewModel.connectPrinter(device)
                  }
                },
                colors = if (isConnectedThis) ButtonDefaults.buttonColors(containerColor = colors.success) else ButtonDefaults.buttonColors(),
                shape = RoundedCornerShape(10.dp)
              ) {
                Text(if (isConnectedThis) "Tes" else "Hubungkan", fontSize = 12.sp)
              }
            }
          }
        }
      }

      item { Spacer(modifier = Modifier.height(60.dp)) }
    }
  }
}
