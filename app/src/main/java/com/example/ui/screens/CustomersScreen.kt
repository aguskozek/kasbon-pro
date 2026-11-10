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
import android.app.Activity
import android.content.Intent
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.CustomerEntity
import com.example.ui.components.AppSearchBar
import com.example.ui.components.KasbonTopBar
import com.example.ui.components.RupiahHelper
import com.example.ui.theme.StatusColors
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.KasbonViewModel

@Composable
fun CustomersScreen(
  viewModel: KasbonViewModel,
  modifier: Modifier = Modifier
) {
  val customers by viewModel.allCustomers.collectAsState()
  val kasbons by viewModel.allKasbons.collectAsState()
  var searchQuery by remember { mutableStateOf("") }
  var showAddDialog by remember { mutableStateOf(false) }

  val filteredCustomers = remember(customers, searchQuery) {
    if (searchQuery.isBlank()) customers
    else customers.filter {
      it.name.contains(searchQuery, ignoreCase = true) ||
          it.phone.contains(searchQuery, ignoreCase = true) ||
          it.address.contains(searchQuery, ignoreCase = true)
    }
  }

  Scaffold(
    topBar = {
      KasbonTopBar(
        title = "Daftar Pelanggan",
        subtitle = "${customers.size} Pelanggan Terdaftar"
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showAddDialog = true },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White,
        modifier = Modifier
          .padding(bottom = 70.dp)
          .testTag("fab_add_customer")
      ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "Tambah Pelanggan")
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
        placeholder = "Cari nama atau no. HP pelanggan...",
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
      )

      if (filteredCustomers.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = if (searchQuery.isBlank()) "Belum ada pelanggan" else "Pelanggan tidak ditemukan",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "Klik tombol '+' di pojok kanan bawah untuk menambahkan.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(filteredCustomers, key = { it.id }) { customer ->
            val customerKasbons = kasbons.filter { it.customerId == customer.id }
            val totalRemainingDebt = customerKasbons.sumOf { it.remainingAmount }

            CustomerListItem(
              customer = customer,
              totalDebt = totalRemainingDebt,
              onClick = {
                viewModel.navigateTo(AppScreen.CustomerDetail(customer.id))
              }
            )
          }
          item { Spacer(modifier = Modifier.height(80.dp)) }
        }
      }
    }
  }

  if (showAddDialog) {
    AddCustomerDialog(
      onDismiss = { showAddDialog = false },
      onSave = { name, phone, address, notes ->
        viewModel.addCustomer(name, phone, address, notes)
        showAddDialog = false
      }
    )
  }
}

@Composable
fun CustomerListItem(
  customer: CustomerEntity,
  totalDebt: Long,
  onClick: () -> Unit
) {
  val colors = StatusColors

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("customer_item_${customer.id}"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
        Text(
          text = customer.name.take(2).uppercase(),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onPrimaryContainer
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = customer.name,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        if (customer.phone.isNotEmpty()) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Phone,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = customer.phone,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = if (totalDebt > 0) RupiahHelper.format(totalDebt) else "Lunas",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = if (totalDebt > 0) colors.danger else colors.success
        )
        Text(
          text = if (totalDebt > 0) "Sisa Utang" else "Bebas Kasbon",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.width(6.dp))
      Icon(
        imageVector = Icons.Default.ChevronRight,
        contentDescription = "Detail",
        tint = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
fun AddCustomerDialog(
  onDismiss: () -> Unit,
  onSave: (String, String, String, String) -> Unit
) {
  val context = LocalContext.current
  var name by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("") }
  var address by remember { mutableStateOf("") }
  var notes by remember { mutableStateOf("") }
  var isError by remember { mutableStateOf(false) }

  val contactPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
      val contactUri = result.data?.data ?: return@rememberLauncherForActivityResult
      try {
        val projection = arrayOf(
          ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
          ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        context.contentResolver.query(contactUri, projection, null, null, null)?.use { cursor ->
          if (cursor.moveToFirst()) {
            val nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val rawName = if (nameIdx >= 0) cursor.getString(nameIdx) ?: "" else ""
            var rawPhone = if (phoneIdx >= 0) cursor.getString(phoneIdx) ?: "" else ""

            // Format clean Indonesian phone number
            rawPhone = rawPhone.replace(" ", "").replace("-", "").replace("(", "").replace(")", "").trim()
            if (rawPhone.startsWith("+62")) {
              rawPhone = "0" + rawPhone.substring(3)
            } else if (rawPhone.startsWith("62")) {
              rawPhone = "0" + rawPhone.substring(2)
            }

            if (rawName.isNotBlank()) name = rawName
            if (rawPhone.isNotBlank()) phone = rawPhone
            isError = false
            Toast.makeText(context, "Kontak '$rawName' berhasil dipilih!", Toast.LENGTH_SHORT).show()
          }
        }
      } catch (e: Exception) {
        Toast.makeText(context, "Gagal memuat kontak: ${e.message}", Toast.LENGTH_SHORT).show()
      }
    }
  }

  val launchContactPicker = {
    try {
      val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
      contactPickerLauncher.launch(intent)
    } catch (e: Exception) {
      Toast.makeText(context, "Tidak dapat membuka daftar kontak: ${e.message}", Toast.LENGTH_SHORT).show()
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Tambah Pelanggan Baru",
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleLarge
      )
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Quick Action Button: Ambil dari Kontak HP
        Button(
          onClick = launchContactPicker,
          colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            contentColor = MaterialTheme.colorScheme.primary
          ),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("btn_pick_contact")
        ) {
          Icon(
            imageVector = Icons.Default.Contacts,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text("Ambil dari Kontak HP", fontWeight = FontWeight.SemiBold)
        }

        OutlinedTextField(
          value = name,
          onValueChange = {
            name = it
            isError = false
          },
          label = { Text("Nama Pelanggan *") },
          isError = isError,
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it },
          label = { Text("Nomor WhatsApp / HP") },
          trailingIcon = {
            IconButton(onClick = launchContactPicker) {
              Icon(
                imageVector = Icons.Default.Contacts,
                contentDescription = "Pilih dari Kontak",
                tint = MaterialTheme.colorScheme.primary
              )
            }
          },
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
          label = { Text("Catatan Tambahan") },
          maxLines = 2,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isBlank()) {
            isError = true
          } else {
            onSave(name, phone, address, notes)
          }
        }
      ) {
        Text("Simpan")
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Batal")
      }
    }
  )
}
