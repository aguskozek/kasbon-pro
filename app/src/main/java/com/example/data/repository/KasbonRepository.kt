package com.example.data.repository

import com.example.data.dao.CustomerDao
import com.example.data.dao.KasbonDao
import com.example.data.dao.PaymentDao
import com.example.data.entity.CustomerEntity
import com.example.data.entity.KasbonEntity
import com.example.data.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class KasbonRepository(
  private val customerDao: CustomerDao,
  private val kasbonDao: KasbonDao,
  private val paymentDao: PaymentDao
) {
  val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
  val allKasbons: Flow<List<KasbonEntity>> = kasbonDao.getAllKasbons()
  val activeKasbons: Flow<List<KasbonEntity>> = kasbonDao.getActiveKasbons()
  val allPayments: Flow<List<PaymentEntity>> = paymentDao.getAllPayments()
  val totalRemainingDebt: Flow<Long?> = kasbonDao.getTotalRemainingDebt()
  val customerCount: Flow<Int> = customerDao.getCustomerCount()

  fun getCustomerById(id: Long): Flow<CustomerEntity?> = customerDao.getCustomerById(id)
  fun getKasbonsForCustomer(customerId: Long): Flow<List<KasbonEntity>> = kasbonDao.getKasbonsForCustomer(customerId)
  fun getPaymentsForCustomer(customerId: Long): Flow<List<PaymentEntity>> = paymentDao.getPaymentsForCustomer(customerId)
  fun searchCustomers(query: String): Flow<List<CustomerEntity>> = customerDao.searchCustomers(query)

  fun getDueKasbons(): Flow<List<KasbonEntity>> {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    val endOfToday = cal.timeInMillis
    return kasbonDao.getDueKasbons(endOfToday)
  }

  fun getTodayKasbons(): Flow<List<KasbonEntity>> {
    val (startOfDay, endOfDay) = getTodayRange()
    return kasbonDao.getKasbonsBetween(startOfDay, endOfDay)
  }

  fun getTodayPayments(): Flow<List<PaymentEntity>> {
    val (startOfDay, endOfDay) = getTodayRange()
    return paymentDao.getPaymentsBetween(startOfDay, endOfDay)
  }

  fun getTodayTotalPayments(): Flow<Long?> {
    val (startOfDay, endOfDay) = getTodayRange()
    return paymentDao.getTodayTotalPayments(startOfDay, endOfDay)
  }

  fun getTodayTotalKasbon(): Flow<Long?> {
    val (startOfDay, endOfDay) = getTodayRange()
    return kasbonDao.getTodayTotalKasbon(startOfDay, endOfDay)
  }

  private fun getTodayRange(): Pair<Long, Long> {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val start = cal.timeInMillis
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    cal.set(Calendar.MILLISECOND, 999)
    val end = cal.timeInMillis
    return Pair(start, end)
  }

  suspend fun insertCustomer(name: String, phone: String, address: String, notes: String): Long {
    val customer = CustomerEntity(
      name = name.trim(),
      phone = phone.trim(),
      address = address.trim(),
      notes = notes.trim()
    )
    return customerDao.insertCustomer(customer)
  }

  suspend fun updateCustomer(customer: CustomerEntity) {
    customerDao.updateCustomer(customer)
  }

  suspend fun deleteCustomer(customer: CustomerEntity) {
    customerDao.deleteCustomer(customer)
  }

  suspend fun createKasbon(
    customerId: Long,
    amount: Long,
    note: String,
    dueDate: Long,
    date: Long = System.currentTimeMillis()
  ): KasbonEntity {
    val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(date))
    val randomSuffix = (100..999).random()
    val invoiceNumber = "KSB-$dateStr-$randomSuffix"

    val kasbon = KasbonEntity(
      customerId = customerId,
      invoiceNumber = invoiceNumber,
      amount = amount,
      remainingAmount = amount,
      note = note.trim(),
      date = date,
      dueDate = dueDate,
      status = KasbonEntity.STATUS_UNPAID
    )
    val id = kasbonDao.insertKasbon(kasbon)
    return kasbon.copy(id = id)
  }

  suspend fun deleteKasbon(kasbon: KasbonEntity) {
    kasbonDao.deleteKasbon(kasbon)
  }

  suspend fun recordPayment(
    customerId: Long,
    kasbonId: Long?,
    amountPaid: Long,
    paymentMethod: String,
    notes: String,
    paymentDate: Long = System.currentTimeMillis()
  ): PaymentEntity {
    val dateStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(paymentDate))
    val randomSuffix = (100..999).random()
    val receiptNumber = "BYR-$dateStr-$randomSuffix"

    // If specific kasbon selected
    if (kasbonId != null && kasbonId > 0) {
      val kasbon = kasbonDao.getKasbonByIdDirect(kasbonId)
      if (kasbon != null) {
        val newRemaining = maxOf(0L, kasbon.remainingAmount - amountPaid)
        val newStatus = if (newRemaining == 0L) KasbonEntity.STATUS_PAID else KasbonEntity.STATUS_PARTIAL
        kasbonDao.updateKasbon(
          kasbon.copy(
            remainingAmount = newRemaining,
            status = newStatus
          )
        )
      }
    } else {
      // General customer payment: apply payment across active kasbons (oldest first)
      var remainingPaymentToApply = amountPaid
      val customerKasbons = kasbonDao.getKasbonsForCustomerDirect(customerId)
        .filter { it.status != KasbonEntity.STATUS_PAID }
        .sortedBy { it.date }

      for (k in customerKasbons) {
        if (remainingPaymentToApply <= 0) break
        val deduct = minOf(remainingPaymentToApply, k.remainingAmount)
        val updatedRemaining = k.remainingAmount - deduct
        val updatedStatus = if (updatedRemaining == 0L) KasbonEntity.STATUS_PAID else KasbonEntity.STATUS_PARTIAL
        kasbonDao.updateKasbon(
          k.copy(
            remainingAmount = updatedRemaining,
            status = updatedStatus
          )
        )
        remainingPaymentToApply -= deduct
      }
    }

    // Calculate customer's remaining debt after this payment
    val updatedCustomerKasbons = kasbonDao.getKasbonsForCustomerDirect(customerId)
    val totalRemainingCustomerDebt = updatedCustomerKasbons.sumOf { it.remainingAmount }

    val payment = PaymentEntity(
      customerId = customerId,
      kasbonId = kasbonId,
      receiptNumber = receiptNumber,
      amountPaid = amountPaid,
      remainingDebtAfter = totalRemainingCustomerDebt,
      paymentDate = paymentDate,
      paymentMethod = paymentMethod,
      notes = notes.trim()
    )
    val id = paymentDao.insertPayment(payment)
    return payment.copy(id = id)
  }

  suspend fun getCustomerTotalDebt(customerId: Long): Long {
    val kasbons = kasbonDao.getKasbonsForCustomerDirect(customerId)
    return kasbons.sumOf { it.remainingAmount }
  }

  suspend fun getCustomerTotalKasbon(customerId: Long): Long {
    val kasbons = kasbonDao.getKasbonsForCustomerDirect(customerId)
    return kasbons.sumOf { it.amount }
  }

  suspend fun getCustomerTotalPaid(customerId: Long): Long {
    val payments = paymentDao.getPaymentsForCustomerDirect(customerId)
    return payments.sumOf { it.amountPaid }
  }
}
