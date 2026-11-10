package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.KasbonEntity
import com.example.ui.components.AppSearchBar
import com.example.ui.components.KasbonTopBar
import com.example.ui.components.RupiahHelper
import com.example.ui.components.StatusBadge
import com.example.ui.theme.StatusColors
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.KasbonViewModel

@Composable
fun KasbonListScreen(
  viewModel: KasbonViewModel,
  modifier: Modifier = Modifier
) {
  val kasbons by viewModel.allKasbons.collectAsState()
  val customers by viewModel.allCustomers.collectAsState()
  var searchQuery by remember { mutableStateOf("") }
  var selectedFilter by remember { mutableIntStateOf(0) } // 0: Semua, 1: Belum Lunas, 2: Jatuh Tempo, 3: Lunas

  val now = System.currentTimeMillis()
  val filteredKasbons = remember(kasbons, customers, searchQuery, selectedFilter) {
    kasbons.filter { kasbon ->
      val customer = customers.find { it.id == kasbon.customerId }
      val matchQuery = searchQuery.isBlank() ||
          kasbon.note.contains(searchQuery, ignoreCase = true) ||
          kasbon.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
          (customer?.name?.contains(searchQuery, ignoreCase = true) == true)

      val matchFilter = when (selectedFilter) {
        1 -> kasbon.status != KasbonEntity.STATUS_PAID
        2 -> kasbon.status != KasbonEntity.STATUS_PAID && kasbon.dueDate <= now
        3 -> kasbon.status == KasbonEntity.STATUS_PAID
        else -> true
      }
      matchQuery && matchFilter
    }
  }

  Scaffold(
    topBar = {
      KasbonTopBar(
        title = "Daftar Kasbon",
        subtitle = "${kasbons.size} Transaksi Kasbon"
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = { viewModel.navigateTo(AppScreen.NewKasbon()) },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White,
        modifier = Modifier
          .padding(bottom = 70.dp)
          .testTag("fab_add_kasbon")
      ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Kasbon")
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
        placeholder = "Cari nota, rincian, atau nama...",
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
      )

      // Filter Chips
      LazyRow(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val filters = listOf("Semua", "Belum Lunas", "Jatuh Tempo", "Lunas")
        items(filters.indices.toList()) { index ->
          FilterChip(
            selected = selectedFilter == index,
            onClick = { selectedFilter = index },
            label = { Text(filters[index]) }
          )
        }
      }

      if (filteredKasbons.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.Receipt,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.size(60.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = "Tidak ada catatan kasbon",
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
          items(filteredKasbons, key = { it.id }) { kasbon ->
            val customer = customers.find { it.id == kasbon.customerId }
            KasbonCardItem(
              kasbon = kasbon,
              customerName = customer?.name ?: "Pelanggan #${kasbon.customerId}",
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
fun KasbonCardItem(
  kasbon: KasbonEntity,
  customerName: String,
  onClick: () -> Unit
) {
  val colors = StatusColors

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("kasbon_card_${kasbon.id}"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = customerName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = kasbon.invoiceNumber,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        StatusBadge(status = kasbon.status)
      }

      Spacer(modifier = Modifier.height(8.dp))

      if (kasbon.note.isNotEmpty()) {
        Text(
          text = kasbon.note,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
      ) {
        Column {
          Text(
            text = "Tgl: ${RupiahHelper.formatDate(kasbon.date)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "Tempo: ${RupiahHelper.formatDate(kasbon.dueDate)}",
            style = MaterialTheme.typography.labelSmall,
            color = if (kasbon.dueDate <= System.currentTimeMillis() && kasbon.status != KasbonEntity.STATUS_PAID) colors.danger else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
          )
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "Nominal: ${RupiahHelper.format(kasbon.amount)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = RupiahHelper.format(kasbon.remainingAmount),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = if (kasbon.remainingAmount > 0) colors.danger else colors.success
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewKasbonScreen(
  preselectedCustomerId: Long? = null,
  viewModel: KasbonViewModel,
  modifier: Modifier = Modifier
) {
  val customers by viewModel.allCustomers.collectAsState()
  var selectedCustomer by remember {
    mutableStateOf(customers.find { it.id == preselectedCustomerId } ?: customers.firstOrNull())
  }
  var dropdownExpanded by remember { mutableStateOf(false) }

  var rawAmountText by remember { mutableStateOf("") }
  var noteText by remember { mutableStateOf("") }
  var daysOption by remember { mutableIntStateOf(7) } // default 7 days

  val calculatedDueDate = remember(daysOption) {
    System.currentTimeMillis() + (daysOption.toLong() * 24 * 3600 * 1000)
  }

  BackHandler {
    viewModel.navigateBack()
  }

  Scaffold(
    topBar = {
      KasbonTopBar(
        title = "Catat Kasbon Baru",
        subtitle = "Pencatatan Cepat & Mudah",
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
        .padding(16.dp)
        .background(MaterialTheme.colorScheme.background),
      verticalArrangement = Arrangement.spacedBy(14.dp)
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

      // 2. Nominal Kasbon (Format Rupiah Otomatis saat mengetik)
      Text(
        text = "Nominal Kasbon *",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold
      )

      val parsedAmount = RupiahHelper.parseRaw(rawAmountText)
      OutlinedTextField(
        value = if (rawAmountText.isEmpty()) "" else RupiahHelper.format(parsedAmount),
        onValueChange = { input ->
          val digits = input.filter { it.isDigit() }
          rawAmountText = digits
        },
        placeholder = { Text("Contoh: 150000") },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_kasbon_amount")
      )

      // Quick Amount Chips
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        listOf(50000L, 100000L, 250000L, 50000L).distinct().forEach { amt ->
          FilterChip(
            selected = parsedAmount == amt,
            onClick = { rawAmountText = amt.toString() },
            label = { Text(RupiahHelper.format(amt), fontSize = 12.sp) }
          )
        }
      }

      // 3. Keterangan / Barang
      Text(
        text = "Rincian Barang / Jasa",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold
      )
      OutlinedTextField(
        value = noteText,
        onValueChange = { noteText = it },
        placeholder = { Text("Contoh: Ganti oli mesin, sparepart, dll.") },
        maxLines = 3,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("input_kasbon_note")
      )

      // 4. Pilihan Jatuh Tempo
      Text(
        text = "Jatuh Tempo: ${RupiahHelper.formatDate(calculatedDueDate)}",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf(3 to "3 Hari", 7 to "7 Hari", 14 to "14 Hari", 30 to "1 Bulan").forEach { (days, label) ->
          FilterChip(
            selected = daysOption == days,
            onClick = { daysOption = days },
            label = { Text(label) }
          )
        }
      }

      Spacer(modifier = Modifier.weight(1f))

      // Tombol Simpan & Cetak/WA
      Button(
        onClick = {
          if (selectedCustomer != null && parsedAmount > 0) {
            viewModel.createKasbon(
              customerId = selectedCustomer!!.id,
              amount = parsedAmount,
              note = noteText.ifEmpty { "Kasbon Barang / Jasa" },
              dueDate = calculatedDueDate
            )
          }
        },
        enabled = selectedCustomer != null && parsedAmount > 0,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("btn_save_kasbon")
      ) {
        Text(
          text = "Simpan & Tampilkan Struk",
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp
        )
      }
    }
  }
}
