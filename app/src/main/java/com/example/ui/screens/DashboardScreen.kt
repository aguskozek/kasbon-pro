package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import java.io.File
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.printer.ReceiptPrintService
import com.example.ui.components.MetricStatCard
import com.example.ui.components.QuickActionButton
import com.example.ui.components.RupiahHelper
import com.example.ui.components.StatusBadge
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.StatusColors
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.KasbonViewModel
import com.example.ui.viewmodel.PrinterViewModel

@Composable
fun DashboardScreen(
  viewModel: KasbonViewModel,
  printerViewModel: PrinterViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val dashboardState by viewModel.dashboardState.collectAsState()
  val storeProfile by viewModel.storeProfile.collectAsState()
  val customers by viewModel.allCustomers.collectAsState()
  val colors = StatusColors

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("dashboard_screen"),
    contentPadding = PaddingValues(bottom = 90.dp)
  ) {
    // Top Hero Branding Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = storeProfile.storeName.ifEmpty { "KHM KASBON PRO" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Text(
                text = "Manajemen Kasbon & Cetak Struk 58mm",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.85f)
              )
            }
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
                  .size(46.dp)
                  .clip(RoundedCornerShape(12.dp))
              )
            } else {
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .clip(CircleShape)
                  .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.AccountBalanceWallet,
                  contentDescription = "Logo",
                  tint = ElectricBlue,
                  modifier = Modifier.size(24.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = "Total Piutang Belum Lunas",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.85f)
          )
          Text(
            text = RupiahHelper.format(dashboardState.totalRemainingDebt),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
          )
        }
      }
    }

    // Quick Action Buttons (5 Utama)
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
      ) {
        Text(
          text = "Aksi Cepat",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Row 1: 3 Action buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          QuickActionButton(
            label = "+ Kasbon",
            icon = Icons.Default.Add,
            backgroundColor = MaterialTheme.colorScheme.primary,
            onClick = { viewModel.navigateTo(AppScreen.NewKasbon()) },
            modifier = Modifier.weight(1f),
            testTag = "btn_add_kasbon"
          )
          QuickActionButton(
            label = "+ Pembayaran",
            icon = Icons.Default.Paid,
            backgroundColor = colors.success,
            onClick = { viewModel.navigateTo(AppScreen.NewPayment()) },
            modifier = Modifier.weight(1f),
            testTag = "btn_add_payment"
          )
          QuickActionButton(
            label = "+ Pelanggan",
            icon = Icons.Default.People,
            backgroundColor = Color(0xFF6366F1), // Indigo
            onClick = { viewModel.navigateTo(AppScreen.Customers) },
            modifier = Modifier.weight(1f),
            testTag = "btn_add_customer"
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Row 2: Kirim Tagihan WA & Cetak Struk
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          QuickActionButton(
            label = "Kirim Tagihan WA",
            icon = Icons.Default.Send,
            backgroundColor = Color(0xFF10B981), // Emerald WA
            onClick = {
              val firstDue = dashboardState.dueKasbons.firstOrNull()
              if (firstDue != null) {
                val cust = customers.find { it.id == firstDue.customerId }
                if (cust != null) {
                  viewModel.previewCustomerSummary(cust.id)
                } else {
                  viewModel.navigateTo(AppScreen.Customers)
                }
              } else {
                viewModel.navigateTo(AppScreen.Customers)
              }
            },
            modifier = Modifier.weight(1f),
            testTag = "btn_send_wa"
          )
          QuickActionButton(
            label = "Cetak Struk",
            icon = Icons.Default.Print,
            backgroundColor = Color(0xFF0284C7), // Light Blue
            onClick = {
              printerViewModel.testPrint(storeProfile)
            },
            modifier = Modifier.weight(1f),
            testTag = "btn_print_receipt"
          )
        }
      }
    }

    item { Spacer(modifier = Modifier.height(20.dp)) }

    // Financial KPI Cards
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
      ) {
        Text(
          text = "Ringkasan Keuangan Hari Ini",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          MetricStatCard(
            title = "Penerimaan Hari Ini",
            value = RupiahHelper.format(dashboardState.todayTotalPayments),
            subtitle = "Cicilan & Pelunasan",
            icon = Icons.Default.CheckCircle,
            accentColor = colors.success,
            modifier = Modifier.weight(1f),
            onClick = { viewModel.navigateTo(AppScreen.PaymentsList) }
          )
          MetricStatCard(
            title = "Kasbon Hari Ini",
            value = RupiahHelper.format(dashboardState.todayTotalKasbon),
            subtitle = "Utang Baru Masuk",
            icon = Icons.Default.Receipt,
            accentColor = colors.warning,
            modifier = Modifier.weight(1f),
            onClick = { viewModel.navigateTo(AppScreen.KasbonList) }
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          MetricStatCard(
            title = "Pelanggan Terdaftar",
            value = "${dashboardState.customerCount} Orang",
            subtitle = "Aktif berbelanja",
            icon = Icons.Default.People,
            accentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
            onClick = { viewModel.navigateTo(AppScreen.Customers) }
          )
          MetricStatCard(
            title = "Jatuh Tempo",
            value = "${dashboardState.dueKasbons.size} Tagihan",
            subtitle = if (dashboardState.dueKasbons.isNotEmpty()) "Perlu Ditagih" else "Semua Aman",
            icon = Icons.Default.DateRange,
            accentColor = if (dashboardState.dueKasbons.isNotEmpty()) colors.danger else colors.success,
            modifier = Modifier.weight(1f),
            onClick = { viewModel.navigateTo(AppScreen.KasbonList) }
          )
        }
      }
    }

    item { Spacer(modifier = Modifier.height(20.dp)) }

    // Tagihan Jatuh Tempo Section
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Tagihan Jatuh Tempo",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )
          if (dashboardState.dueKasbons.isNotEmpty()) {
            Text(
              text = "Lihat Semua",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.clickable { viewModel.navigateTo(AppScreen.KasbonList) }
            )
          }
        }
        Spacer(modifier = Modifier.height(8.dp))
      }
    }

    if (dashboardState.dueKasbons.isEmpty()) {
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = "Aman",
              tint = colors.success,
              modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Tidak Ada Tagihan Jatuh Tempo",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = "Semua kasbon pelanggan saat ini masih dalam batas aman.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    } else {
      items(dashboardState.dueKasbons.take(3)) { kasbon ->
        val customer = customers.find { it.id == kasbon.customerId }
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable {
              if (customer != null) viewModel.navigateTo(AppScreen.CustomerDetail(customer.id))
            },
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = customer?.name ?: "Pelanggan #${kasbon.customerId}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = kasbon.note.ifEmpty { "Kasbon ${kasbon.invoiceNumber}" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = "Jatuh Tempo: ${RupiahHelper.formatDate(kasbon.dueDate)}",
                style = MaterialTheme.typography.labelSmall,
                color = colors.danger,
                fontWeight = FontWeight.SemiBold
              )
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = RupiahHelper.format(kasbon.remainingAmount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.danger
              )
              Spacer(modifier = Modifier.height(4.dp))
              Row(verticalAlignment = Alignment.CenterVertically) {
                StatusBadge(status = kasbon.status)
                Spacer(modifier = Modifier.width(6.dp))
                if (customer != null && customer.phone.isNotEmpty()) {
                  IconButton(
                    onClick = {
                      val msg = "Halo ${customer.name}, pengingat kasbon di ${storeProfile.storeName} sebesar ${RupiahHelper.format(kasbon.remainingAmount)} telah jatuh tempo pada ${RupiahHelper.formatDate(kasbon.dueDate)}. Mohon konfirmasi pembayarannya. Terima kasih."
                      ReceiptPrintService.shareViaWhatsApp(context, customer.phone, msg)
                    },
                    modifier = Modifier.size(32.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Send,
                      contentDescription = "WA",
                      tint = Color(0xFF10B981),
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    item { Spacer(modifier = Modifier.height(20.dp)) }

    // Recent Payments
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Pembayaran Terakhir",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )
          Text(
            text = "Riwayat",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { viewModel.navigateTo(AppScreen.PaymentsList) }
          )
        }
        Spacer(modifier = Modifier.height(8.dp))
      }
    }

    if (dashboardState.recentPayments.isEmpty()) {
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Text(
            text = "Belum ada pembayaran yang dicatat.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
          )
        }
      }
    } else {
      items(dashboardState.recentPayments.take(3)) { payment ->
        val customer = customers.find { it.id == payment.customerId }
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(
                text = customer?.name ?: "Pelanggan #${payment.customerId}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "${payment.receiptNumber} • ${payment.paymentMethod}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = RupiahHelper.formatDate(payment.paymentDate),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Text(
              text = "+ " + RupiahHelper.format(payment.amountPaid),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = colors.success
            )
          }
        }
      }
    }
  }
}
