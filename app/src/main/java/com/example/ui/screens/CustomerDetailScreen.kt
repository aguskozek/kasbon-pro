package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.CustomerEntity
import com.example.printer.ReceiptPrintService
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.KasbonTopBar
import com.example.ui.components.QuickActionButton
import com.example.ui.components.RupiahHelper
import com.example.ui.components.StatusBadge
import com.example.ui.theme.StatusColors
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.KasbonViewModel

@Composable
fun CustomerDetailScreen(
  customerId: Long,
  viewModel: KasbonViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val customers by viewModel.allCustomers.collectAsState()
  val kasbons by viewModel.allKasbons.collectAsState()
  val payments by viewModel.allPayments.collectAsState()
  val storeProfile by viewModel.storeProfile.collectAsState()
  val colors = StatusColors

  val customer = customers.find { it.id == customerId }
  val customerKasbons = remember(kasbons, customerId) {
    kasbons.filter { it.customerId == customerId }
  }
  val customerPayments = remember(payments, customerId) {
    payments.filter { it.customerId == customerId }
  }

  val totalKasbon = customerKasbons.sumOf { it.amount }
  val totalPaid = customerPayments.sumOf { it.amountPaid }
  val remainingDebt = customerKasbons.sumOf { it.remainingAmount }

  var selectedTab by remember { mutableIntStateOf(0) }
  var showDeleteConfirm by remember { mutableStateOf(false) }
  var showEditDialog by remember { mutableStateOf(false) }

  BackHandler {
    viewModel.navigateBack()
  }

  if (customer == null) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text("Pelanggan tidak ditemukan")
    }
    return
  }

  Scaffold(
    topBar = {
      KasbonTopBar(
        title = "Detail Pelanggan",
        subtitle = customer.name,
        showBackButton = true,
        onBackClick = { viewModel.navigateBack() },
        actions = {
          IconButton(onClick = { showEditDialog = true }) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Pelanggan")
          }
          IconButton(onClick = { showDeleteConfirm = true }) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Hapus Pelanggan",
              tint = colors.danger
            )
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
      contentPadding = PaddingValues(bottom = 90.dp)
    ) {
      // Customer Info Card
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
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
                  .size(52.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = customer.name.take(2).uppercase(),
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
              }
              Spacer(modifier = Modifier.width(14.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = customer.name,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
                if (customer.phone.isNotEmpty()) {
                  Text(
                    text = customer.phone,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
                if (customer.address.isNotEmpty()) {
                  Text(
                    text = customer.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              if (customer.phone.isNotEmpty()) {
                IconButton(
                  onClick = {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.phone}"))
                    context.startActivity(intent)
                  }
                ) {
                  Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = "Telepon",
                    tint = MaterialTheme.colorScheme.primary
                  )
                }
              }
            }

            if (customer.notes.isNotEmpty()) {
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = "Catatan: ${customer.notes}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      // Financial Summary Strip
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FinancialBadge(
            label = "Total Kasbon",
            value = RupiahHelper.format(totalKasbon),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
          )
          FinancialBadge(
            label = "Sudah Bayar",
            value = RupiahHelper.format(totalPaid),
            color = colors.success,
            modifier = Modifier.weight(1f)
          )
          FinancialBadge(
            label = "Sisa Utang",
            value = RupiahHelper.format(remainingDebt),
            color = if (remainingDebt > 0) colors.danger else colors.success,
            modifier = Modifier.weight(1f)
          )
        }
      }

      item { Spacer(modifier = Modifier.height(14.dp)) }

      // Action Buttons for this customer
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            QuickActionButton(
              label = "+ Kasbon Baru",
              icon = Icons.Default.Add,
              backgroundColor = MaterialTheme.colorScheme.primary,
              onClick = {
                viewModel.navigateTo(AppScreen.NewKasbon(preselectedCustomerId = customer.id))
              },
              modifier = Modifier.weight(1f)
            )
            QuickActionButton(
              label = "+ Bayar Utang",
              icon = Icons.Default.Paid,
              backgroundColor = colors.success,
              onClick = {
                viewModel.navigateTo(AppScreen.NewPayment(preselectedCustomerId = customer.id))
              },
              modifier = Modifier.weight(1f)
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            QuickActionButton(
              label = "Kirim Tagihan WA",
              icon = Icons.Default.Send,
              backgroundColor = Color(0xFF10B981),
              onClick = {
                val unpaid = customerKasbons.filter { it.status != "PAID" }
                val msg = buildString {
                  appendLine("*${storeProfile.receiptHeader}*")
                  appendLine("Halo ${customer.name}, berikut rincian tagihan kasbon Anda di ${storeProfile.storeName}:")
                  appendLine("---------------------------")
                  unpaid.forEachIndexed { idx, k ->
                    appendLine("${idx + 1}. ${k.note.ifEmpty { "Kasbon" }}")
                    appendLine("   Nominal : ${RupiahHelper.format(k.amount)}")
                    appendLine("   Sisa    : ${RupiahHelper.format(k.remainingAmount)}")
                    appendLine("   Tempo   : ${RupiahHelper.formatDate(k.dueDate)}")
                  }
                  appendLine("---------------------------")
                  appendLine("*TOTAL UTANG: ${RupiahHelper.format(remainingDebt)}*")
                  appendLine()
                  appendLine("Mohon konfirmasi jika ada pertanyaan. Terima kasih.")
                }
                ReceiptPrintService.shareViaWhatsApp(context, customer.phone, msg)
              },
              modifier = Modifier.weight(1f)
            )
            QuickActionButton(
              label = "Cetak Rekap",
              icon = Icons.Default.Print,
              backgroundColor = Color(0xFF0284C7),
              onClick = {
                viewModel.previewCustomerSummary(customer.id)
              },
              modifier = Modifier.weight(1f)
            )
          }
        }
      }

      item { Spacer(modifier = Modifier.height(16.dp)) }

      // Tabs: Kasbon vs Pembayaran
      item {
        TabRow(
          selectedTabIndex = selectedTab,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            text = { Text("Kasbon (${customerKasbons.size})") }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            text = { Text("Pembayaran (${customerPayments.size})") }
          )
        }
        Spacer(modifier = Modifier.height(10.dp))
      }

      if (selectedTab == 0) {
        if (customerKasbons.isEmpty()) {
          item {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
              contentAlignment = Alignment.Center
            ) {
              Text("Belum ada catatan kasbon untuk pelanggan ini.")
            }
          }
        } else {
          items(customerKasbons) { kasbon ->
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                    text = kasbon.note.ifEmpty { "Kasbon ${kasbon.invoiceNumber}" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "Tgl: ${RupiahHelper.formatDate(kasbon.date)} • Tempo: ${RupiahHelper.formatDate(kasbon.dueDate)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Text(
                    text = "Awal: ${RupiahHelper.format(kasbon.amount)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }

                Column(horizontalAlignment = Alignment.End) {
                  Text(
                    text = RupiahHelper.format(kasbon.remainingAmount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (kasbon.remainingAmount > 0) colors.danger else colors.success
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  StatusBadge(status = kasbon.status)
                }
              }
            }
          }
        }
      } else {
        if (customerPayments.isEmpty()) {
          item {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
              contentAlignment = Alignment.Center
            ) {
              Text("Belum ada riwayat pembayaran.")
            }
          }
        } else {
          items(customerPayments) { payment ->
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
              shape = RoundedCornerShape(12.dp),
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
                    text = payment.receiptNumber,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "${RupiahHelper.formatDate(payment.paymentDate)} • ${payment.paymentMethod}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  if (payment.notes.isNotEmpty()) {
                    Text(
                      text = payment.notes,
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }
                Column(horizontalAlignment = Alignment.End) {
                  Text(
                    text = "+ " + RupiahHelper.format(payment.amountPaid),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.success
                  )
                  Text(
                    text = "Sisa: ${RupiahHelper.format(payment.remainingDebtAfter)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  ConfirmationDialog(
    show = showDeleteConfirm,
    title = "Hapus Pelanggan?",
    message = "Semua data kasbon dan riwayat pembayaran pelanggan ini akan dihapus permanen.",
    isDanger = true,
    confirmButtonText = "Hapus",
    onConfirm = {
      viewModel.deleteCustomer(customer)
    },
    onDismiss = { showDeleteConfirm = false }
  )

  if (showEditDialog) {
    EditCustomerDialog(
      customer = customer,
      onDismiss = { showEditDialog = false },
      onSave = { updated ->
        viewModel.updateCustomer(updated)
        showEditDialog = false
      }
    )
  }
}

@Composable
fun FinancialBadge(
  label: String,
  value: String,
  color: Color,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(10.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = color
      )
    }
  }
}

@Composable
fun EditCustomerDialog(
  customer: CustomerEntity,
  onDismiss: () -> Unit,
  onSave: (CustomerEntity) -> Unit
) {
  var name by remember { mutableStateOf(customer.name) }
  var phone by remember { mutableStateOf(customer.phone) }
  var address by remember { mutableStateOf(customer.address) }
  var notes by remember { mutableStateOf(customer.notes) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Edit Data Pelanggan", fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Nama") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it },
          label = { Text("No. HP / WA") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = address,
          onValueChange = { address = it },
          label = { Text("Alamat") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Catatan") },
          maxLines = 2,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank()) {
            onSave(customer.copy(name = name, phone = phone, address = address, notes = notes))
          }
        }
      ) {
        Text("Simpan Perubahan")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) { Text("Batal") }
    }
  )
}
