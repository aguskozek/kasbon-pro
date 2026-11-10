package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import com.example.data.entity.CustomerEntity
import com.example.data.entity.PaymentEntity
import com.example.ui.components.AppSearchBar
import com.example.ui.components.KasbonTopBar
import com.example.ui.components.RupiahHelper
import com.example.ui.theme.StatusColors
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.KasbonViewModel

@Composable
fun PaymentsListScreen(
  viewModel: KasbonViewModel,
  modifier: Modifier = Modifier
) {
  val payments by viewModel.allPayments.collectAsState()
  val customers by viewModel.allCustomers.collectAsState()
  var searchQuery by remember { mutableStateOf("") }
  val colors = StatusColors

  val filteredPayments = remember(payments, customers, searchQuery) {
    if (searchQuery.isBlank()) payments
    else payments.filter { payment ->
      val customer = customers.find { it.id == payment.customerId }
      payment.receiptNumber.contains(searchQuery, ignoreCase = true) ||
          payment.notes.contains(searchQuery, ignoreCase = true) ||
          (customer?.name?.contains(searchQuery, ignoreCase = true) == true)
    }
  }

  Scaffold(
    topBar = {
      KasbonTopBar(
        title = "Riwayat Pembayaran",
        subtitle = "${payments.size} Transaksi Pembayaran"
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = { viewModel.navigateTo(AppScreen.NewPayment()) },
        containerColor = colors.success,
        contentColor = Color.White,
        modifier = Modifier
          .padding(bottom = 70.dp)
          .testTag("fab_add_payment")
      ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "Terima Pembayaran")
      }
    },
    modifier = modifier.fillMaxSize()
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .background(MaterialTheme.colorScheme.background)
    ) {
      AppSearchBar(
        query = searchQuery,
        onQueryChange = { searchQuery = it },
        placeholder = "Cari nomor struk atau nama pelanggan...",
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
      )

      if (filteredPayments.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.Paid,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.size(60.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "Belum ada riwayat pembayaran",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(filteredPayments, key = { it.id }) { payment ->
            val customer = customers.find { it.id == payment.customerId }
            PaymentCardItem(
              payment = payment,
              customer = customer,
              onClick = {
                if (customer != null) {
                  viewModel.navigateTo(AppScreen.CustomerDetail(customer.id))
                }
              }
            )
          }
          item { Spacer(modifier = Modifier.height(80.dp)) }
        }
      }
    }
  }
}

@Composable
fun PaymentCardItem(
  payment: PaymentEntity,
  customer: CustomerEntity?,
  onClick: () -> Unit
) {
  val colors = StatusColors

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("payment_card_${payment.id}"),
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
          text = customer?.name ?: "Pelanggan #${payment.customerId}",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "${payment.receiptNumber} • ${payment.paymentMethod}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = "Tanggal: ${RupiahHelper.formatDate(payment.paymentDate)}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (payment.notes.isNotEmpty()) {
          Text(
            text = "Ket: ${payment.notes}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = "+ " + RupiahHelper.format(payment.amountPaid),
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.ExtraBold,
          color = colors.success
        )
        Text(
          text = "Sisa Utang: ${RupiahHelper.format(payment.remainingDebtAfter)}",
          style = MaterialTheme.typography.labelSmall,
          color = if (payment.remainingDebtAfter > 0) colors.danger else colors.success,
          fontWeight = FontWeight.SemiBold
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPaymentScreen(
  preselectedCustomerId: Long? = null,
  preselectedKasbonId: Long? = null,
  viewModel: KasbonViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val customers by viewModel.allCustomers.collectAsState()
  val kasbons by viewModel.allKasbons.collectAsState()
  val storeProfile by viewModel.storeProfile.collectAsState()
  val colors = StatusColors

  var selectedCustomer by remember {
    mutableStateOf(customers.find { it.id == preselectedCustomerId } ?: customers.firstOrNull())
  }
  var dropdownExpanded by remember { mutableStateOf(false) }

  val activeCustomerKasbons = remember(kasbons, selectedCustomer) {
    if (selectedCustomer == null) emptyList()
    else kasbons.filter { it.customerId == selectedCustomer!!.id && it.status != "PAID" }
  }

  val totalCustomerDebt = remember(activeCustomerKasbons) {
    activeCustomerKasbons.sumOf { it.remainingAmount }
  }

  var rawAmountText by remember { mutableStateOf("") }
  var paymentMethod by remember { mutableStateOf("Tunai") }
  var notesText by remember { mutableStateOf("") }

  // Tunai (Cash) change calculator states
  var cashReceivedRaw by remember { mutableStateOf("") }

  // QRIS states
  var showZoomQrisDialog by remember { mutableStateOf(false) }

  // Bank Transfer states
  var selectedBank by remember(storeProfile.bankName) { mutableStateOf(storeProfile.bankName) }

  val parsedAmount = RupiahHelper.parseRaw(rawAmountText)
  val cashReceived = RupiahHelper.parseRaw(cashReceivedRaw)
  val cashChange = if (cashReceived >= parsedAmount && parsedAmount > 0) cashReceived - parsedAmount else 0L

  val qrisPickerLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.PickVisualMedia()
  ) { uri ->
    if (uri != null) {
      try {
        val destFile = File(context.filesDir, "store_qris_${System.currentTimeMillis()}.png")
        context.contentResolver.openInputStream(uri)?.use { input ->
          destFile.outputStream().use { output ->
            input.copyTo(output)
          }
        }
        viewModel.updateStoreProfile(
          name = storeProfile.storeName,
          address = storeProfile.storeAddress,
          phone = storeProfile.storePhone,
          header = storeProfile.receiptHeader,
          footer = storeProfile.receiptFooter,
          autoPrint = storeProfile.autoPrint,
          copies = storeProfile.printCopies,
          charsPerLine = storeProfile.charsPerLine,
          logoPath = storeProfile.logoPath,
          qrisImagePath = destFile.absolutePath,
          bankName = storeProfile.bankName,
          bankAccountNumber = storeProfile.bankAccountNumber,
          bankAccountHolder = storeProfile.bankAccountHolder
        )
        Toast.makeText(context, "Barcode QRIS berhasil diperbarui!", Toast.LENGTH_SHORT).show()
      } catch (e: Exception) {
        Toast.makeText(context, "Gagal memuat QRIS: ${e.message}", Toast.LENGTH_SHORT).show()
      }
    }
  }

  BackHandler {
    viewModel.navigateBack()
  }

  Scaffold(
    topBar = {
      KasbonTopBar(
        title = "Terima Pembayaran",
        subtitle = "Cicilan atau Pelunasan Penuh",
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
        .verticalScroll(rememberScrollState())
        .padding(16.dp)
        .background(MaterialTheme.colorScheme.background),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Pilih Pelanggan
      Text(
        text = "Pilih Pelanggan *",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold
      )

      ExposedDropdownMenuBox(
        expanded = dropdownExpanded,
        onExpandedChange = { dropdownExpanded = it }
      ) {
        OutlinedTextField(
          value = selectedCustomer?.name ?: "Pilih Pelanggan",
          onValueChange = {},
          readOnly = true,
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
          modifier = Modifier
            .fillMaxWidth()
            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
          shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
          expanded = dropdownExpanded,
          onDismissRequest = { dropdownExpanded = false }
        ) {
          customers.forEach { customer ->
            DropdownMenuItem(
              text = { Text("${customer.name} (${customer.phone})") },
              onClick = {
                selectedCustomer = customer
                dropdownExpanded = false
              }
            )
          }
        }
      }

      // Debt Info Card
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (totalCustomerDebt > 0) colors.dangerContainer.copy(alpha = 0.5f) else colors.successContainer.copy(alpha = 0.5f)
        )
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Total Sisa Utang Saat Ini",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = if (totalCustomerDebt > 0) "${activeCustomerKasbons.size} Kasbon Aktif" else "Semua Kasbon Sudah Lunas",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Text(
            text = RupiahHelper.format(totalCustomerDebt),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = if (totalCustomerDebt > 0) colors.danger else colors.success
          )
        }
      }

      // 2. Nominal Pembayaran
      Text(
        text = "Nominal Pembayaran *",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold
      )

      OutlinedTextField(
        value = if (rawAmountText.isEmpty()) "" else RupiahHelper.format(parsedAmount),
        onValueChange = { input ->
          val digits = input.filter { it.isDigit() }
          rawAmountText = digits
        },
        placeholder = { Text("Contoh: 100000") },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_payment_amount")
      )

      // Quick Pay Helpers: Bayar Lunas / Setengah
      if (totalCustomerDebt > 0) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilterChip(
            selected = parsedAmount == totalCustomerDebt,
            onClick = {
              rawAmountText = totalCustomerDebt.toString()
              if (paymentMethod == "Tunai" && cashReceivedRaw.isEmpty()) {
                cashReceivedRaw = totalCustomerDebt.toString()
              }
            },
            label = { Text("Lunas (${RupiahHelper.format(totalCustomerDebt)})") }
          )
          if (totalCustomerDebt >= 20000) {
            val half = totalCustomerDebt / 2
            FilterChip(
              selected = parsedAmount == half,
              onClick = {
                rawAmountText = half.toString()
                if (paymentMethod == "Tunai" && cashReceivedRaw.isEmpty()) {
                  cashReceivedRaw = half.toString()
                }
              },
              label = { Text("50% (${RupiahHelper.format(half)})") }
            )
          }
        }
      }

      // 3. Pilihan Metode Pembayaran (Sesuai Referensi Pengguna)
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Pilihan Metode Pembayaran *",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Pilih salah satu metode pembayaran yang digunakan pelanggan:",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))

        // 3 Elegant Selection Cards
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf(
            Triple("Tunai", Icons.Default.Payments, "Cash"),
            Triple("QRIS", Icons.Default.QrCode, "Scan QR"),
            Triple("Transfer Bank", Icons.Default.AccountBalance, "Rekening")
          ).forEach { (method, icon, sub) ->
            val isSelected = paymentMethod == method
            Card(
              modifier = Modifier
                .weight(1f)
                .clickable { paymentMethod = method }
                .testTag("method_$method"),
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
              ),
              border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
            ) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 10.dp, horizontal = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Icon(
                  imageVector = icon,
                  contentDescription = method,
                  tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = method,
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = sub,
                  style = MaterialTheme.typography.labelSmall,
                  fontSize = 10.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }

      // ==============================================================
      // DETAIL PANEL METODE PEMBAYARAN SESUAI GAMBAR REFERENSI
      // ==============================================================

      when (paymentMethod) {
        "Tunai" -> {
          // 1. TUNAI (CASH): Hitung Uang Tunai Diterima & Kalkulator Kembalian
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
          ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.success.copy(alpha = 0.15f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(imageVector = Icons.Default.Payments, contentDescription = null, tint = colors.success, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = "Kalkulator Pembayaran Tunai",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "Catat nominal uang diterima, kembalian akan otomatis dihitung",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              OutlinedTextField(
                value = if (cashReceivedRaw.isEmpty()) "" else RupiahHelper.format(cashReceived),
                onValueChange = { input ->
                  val digits = input.filter { it.isDigit() }
                  cashReceivedRaw = digits
                },
                label = { Text("Uang Tunai Diterima dari Pelanggan") },
                placeholder = { Text("Contoh: 100000") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("input_cash_received")
              )

              // Preset Uang Cepat (Uang Pas, 50rb, 100rb, 200rb, 500rb)
              Text("Tombol Cepat Uang Diterima:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                if (parsedAmount > 0) {
                  FilterChip(
                    selected = cashReceived == parsedAmount,
                    onClick = { cashReceivedRaw = parsedAmount.toString() },
                    label = { Text("Uang Pas") }
                  )
                }
                listOf(50000L, 100000L, 200000L, 500000L).forEach { preset ->
                  if (preset >= parsedAmount || parsedAmount == 0L) {
                    FilterChip(
                      selected = cashReceived == preset,
                      onClick = { cashReceivedRaw = preset.toString() },
                      label = { Text(RupiahHelper.format(preset)) }
                    )
                  }
                }
              }

              // Kalkulator Kembalian Otomatis
              if (parsedAmount > 0 && cashReceived >= parsedAmount) {
                Card(
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(10.dp),
                  colors = CardDefaults.cardColors(containerColor = colors.successContainer.copy(alpha = 0.6f)),
                  border = BorderStroke(1.5.dp, colors.success)
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = colors.success,
                        modifier = Modifier.size(24.dp)
                      )
                      Spacer(modifier = Modifier.width(8.dp))
                      Column {
                        Text(
                          text = "Uang Kembalian:",
                          style = MaterialTheme.typography.labelMedium,
                          fontWeight = FontWeight.Bold,
                          color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                          text = if (cashChange == 0L) "Uang pas (tanpa kembalian)" else "Kembalikan ke pelanggan",
                          style = MaterialTheme.typography.labelSmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                      }
                    }

                    Text(
                      text = RupiahHelper.format(cashChange),
                      style = MaterialTheme.typography.titleLarge,
                      fontWeight = FontWeight.ExtraBold,
                      color = colors.success
                    )
                  }
                }
              } else if (cashReceived > 0 && cashReceived < parsedAmount) {
                Card(
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(10.dp),
                  colors = CardDefaults.cardColors(containerColor = colors.dangerContainer.copy(alpha = 0.4f)),
                  border = BorderStroke(1.dp, colors.danger)
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = "Uang Diterima Masih Kurang:",
                      style = MaterialTheme.typography.labelMedium,
                      color = colors.danger,
                      fontWeight = FontWeight.Bold
                    )
                    Text(
                      text = "- " + RupiahHelper.format(parsedAmount - cashReceived),
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      color = colors.danger
                    )
                  }
                }
              }
            }
          }
        }

        "QRIS" -> {
          // 2. QRIS: Tampilkan / Upload QRIS Toko agar pelanggan tinggal scan dari HP
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              // QRIS Badge Header
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFDC2626)
              ) {
                Text(
                  text = "QRIS - PEMBAYARAN DIGITAL",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.ExtraBold,
                  color = Color.White,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
              }
              Spacer(modifier = Modifier.height(10.dp))

              val qrisFile = remember(storeProfile.qrisImagePath) {
                if (storeProfile.qrisImagePath.isNotEmpty()) File(storeProfile.qrisImagePath) else null
              }
              val qrisBitmap = remember(qrisFile) {
                if (qrisFile != null && qrisFile.exists()) {
                  android.graphics.BitmapFactory.decodeFile(qrisFile.absolutePath)
                } else null
              }

              if (qrisBitmap != null) {
                Image(
                  bitmap = qrisBitmap.asImageBitmap(),
                  contentDescription = "Barcode QRIS Toko",
                  modifier = Modifier
                    .size(190.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(8.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Button(
                    onClick = { showZoomQrisDialog = true },
                    modifier = Modifier.height(38.dp)
                  ) {
                    Icon(imageVector = Icons.Default.Fullscreen, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Perbesar QRIS (Layar Penuh)", fontSize = 12.sp)
                  }

                  OutlinedButton(
                    onClick = {
                      qrisPickerLauncher.launch(
                        androidx.activity.result.PickVisualMediaRequest(
                          ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                      )
                    },
                    modifier = Modifier.height(38.dp)
                  ) {
                    Text("Ganti QRIS", fontSize = 12.sp)
                  }
                }
              } else {
                Box(
                  modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                  contentAlignment = Alignment.Center
                ) {
                  Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                      imageVector = Icons.Default.QrCode,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                      text = "Belum Ada Barcode QRIS",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                  onClick = {
                    qrisPickerLauncher.launch(
                      androidx.activity.result.PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                      )
                    )
                  },
                  modifier = Modifier.height(38.dp)
                ) {
                  Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Unggah Barcode QRIS Toko", fontSize = 12.sp)
                }
              }

              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = "Minta pelanggan scan barcode QRIS di atas langsung dari HP mereka menggunakan GoPay, OVO, Dana, ShopeePay, BCA, atau M-Banking apa pun.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }
          }
        }

        "Transfer Bank" -> {
          // 3. TRANSFER BANK: Detail nomor rekening bank toko untuk transfer langsung
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
          ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Box(
                    modifier = Modifier
                      .size(32.dp)
                      .clip(RoundedCornerShape(8.dp))
                      .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Text(
                    text = "Rekening Tujuan Transfer",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                  )
                }

                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = MaterialTheme.colorScheme.primaryContainer
                ) {
                  Text(
                    text = selectedBank,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                  )
                }
              }

              // Bank selector chips
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                listOf("BCA", "BRI", "Mandiri", "BNI", "BSI").forEach { b ->
                  FilterChip(
                    selected = selectedBank == b,
                    onClick = { selectedBank = b },
                    label = { Text(b) }
                  )
                }
              }

              // Account Number Box with one-tap copy
              Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Column {
                    Text(
                      text = "Nomor Rekening ${selectedBank}:",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                      text = storeProfile.bankAccountNumber.ifEmpty { "8923456789" },
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.ExtraBold,
                      fontFamily = FontFamily.Monospace,
                      color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                      text = "A/N ${storeProfile.bankAccountHolder.ifEmpty { storeProfile.storeName }}",
                      style = MaterialTheme.typography.bodySmall,
                      fontWeight = FontWeight.Medium,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                  }

                  Button(
                    onClick = {
                      val accNum = storeProfile.bankAccountNumber.ifEmpty { "8923456789" }
                      val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                      val clip = ClipData.newPlainText("Nomor Rekening", accNum)
                      clipboard.setPrimaryClip(clip)
                      Toast.makeText(context, "Nomor rekening $accNum berhasil disalin!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.height(38.dp)
                  ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Salin Rekening", fontSize = 11.sp)
                  }
                }
              }

              Text(
                text = "Tunjukkan nomor rekening di atas kepada pelanggan atau tekan 'Salin Rekening' untuk dikirimkan melalui WhatsApp/SMS.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      // 4. Catatan Tambahan
      Text(
        text = "Catatan Pembayaran (Opsional)",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold
      )
      OutlinedTextField(
        value = notesText,
        onValueChange = { notesText = it },
        placeholder = { Text("Contoh: Cicilan ke-1, pelunasan nota, dll.") },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_payment_notes")
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Button Simpan & Cetak
      Button(
        onClick = {
          if (selectedCustomer != null && parsedAmount > 0) {
            val finalNote = when (paymentMethod) {
              "Tunai" -> {
                val cashInfo = if (cashReceived > 0) {
                  "Tunai (Diterima: ${RupiahHelper.format(cashReceived)}, Kembali: ${RupiahHelper.format(cashChange)})"
                } else "Tunai"
                if (notesText.isNotBlank()) "$notesText | $cashInfo" else cashInfo
              }
              "QRIS" -> {
                if (notesText.isNotBlank()) "$notesText | QRIS Digital" else "Pembayaran QRIS"
              }
              "Transfer Bank" -> {
                val bankInfo = "Transfer Bank $selectedBank (${storeProfile.bankAccountNumber})"
                if (notesText.isNotBlank()) "$notesText | $bankInfo" else bankInfo
              }
              else -> notesText
            }

            viewModel.processPayment(
              customerId = selectedCustomer!!.id,
              kasbonId = preselectedKasbonId,
              amountPaid = parsedAmount,
              paymentMethod = paymentMethod,
              notes = finalNote
            )
          }
        },
        enabled = selectedCustomer != null && parsedAmount > 0,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("btn_save_payment")
      ) {
        Icon(imageVector = Icons.Default.Paid, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Simpan & Tampilkan Struk Bukti",
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp
        )
      }

      Spacer(modifier = Modifier.height(40.dp))
    }
  }

  // Dialog Perbesar QRIS Fullscreen
  if (showZoomQrisDialog) {
    AlertDialog(
      onDismissRequest = { showZoomQrisDialog = false },
      title = {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Scan QRIS Toko", fontWeight = FontWeight.Bold)
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFFDC2626)
          ) {
            Text("QRIS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
          }
        }
      },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = storeProfile.storeName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          if (parsedAmount > 0) {
            Text(
              text = "Nominal: ${RupiahHelper.format(parsedAmount)}",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.SemiBold
            )
          }
          Spacer(modifier = Modifier.height(14.dp))

          val qrisFile = remember(storeProfile.qrisImagePath) {
            if (storeProfile.qrisImagePath.isNotEmpty()) File(storeProfile.qrisImagePath) else null
          }
          val qrisBitmap = remember(qrisFile) {
            if (qrisFile != null && qrisFile.exists()) {
              android.graphics.BitmapFactory.decodeFile(qrisFile.absolutePath)
            } else null
          }

          if (qrisBitmap != null) {
            Image(
              bitmap = qrisBitmap.asImageBitmap(),
              contentDescription = "QRIS Toko Full",
              modifier = Modifier
                .size(260.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(10.dp)
            )
          }
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "Minta pelanggan membuka kamera atau aplikasi e-wallet mereka lalu scan kode di atas.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
        }
      },
      confirmButton = {
        Button(
          onClick = { showZoomQrisDialog = false },
          shape = RoundedCornerShape(8.dp)
        ) {
          Text("Tutup")
        }
      }
    )
  }
}
