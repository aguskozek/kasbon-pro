package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.backup.BackupRestoreManager
import com.example.data.db.AppDatabase
import com.example.data.entity.CustomerEntity
import com.example.data.entity.KasbonEntity
import com.example.data.entity.PaymentEntity
import com.example.data.pref.StoreProfile
import com.example.data.pref.UserPreferencesRepository
import com.example.data.repository.KasbonRepository
import com.example.printer.EscPosReceiptFormatter
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

sealed class AppScreen {
  object Dashboard : AppScreen()
  object Customers : AppScreen()
  object KasbonList : AppScreen()
  object PaymentsList : AppScreen()
  object Settings : AppScreen()
  data class CustomerDetail(val customerId: Long) : AppScreen()
  data class NewKasbon(val preselectedCustomerId: Long? = null) : AppScreen()
  data class NewPayment(val preselectedCustomerId: Long? = null, val preselectedKasbonId: Long? = null) : AppScreen()
  data class ReceiptPreview(
    val title: String,
    val receiptText: String,
    val customerPhone: String,
    val customerName: String,
    val canPrint: Boolean = true
  ) : AppScreen()
  object PrinterSettings : AppScreen()
}

data class DashboardUiState(
  val totalRemainingDebt: Long = 0L,
  val todayTotalPayments: Long = 0L,
  val todayTotalKasbon: Long = 0L,
  val customerCount: Int = 0,
  val dueKasbons: List<KasbonEntity> = emptyList(),
  val recentKasbons: List<KasbonEntity> = emptyList(),
  val recentPayments: List<PaymentEntity> = emptyList()
)

class KasbonViewModel(
  application: Application
) : AndroidViewModel(application) {

  private val database = AppDatabase.getDatabase(application, viewModelScope)
  val repository = KasbonRepository(database.customerDao(), database.kasbonDao(), database.paymentDao())
  val preferencesRepository = UserPreferencesRepository(application)
  val backupManager = BackupRestoreManager(
    application,
    database.customerDao(),
    database.kasbonDao(),
    database.paymentDao()
  )

  // Navigation Stack
  private val _screenStack = MutableStateFlow<List<AppScreen>>(listOf(AppScreen.Dashboard))
  val currentScreen: StateFlow<AppScreen> = _screenStack.combine(MutableStateFlow(Unit)) { stack, _ ->
    stack.lastOrNull() ?: AppScreen.Dashboard
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppScreen.Dashboard)

  fun navigateTo(screen: AppScreen) {
    _screenStack.value = _screenStack.value + screen
  }

  fun navigateBack(): Boolean {
    val current = _screenStack.value
    return if (current.size > 1) {
      _screenStack.value = current.dropLast(1)
      true
    } else {
      false
    }
  }

  fun switchBottomTab(screen: AppScreen) {
    _screenStack.value = listOf(screen)
  }

  // User Preferences
  val themeMode: StateFlow<AppThemeMode> = preferencesRepository.themeMode
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppThemeMode.SYSTEM)

  val storeProfile: StateFlow<StoreProfile> = preferencesRepository.storeProfile
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StoreProfile())

  fun setThemeMode(mode: AppThemeMode) {
    viewModelScope.launch { preferencesRepository.setThemeMode(mode) }
  }

  fun updateStoreProfile(
    name: String,
    address: String,
    phone: String,
    header: String,
    footer: String,
    autoPrint: Boolean,
    copies: Int,
    charsPerLine: Int,
    logoPath: String = storeProfile.value.logoPath,
    qrisImagePath: String = storeProfile.value.qrisImagePath,
    bankName: String = storeProfile.value.bankName,
    bankAccountNumber: String = storeProfile.value.bankAccountNumber,
    bankAccountHolder: String = storeProfile.value.bankAccountHolder
  ) {
    viewModelScope.launch {
      preferencesRepository.updateStoreProfile(
        storeName = name,
        storeAddress = address,
        storePhone = phone,
        receiptHeader = header,
        receiptFooter = footer,
        autoPrint = autoPrint,
        printCopies = copies,
        charsPerLine = charsPerLine,
        logoPath = logoPath,
        qrisImagePath = qrisImagePath,
        bankName = bankName,
        bankAccountNumber = bankAccountNumber,
        bankAccountHolder = bankAccountHolder
      )
      _toastMessage.value = "Pengaturan profil usaha disimpan."
    }
  }

  fun saveStoreLogo(path: String) {
    viewModelScope.launch {
      preferencesRepository.saveStoreLogo(path)
      _toastMessage.value = if (path.isEmpty()) "Logo usaha dihapus." else "Logo usaha berhasil diperbarui."
    }
  }

  fun updatePaperWidth(charsPerLine: Int) {
    viewModelScope.launch {
      preferencesRepository.updatePaperWidth(charsPerLine)
      _toastMessage.value = "Format lebar kertas diubah ke $charsPerLine karakter (${if (charsPerLine <= 32) "58mm" else "80mm"})."
    }
  }

  // Data Flows
  val allCustomers = repository.allCustomers
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allKasbons = repository.allKasbons
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allPayments = repository.allPayments
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Dashboard Aggregation
  private val dashboardStats = combine(
    repository.totalRemainingDebt,
    repository.getTodayTotalPayments(),
    repository.getTodayTotalKasbon(),
    repository.customerCount,
    repository.getDueKasbons()
  ) { totalDebt, todayPayments, todayKasbon, count, dueList ->
    Triple(totalDebt ?: 0L, todayPayments ?: 0L, todayKasbon ?: 0L) to Pair(count, dueList)
  }

  val dashboardState: StateFlow<DashboardUiState> = combine(
    dashboardStats,
    repository.allKasbons,
    repository.allPayments
  ) { stats, kasbons, payments ->
    val (tri, countAndDue) = stats
    val (totalDebt, todayPayments, todayKasbon) = tri
    val (count, dueList) = countAndDue
    DashboardUiState(
      totalRemainingDebt = totalDebt,
      todayTotalPayments = todayPayments,
      todayTotalKasbon = todayKasbon,
      customerCount = count,
      dueKasbons = dueList,
      recentKasbons = kasbons.take(5),
      recentPayments = payments.take(5)
    )
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

  // Notifications / UI Feedback
  private val _toastMessage = MutableStateFlow<String?>(null)
  val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

  fun clearToast() {
    _toastMessage.value = null
  }

  // Customer Management
  fun addCustomer(name: String, phone: String, address: String, notes: String, onSuccess: (Long) -> Unit = {}) {
    viewModelScope.launch {
      if (name.isBlank()) {
        _toastMessage.value = "Nama pelanggan wajib diisi."
        return@launch
      }
      val id = repository.insertCustomer(name, phone, address, notes)
      _toastMessage.value = "Pelanggan $name berhasil ditambahkan."
      onSuccess(id)
    }
  }

  fun updateCustomer(customer: CustomerEntity) {
    viewModelScope.launch {
      repository.updateCustomer(customer)
      _toastMessage.value = "Data pelanggan berhasil diperbarui."
    }
  }

  fun deleteCustomer(customer: CustomerEntity) {
    viewModelScope.launch {
      repository.deleteCustomer(customer)
      _toastMessage.value = "Pelanggan ${customer.name} telah dihapus."
      navigateBack()
    }
  }

  // Kasbon Operations
  fun createKasbon(
    customerId: Long,
    amount: Long,
    note: String,
    dueDate: Long,
    onSuccess: (KasbonEntity) -> Unit = {}
  ) {
    viewModelScope.launch {
      if (amount <= 0) {
        _toastMessage.value = "Nominal kasbon harus lebih dari 0."
        return@launch
      }
      val kasbon = repository.createKasbon(customerId, amount, note, dueDate)
      _toastMessage.value = "Kasbon berhasil dicatat!"

      // Prepare preview receipt
      val customer = allCustomers.value.find { it.id == customerId }
      if (customer != null) {
        val totalCustomerDebt = repository.getCustomerTotalDebt(customerId)
        val text = EscPosReceiptFormatter.generateNewKasbonReceiptText(
          store = storeProfile.value,
          customer = customer,
          kasbon = kasbon,
          totalCustomerDebt = totalCustomerDebt
        )
        navigateTo(
          AppScreen.ReceiptPreview(
            title = "Struk Kasbon Baru",
            receiptText = text,
            customerPhone = customer.phone,
            customerName = customer.name
          )
        )
      } else {
        navigateBack()
      }
      onSuccess(kasbon)
    }
  }

  fun deleteKasbon(kasbon: KasbonEntity) {
    viewModelScope.launch {
      repository.deleteKasbon(kasbon)
      _toastMessage.value = "Catatan kasbon telah dihapus."
    }
  }

  // Payment Operations
  fun processPayment(
    customerId: Long,
    kasbonId: Long?,
    amountPaid: Long,
    paymentMethod: String,
    notes: String,
    onSuccess: (PaymentEntity) -> Unit = {}
  ) {
    viewModelScope.launch {
      if (amountPaid <= 0) {
        _toastMessage.value = "Nominal pembayaran harus lebih dari 0."
        return@launch
      }
      val customer = allCustomers.value.find { it.id == customerId }
      if (customer == null) {
        _toastMessage.value = "Pelanggan tidak ditemukan."
        return@launch
      }

      val totalKasbonBefore = repository.getCustomerTotalKasbon(customerId)
      val totalPaidBefore = repository.getCustomerTotalPaid(customerId)

      val payment = repository.recordPayment(
        customerId = customerId,
        kasbonId = kasbonId,
        amountPaid = amountPaid,
        paymentMethod = paymentMethod,
        notes = notes
      )

      val remainingDebtAfter = repository.getCustomerTotalDebt(customerId)
      _toastMessage.value = "Pembayaran sebesar ${EscPosReceiptFormatter.formatRupiah(amountPaid)} berhasil dicatat!"

      // Build receipt preview
      val text = EscPosReceiptFormatter.generatePaymentReceiptText(
        store = storeProfile.value,
        customer = customer,
        payment = payment,
        totalKasbon = totalKasbonBefore,
        totalPaidBefore = totalPaidBefore,
        remainingDebt = remainingDebtAfter
      )

      navigateTo(
        AppScreen.ReceiptPreview(
          title = "Bukti Pembayaran Kasbon",
          receiptText = text,
          customerPhone = customer.phone,
          customerName = customer.name
        )
      )
      onSuccess(payment)
    }
  }

  // Generate Customer Summary Receipt
  fun previewCustomerSummary(customerId: Long) {
    viewModelScope.launch {
      val customer = allCustomers.value.find { it.id == customerId } ?: return@launch
      val kasbons = repository.getKasbonsForCustomer(customerId).first()
      val totalDebt = repository.getCustomerTotalDebt(customerId)
      val text = EscPosReceiptFormatter.generateCustomerSummaryText(
        store = storeProfile.value,
        customer = customer,
        kasbons = kasbons,
        totalDebt = totalDebt
      )
      navigateTo(
        AppScreen.ReceiptPreview(
          title = "Rekap Kasbon Pelanggan",
          receiptText = text,
          customerPhone = customer.phone,
          customerName = customer.name
        )
      )
    }
  }

  // Backup & Restore
  fun exportBackup(onReady: (File) -> Unit) {
    viewModelScope.launch {
      val res = backupManager.exportBackupToJson()
      if (res.isSuccess) {
        val file = res.getOrThrow()
        _toastMessage.value = "Berkas cadangan dibuat: ${file.name}"
        onReady(file)
      } else {
        _toastMessage.value = "Gagal membuat cadangan: ${res.exceptionOrNull()?.message}"
      }
    }
  }

  fun importBackup(jsonString: String) {
    viewModelScope.launch {
      val res = backupManager.importBackupFromJson(jsonString)
      if (res.isSuccess) {
        _toastMessage.value = res.getOrThrow()
      } else {
        _toastMessage.value = "Gagal memulihkan cadangan: ${res.exceptionOrNull()?.message}"
      }
    }
  }
}
