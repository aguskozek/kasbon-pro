package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import com.example.ui.components.KasbonTopBar
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.StatusColors
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.KasbonViewModel

@Composable
fun SettingsScreen(
  viewModel: KasbonViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val themeMode by viewModel.themeMode.collectAsState()
  val storeProfile by viewModel.storeProfile.collectAsState()
  val colors = StatusColors

  var showStoreProfileDialog by remember { mutableStateOf(false) }

  // File picker launcher for importing JSON backup
  val importFileLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.GetContent()
  ) { uri ->
    if (uri != null) {
      try {
        context.contentResolver.openInputStream(uri)?.use { stream ->
          val jsonStr = stream.bufferedReader().use { it.readText() }
          viewModel.importBackup(jsonStr)
        }
      } catch (e: Exception) {
        Toast.makeText(context, "Gagal membaca berkas: ${e.message}", Toast.LENGTH_SHORT).show()
      }
    }
  }

  Scaffold(
    topBar = {
      KasbonTopBar(
        title = "Pengaturan",
        subtitle = "Tema, Toko, Printer & Cadangan"
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
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Theme Selection Card (Light, Dark, System)
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Tampilan & Tema Aplikasi",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Pilih tema terang, gelap, atau ikuti pengaturan HP",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              ThemeOptionChip(
                label = "Terang",
                icon = Icons.Default.LightMode,
                selected = themeMode == AppThemeMode.LIGHT,
                onClick = { viewModel.setThemeMode(AppThemeMode.LIGHT) },
                modifier = Modifier.weight(1f)
              )
              ThemeOptionChip(
                label = "Gelap",
                icon = Icons.Default.DarkMode,
                selected = themeMode == AppThemeMode.DARK,
                onClick = { viewModel.setThemeMode(AppThemeMode.DARK) },
                modifier = Modifier.weight(1f)
              )
              ThemeOptionChip(
                label = "Sistem",
                icon = Icons.Default.BrightnessAuto,
                selected = themeMode == AppThemeMode.SYSTEM,
                onClick = { viewModel.setThemeMode(AppThemeMode.SYSTEM) },
                modifier = Modifier.weight(1f)
              )
            }
          }
        }
      }

      // 2. Printer Bluetooth Menu
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.navigateTo(AppScreen.PrinterSettings) }
            .testTag("menu_printer_settings"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Print,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Printer Bluetooth 58mm",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = if (storeProfile.lastPrinterName.isNotEmpty()) "Tersimpan: ${storeProfile.lastPrinterName}" else "Atur koneksi printer thermal",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Icon(
              imageVector = Icons.Default.ChevronRight,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      // 3. Pilihan Metode Pembayaran (Tunai, QRIS, Transfer Bank) - Reference Feature
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { showStoreProfileDialog = true }
            .testTag("menu_payment_methods"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color(0xFFF59E0B).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.CreditCard,
                  contentDescription = null,
                  tint = Color(0xFFF59E0B),
                  modifier = Modifier.size(24.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Pilihan Metode Pembayaran",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Tunai (Kalkulator) • QRIS Barcode • Transfer Bank",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // 3 feature items
            Row(verticalAlignment = Alignment.Top) {
              Text("1. ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
              Text("Tunai (Cash): Otomatis hitung uang diterima & kembalian.", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Top) {
              Text("2. ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
              Text(
                text = "QRIS: ${if (storeProfile.qrisImagePath.isNotEmpty()) "Barcode toko aktif" else "Unggah barcode QRIS toko Anda agar pelanggan scan"}.",
                style = MaterialTheme.typography.bodySmall
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Top) {
              Text("3. ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
              Text(
                text = "Transfer Bank: ${storeProfile.bankName} ${storeProfile.bankAccountNumber} (A/N ${storeProfile.bankAccountHolder})",
                style = MaterialTheme.typography.bodySmall
              )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              listOf("Tunai", "QRIS Dinamis", "Transfer Bank").forEach { tag ->
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ) {
                  Text(
                    text = tag,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }
            }
          }
        }
      }

      // 4. Store Profile & Struk Header/Footer
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { showStoreProfileDialog = true }
            .testTag("menu_store_profile"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            val logoFile = remember(storeProfile.logoPath) {
              if (storeProfile.logoPath.isNotEmpty()) java.io.File(storeProfile.logoPath) else null
            }
            val logoBitmap = remember(logoFile) {
              if (logoFile != null && logoFile.exists()) {
                android.graphics.BitmapFactory.decodeFile(logoFile.absolutePath)
              } else null
            }

            if (logoBitmap != null) {
              androidx.compose.foundation.Image(
                bitmap = logoBitmap.asImageBitmap(),
                contentDescription = "Logo Usaha",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier
                  .size(48.dp)
                  .clip(RoundedCornerShape(12.dp))
              )
            } else {
              Box(
                modifier = Modifier
                  .size(48.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF6366F1).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Store,
                  contentDescription = null,
                  tint = Color(0xFF6366F1),
                  modifier = Modifier.size(24.dp)
                )
              }
            }

            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Profil Usaha & Format Struk",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "${storeProfile.storeName} • ${if (storeProfile.logoPath.isNotEmpty()) "Logo Terpasang" else "Atur Logo & Kontak"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Icon(
              imageVector = Icons.Default.ChevronRight,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      // 5. Backup & Restore (Google Drive & JSON)
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Pencadangan Data (Google Drive & Offline)",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Simpan berkas cadangan database kasbon ke Google Drive atau memori perangkat",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Button(
                onClick = {
                  viewModel.exportBackup { file ->
                    viewModel.backupManager.shareBackupToGoogleDriveOrApps(file)
                  }
                },
                modifier = Modifier
                  .weight(1f)
                  .testTag("btn_export_backup")
              ) {
                Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Cadangkan Data")
              }

              OutlinedButton(
                onClick = {
                  importFileLauncher.launch("application/json")
                },
                modifier = Modifier
                  .weight(1f)
                  .testTag("btn_import_backup")
              ) {
                Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Pulihkan")
              }
            }
          }
        }
      }

      // 6. About App Card
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = "KHM KASBON PRO",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
          )
          Text(
            text = "Versi 1.0 • Offline Ready & Bluetooth Printing",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "Dirancang untuk Bengkel, Warung, Toko & UMKM Indonesia",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      item { Spacer(modifier = Modifier.height(60.dp)) }
    }
  }

  if (showStoreProfileDialog) {
    StoreProfileDialog(
      currentProfile = storeProfile,
      onDismiss = { showStoreProfileDialog = false },
      onSave = { name, address, phone, header, footer, autoPrint, copies, charsPerLine, logoPath, qrisPath, bankName, bankAccNum, bankAccHolder ->
        viewModel.updateStoreProfile(
          name = name,
          address = address,
          phone = phone,
          header = header,
          footer = footer,
          autoPrint = autoPrint,
          copies = copies,
          charsPerLine = charsPerLine,
          logoPath = logoPath,
          qrisImagePath = qrisPath,
          bankName = bankName,
          bankAccountNumber = bankAccNum,
          bankAccountHolder = bankAccHolder
        )
        showStoreProfileDialog = false
      }
    )
  }
}

@Composable
fun ThemeOptionChip(
  label: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.clickable { onClick() },
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
      else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ),
    border = if (selected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(24.dp)
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
      )
    }
  }
}

@Composable
fun StoreProfileDialog(
  currentProfile: com.example.data.pref.StoreProfile,
  onDismiss: () -> Unit,
  onSave: (String, String, String, String, String, Boolean, Int, Int, String, String, String, String, String) -> Unit
) {
  val context = LocalContext.current
  var name by remember { mutableStateOf(currentProfile.storeName) }
  var address by remember { mutableStateOf(currentProfile.storeAddress) }
  var phone by remember { mutableStateOf(currentProfile.storePhone) }
  var header by remember { mutableStateOf(currentProfile.receiptHeader) }
  var footer by remember { mutableStateOf(currentProfile.receiptFooter) }
  var copies by remember { mutableIntStateOf(currentProfile.printCopies) }
  var charsPerLine by remember { mutableIntStateOf(currentProfile.charsPerLine) }
  var currentLogoPath by remember { mutableStateOf(currentProfile.logoPath) }
  var currentQrisPath by remember { mutableStateOf(currentProfile.qrisImagePath) }
  var bankName by remember { mutableStateOf(currentProfile.bankName) }
  var bankAccountNumber by remember { mutableStateOf(currentProfile.bankAccountNumber) }
  var bankAccountHolder by remember { mutableStateOf(currentProfile.bankAccountHolder) }

  val photoPickerLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      try {
        val destFile = java.io.File(context.filesDir, "store_logo_${System.currentTimeMillis()}.png")
        context.contentResolver.openInputStream(uri)?.use { input ->
          destFile.outputStream().use { output ->
            input.copyTo(output)
          }
        }
        currentLogoPath = destFile.absolutePath
        Toast.makeText(context, "Logo usaha berhasil dipilih!", Toast.LENGTH_SHORT).show()
      } catch (e: Exception) {
        Toast.makeText(context, "Gagal memuat logo: ${e.message}", Toast.LENGTH_SHORT).show()
      }
    }
  }

  val qrisPickerLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      try {
        val destFile = java.io.File(context.filesDir, "store_qris_${System.currentTimeMillis()}.png")
        context.contentResolver.openInputStream(uri)?.use { input ->
          destFile.outputStream().use { output ->
            input.copyTo(output)
          }
        }
        currentQrisPath = destFile.absolutePath
        Toast.makeText(context, "Barcode QRIS berhasil diperbarui!", Toast.LENGTH_SHORT).show()
      } catch (e: Exception) {
        Toast.makeText(context, "Gagal memuat QRIS: ${e.message}", Toast.LENGTH_SHORT).show()
      }
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Pengaturan Profil Usaha & Struk",
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleLarge
      )
    },
    text = {
      androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // 1. Logo Section
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              val logoFile = remember(currentLogoPath) {
                if (currentLogoPath.isNotEmpty()) java.io.File(currentLogoPath) else null
              }
              val bitmap = remember(logoFile) {
                if (logoFile != null && logoFile.exists()) {
                  android.graphics.BitmapFactory.decodeFile(logoFile.absolutePath)
                } else null
              }

              if (bitmap != null) {
                androidx.compose.foundation.Image(
                  bitmap = bitmap.asImageBitmap(),
                  contentDescription = "Logo",
                  contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                  modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                )
              } else {
                Box(
                  modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = if (currentLogoPath.isNotEmpty()) "Logo Terpasang" else "Belum Ada Logo",
                  style = MaterialTheme.typography.labelLarge,
                  fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Button(
                    onClick = {
                      photoPickerLauncher.launch(
                        androidx.activity.result.PickVisualMediaRequest(
                          ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                      )
                    },
                    modifier = Modifier.height(34.dp)
                  ) {
                    Text(if (currentLogoPath.isEmpty()) "+ Tambah Logo" else "Ganti", fontSize = 11.sp)
                  }
                  if (currentLogoPath.isNotEmpty()) {
                    OutlinedButton(
                      onClick = { currentLogoPath = "" },
                      modifier = Modifier.height(34.dp)
                    ) {
                      Text("Hapus", fontSize = 11.sp)
                    }
                  }
                }
              }
            }
          }
        }

        // 2. Data Usaha
        item {
          OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nama Toko / Usaha") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
          )
        }

        item {
          OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("No. WhatsApp Toko") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
          )
        }

        item {
          OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Alamat Toko") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
          )
        }

        item {
          OutlinedTextField(
            value = header,
            onValueChange = { header = it },
            label = { Text("Header Struk (Atas)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
          )
        }

        item {
          OutlinedTextField(
            value = footer,
            onValueChange = { footer = it },
            label = { Text("Footer Struk (Bawah)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
          )
        }

        // 3. Section Metode Pembayaran Transfer Bank
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
          ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Rekening Transfer Bank Toko", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
              }
              Text("Detail ini akan ditampilkan kepada pelanggan saat memilih metode Transfer Bank.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

              OutlinedTextField(
                value = bankName,
                onValueChange = { bankName = it },
                label = { Text("Nama Bank (misal: BCA, BRI, Mandiri)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
              )

              OutlinedTextField(
                value = bankAccountNumber,
                onValueChange = { bankAccountNumber = it },
                label = { Text("Nomor Rekening") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
              )

              OutlinedTextField(
                value = bankAccountHolder,
                onValueChange = { bankAccountHolder = it },
                label = { Text("Atas Nama Pemilik Rekening") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
              )
            }
          }
        }

        // 4. Section QRIS Toko
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.QrCode, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Barcode QRIS Toko", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
              }
              Text(
                text = "Unggah barcode QRIS toko Anda agar pelanggan dapat scan langsung dari HP mereka.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(10.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
              ) {
                val qrisFile = remember(currentQrisPath) {
                  if (currentQrisPath.isNotEmpty()) java.io.File(currentQrisPath) else null
                }
                val qrisBitmap = remember(qrisFile) {
                  if (qrisFile != null && qrisFile.exists()) {
                    android.graphics.BitmapFactory.decodeFile(qrisFile.absolutePath)
                  } else null
                }

                if (qrisBitmap != null) {
                  androidx.compose.foundation.Image(
                    bitmap = qrisBitmap.asImageBitmap(),
                    contentDescription = "QRIS Barcode",
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                    modifier = Modifier
                      .size(60.dp)
                      .clip(RoundedCornerShape(8.dp))
                      .background(Color.White)
                      .padding(4.dp)
                  )
                } else {
                  Box(
                    modifier = Modifier
                      .size(60.dp)
                      .clip(RoundedCornerShape(8.dp))
                      .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(imageVector = Icons.Default.QrCode, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                  }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = if (currentQrisPath.isNotEmpty()) "QRIS Aktif" else "Belum Ada QRIS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                      onClick = {
                        qrisPickerLauncher.launch(
                          androidx.activity.result.PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                          )
                        )
                      },
                      modifier = Modifier.height(34.dp)
                    ) {
                      Text(if (currentQrisPath.isEmpty()) "+ Upload QRIS" else "Ganti QRIS", fontSize = 11.sp)
                    }
                    if (currentQrisPath.isNotEmpty()) {
                      OutlinedButton(
                        onClick = { currentQrisPath = "" },
                        modifier = Modifier.height(34.dp)
                      ) {
                        Text("Hapus", fontSize = 11.sp)
                      }
                    }
                  }
                }
              }
            }
          }
        }

        // 5. Section Jumlah Salinan Cetak (SANGAT RAPIH, TIDAK TERHIMPIT)
        item {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 4.dp)
          ) {
            Text(
              text = "Jumlah Salinan Cetak Struk",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Pilih rangkap struk saat mencetak transaksi kasbon:",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Two wide, spacious responsive cards side by side
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              // 1 Lembar Card
              Card(
                modifier = Modifier
                  .weight(1f)
                  .clickable { copies = 1 }
                  .testTag("copy_option_1"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                  containerColor = if (copies == 1) MaterialTheme.colorScheme.primaryContainer
                  else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = if (copies == 1) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 12.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    if (copies == 1) {
                      Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                      )
                      Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                      text = "1 Lembar",
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      color = if (copies == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                  }
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = "Hemat Kertas (Standar)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              // 2 Lembar Card
              Card(
                modifier = Modifier
                  .weight(1f)
                  .clickable { copies = 2 }
                  .testTag("copy_option_2"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                  containerColor = if (copies == 2) MaterialTheme.colorScheme.primaryContainer
                  else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = if (copies == 2) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 12.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    if (copies == 2) {
                      Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                      )
                      Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                      text = "2 Lembar",
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      color = if (copies == 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                  }
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = "Toko + Pelanggan",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }
        }

        // 6. Section Ukuran Kertas Struk (58 mm vs 80 mm)
        item {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 4.dp)
          ) {
            Text(
              text = "Ukuran Kertas Struk Thermal",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Pilih lebar kertas sesuai jenis printer Bluetooth Anda:",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              // 58 mm
              val is58 = charsPerLine <= 32
              Card(
                modifier = Modifier
                  .weight(1f)
                  .clickable { charsPerLine = 32 },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                  containerColor = if (is58) MaterialTheme.colorScheme.primaryContainer
                  else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = if (is58) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 12.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    if (is58) {
                      Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                      )
                      Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                      text = "58 mm",
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      color = if (is58) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                  }
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = "32 Kolom (Standar)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              // 80 mm
              val is80 = charsPerLine >= 48
              Card(
                modifier = Modifier
                  .weight(1f)
                  .clickable { charsPerLine = 48 },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                  containerColor = if (is80) MaterialTheme.colorScheme.primaryContainer
                  else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = if (is80) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 12.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    if (is80) {
                      Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                      )
                      Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                      text = "80 mm",
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      color = if (is80) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                  }
                  Spacer(modifier = Modifier.height(2.dp))
                  Text(
                    text = "48 Kolom (Lebar)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSave(
            name,
            address,
            phone,
            header,
            footer,
            currentProfile.autoPrint,
            copies,
            charsPerLine,
            currentLogoPath,
            currentQrisPath,
            bankName,
            bankAccountNumber,
            bankAccountHolder
          )
        },
        shape = RoundedCornerShape(10.dp)
      ) {
        Text("Simpan Profil")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) { Text("Batal") }
    }
  )
}
