package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import java.io.File
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.printer.PrinterStatus
import com.example.printer.ReceiptPrintService
import com.example.ui.components.KasbonTopBar
import com.example.ui.theme.StatusColors
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.KasbonViewModel
import com.example.ui.viewmodel.PrinterViewModel
import kotlinx.coroutines.launch

@Composable
fun ReceiptPreviewScreen(
  title: String,
  receiptText: String,
  customerPhone: String,
  customerName: String,
  viewModel: KasbonViewModel,
  printerViewModel: PrinterViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val printerStatus by printerViewModel.printerStatus.collectAsState()
  val colors = StatusColors

  BackHandler {
    viewModel.navigateBack()
  }

  Scaffold(
    topBar = {
      KasbonTopBar(
        title = title,
        subtitle = customerName,
        showBackButton = true,
        onBackClick = { viewModel.navigateBack() }
      )
    },
    modifier = modifier.fillMaxSize()
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .background(MaterialTheme.colorScheme.background)
        .padding(16.dp)
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Printer Connection Banner
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
          containerColor = when (printerStatus) {
            is PrinterStatus.Connected -> colors.successContainer
            is PrinterStatus.Connecting -> colors.warningContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
          }
        )
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = when (printerStatus) {
                is PrinterStatus.Connected -> Icons.Default.CheckCircle
                is PrinterStatus.Connecting -> Icons.Default.Bluetooth
                else -> Icons.Default.Warning
              },
              contentDescription = null,
              tint = when (printerStatus) {
                is PrinterStatus.Connected -> colors.success
                is PrinterStatus.Connecting -> colors.warning
                else -> MaterialTheme.colorScheme.onSurfaceVariant
              },
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = when (val s = printerStatus) {
                is PrinterStatus.Connected -> "Terhubung: ${s.deviceName}"
                is PrinterStatus.Connecting -> "Menyambungkan..."
                is PrinterStatus.Error -> "Koneksi Printer: ${s.message}"
                else -> "Printer Bluetooth: Belum Terhubung"
              },
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.SemiBold
            )
          }

          if (printerStatus !is PrinterStatus.Connected) {
            OutlinedButton(
              onClick = { viewModel.navigateTo(AppScreen.PrinterSettings) },
              modifier = Modifier.height(34.dp)
            ) {
              Text("Atur Printer", fontSize = 11.sp)
            }
          }
        }
      }

      val storeProfile by viewModel.storeProfile.collectAsState()
      val logoFile = remember(storeProfile.logoPath) {
        if (storeProfile.logoPath.isNotEmpty()) java.io.File(storeProfile.logoPath) else null
      }
      val logoBitmap = remember(logoFile) {
        if (logoFile != null && logoFile.exists()) {
          android.graphics.BitmapFactory.decodeFile(logoFile.absolutePath)
        } else null
      }

      // Paper width selector (58mm vs 80mm)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Ukuran Kertas:",
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          val is58 = storeProfile.charsPerLine <= 32
          FilterChip(
            selected = is58,
            onClick = { viewModel.updatePaperWidth(32) },
            label = { Text("58 mm (32 Kolom)", fontSize = 11.sp) }
          )
          FilterChip(
            selected = !is58,
            onClick = { viewModel.updatePaperWidth(48) },
            label = { Text("80 mm (48 Kolom)", fontSize = 11.sp) }
          )
        }
      }

      // Realistic Thermal Paper Card
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          if (logoBitmap != null) {
            androidx.compose.foundation.Image(
              bitmap = logoBitmap.asImageBitmap(),
              contentDescription = "Logo Usaha",
              contentScale = androidx.compose.ui.layout.ContentScale.Fit,
              modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.height(10.dp))
          }

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = receiptText,
              fontFamily = FontFamily.Monospace,
              fontSize = if (storeProfile.charsPerLine > 32) 10.sp else 11.5.sp,
              lineHeight = if (storeProfile.charsPerLine > 32) 15.sp else 16.5.sp,
              color = Color(0xFF111827) // Clear thermal black
            )
          }
        }
      }

      // Action Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // WhatsApp Share Button
        Button(
          onClick = {
            ReceiptPrintService.shareViaWhatsApp(context, customerPhone, receiptText)
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .weight(1f)
            .height(52.dp)
            .testTag("btn_share_receipt_wa")
        ) {
          Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Kirim WA", fontWeight = FontWeight.Bold)
        }

        // Bluetooth Print Button
        Button(
          onClick = {
            scope.launch {
              val currentStat = printerStatus
              if (currentStat is PrinterStatus.Connected) {
                val data = receiptText.toByteArray(Charsets.US_ASCII)
                val res = printerViewModel.printerManager.printData(data)
                if (res.isSuccess) {
                  Toast.makeText(context, "Struk berhasil dicetak!", Toast.LENGTH_SHORT).show()
                } else {
                  Toast.makeText(context, "Gagal mencetak: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                }
              } else {
                Toast.makeText(context, "Hubungkan printer Bluetooth terlebih dahulu di menu Pengaturan.", Toast.LENGTH_LONG).show()
                viewModel.navigateTo(AppScreen.PrinterSettings)
              }
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier
            .weight(1f)
            .height(52.dp)
            .testTag("btn_print_thermal")
        ) {
          Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Cetak Struk", fontWeight = FontWeight.Bold)
        }
      }

      // Secondary Action: Share / Copy
      OutlinedButton(
        onClick = {
          ReceiptPrintService.shareGeneric(context, receiptText)
        },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(46.dp)
      ) {
        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Bagikan Teks / Aplikasi Lain")
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}
