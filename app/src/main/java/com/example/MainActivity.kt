package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Paid
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ui.screens.CustomerDetailScreen
import com.example.ui.screens.CustomersScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.KasbonListScreen
import com.example.ui.screens.NewKasbonScreen
import com.example.ui.screens.NewPaymentScreen
import com.example.ui.screens.PaymentsListScreen
import com.example.ui.screens.PrinterSettingsScreen
import com.example.ui.screens.ReceiptPreviewScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.KhmKasbonTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.KasbonViewModel
import com.example.ui.viewmodel.PrinterViewModel

class MainActivity : ComponentActivity() {

  private val kasbonViewModel: KasbonViewModel by viewModels()

  private val printerViewModel: PrinterViewModel by viewModels {
    object : ViewModelProvider.Factory {
      @Suppress("UNCHECKED_CAST")
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PrinterViewModel(application, kasbonViewModel.preferencesRepository) as T
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val themeMode by kasbonViewModel.themeMode.collectAsState()

      KhmKasbonTheme(themeMode = themeMode) {
        MainAppContent(
          kasbonViewModel = kasbonViewModel,
          printerViewModel = printerViewModel
        )
      }
    }
  }
}

@Composable
fun MainAppContent(
  kasbonViewModel: KasbonViewModel,
  printerViewModel: PrinterViewModel
) {
  val context = LocalContext.current
  val currentScreen by kasbonViewModel.currentScreen.collectAsState()
  val toastMessage by kasbonViewModel.toastMessage.collectAsState()
  val printerMessage by printerViewModel.message.collectAsState()
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(toastMessage) {
    toastMessage?.let { msg ->
      Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
      kasbonViewModel.clearToast()
    }
  }

  LaunchedEffect(printerMessage) {
    printerMessage?.let { msg ->
      Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
      printerViewModel.clearMessage()
    }
  }

  val isTopLevelScreen = when (currentScreen) {
    is AppScreen.Dashboard,
    is AppScreen.Customers,
    is AppScreen.KasbonList,
    is AppScreen.PaymentsList,
    is AppScreen.Settings -> true
    else -> false
  }

  Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    bottomBar = {
      if (isTopLevelScreen) {
        BottomNavigationBar(
          currentScreen = currentScreen,
          onSelectScreen = { screen ->
            kasbonViewModel.switchBottomTab(screen)
          }
        )
      }
    },
    modifier = Modifier.fillMaxSize()
  ) { padding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(if (isTopLevelScreen) padding else androidx.compose.foundation.layout.PaddingValues())
    ) {
      when (val screen = currentScreen) {
        is AppScreen.Dashboard -> {
          DashboardScreen(
            viewModel = kasbonViewModel,
            printerViewModel = printerViewModel
          )
        }
        is AppScreen.Customers -> {
          CustomersScreen(
            viewModel = kasbonViewModel
          )
        }
        is AppScreen.KasbonList -> {
          KasbonListScreen(
            viewModel = kasbonViewModel
          )
        }
        is AppScreen.PaymentsList -> {
          PaymentsListScreen(
            viewModel = kasbonViewModel
          )
        }
        is AppScreen.Settings -> {
          SettingsScreen(
            viewModel = kasbonViewModel
          )
        }
        is AppScreen.CustomerDetail -> {
          CustomerDetailScreen(
            customerId = screen.customerId,
            viewModel = kasbonViewModel
          )
        }
        is AppScreen.NewKasbon -> {
          NewKasbonScreen(
            preselectedCustomerId = screen.preselectedCustomerId,
            viewModel = kasbonViewModel
          )
        }
        is AppScreen.NewPayment -> {
          NewPaymentScreen(
            preselectedCustomerId = screen.preselectedCustomerId,
            preselectedKasbonId = screen.preselectedKasbonId,
            viewModel = kasbonViewModel
          )
        }
        is AppScreen.ReceiptPreview -> {
          ReceiptPreviewScreen(
            title = screen.title,
            receiptText = screen.receiptText,
            customerPhone = screen.customerPhone,
            customerName = screen.customerName,
            viewModel = kasbonViewModel,
            printerViewModel = printerViewModel
          )
        }
        is AppScreen.PrinterSettings -> {
          PrinterSettingsScreen(
            viewModel = kasbonViewModel,
            printerViewModel = printerViewModel
          )
        }
      }
    }
  }
}

@Composable
fun BottomNavigationBar(
  currentScreen: AppScreen,
  onSelectScreen: (AppScreen) -> Unit
) {
  NavigationBar(
    containerColor = MaterialTheme.colorScheme.surface,
    tonalElevation = 4.dp,
    modifier = Modifier
      .windowInsetsPadding(WindowInsets.navigationBars)
      .testTag("bottom_nav_bar")
  ) {
    val navColors = androidx.compose.material3.NavigationBarItemDefaults.colors(
      selectedIconColor = MaterialTheme.colorScheme.primary,
      selectedTextColor = MaterialTheme.colorScheme.primary,
      indicatorColor = MaterialTheme.colorScheme.primaryContainer,
      unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
      unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    // 1. Beranda
    val isHome = currentScreen is AppScreen.Dashboard
    NavigationBarItem(
      icon = {
        Icon(
          imageVector = if (isHome) Icons.Default.Home else Icons.Outlined.Home,
          contentDescription = "Beranda",
          modifier = Modifier.size(22.dp)
        )
      },
      label = {
        Text(
          text = "Beranda",
          fontSize = 11.sp,
          fontWeight = if (isHome) FontWeight.Bold else FontWeight.Medium,
          maxLines = 1,
          softWrap = false,
          letterSpacing = (-0.2).sp
        )
      },
      alwaysShowLabel = true,
      selected = isHome,
      onClick = { onSelectScreen(AppScreen.Dashboard) },
      colors = navColors,
      modifier = Modifier.testTag("nav_home")
    )

    // 2. Pelanggan
    val isCust = currentScreen is AppScreen.Customers
    NavigationBarItem(
      icon = {
        Icon(
          imageVector = if (isCust) Icons.Default.People else Icons.Outlined.People,
          contentDescription = "Pelanggan",
          modifier = Modifier.size(22.dp)
        )
      },
      label = {
        Text(
          text = "Pelanggan",
          fontSize = 11.sp,
          fontWeight = if (isCust) FontWeight.Bold else FontWeight.Medium,
          maxLines = 1,
          softWrap = false,
          letterSpacing = (-0.3).sp
        )
      },
      alwaysShowLabel = true,
      selected = isCust,
      onClick = { onSelectScreen(AppScreen.Customers) },
      colors = navColors,
      modifier = Modifier.testTag("nav_customers")
    )

    // 3. Kasbon
    val isKasbon = currentScreen is AppScreen.KasbonList
    NavigationBarItem(
      icon = {
        Icon(
          imageVector = if (isKasbon) Icons.Default.ReceiptLong else Icons.Outlined.ReceiptLong,
          contentDescription = "Kasbon",
          modifier = Modifier.size(22.dp)
        )
      },
      label = {
        Text(
          text = "Kasbon",
          fontSize = 11.sp,
          fontWeight = if (isKasbon) FontWeight.Bold else FontWeight.Medium,
          maxLines = 1,
          softWrap = false,
          letterSpacing = (-0.2).sp
        )
      },
      alwaysShowLabel = true,
      selected = isKasbon,
      onClick = { onSelectScreen(AppScreen.KasbonList) },
      colors = navColors,
      modifier = Modifier.testTag("nav_kasbon")
    )

    // 4. Pembayaran
    val isPayment = currentScreen is AppScreen.PaymentsList
    NavigationBarItem(
      icon = {
        Icon(
          imageVector = if (isPayment) Icons.Default.Paid else Icons.Outlined.Paid,
          contentDescription = "Pembayaran",
          modifier = Modifier.size(22.dp)
        )
      },
      label = {
        Text(
          text = "Pembayaran",
          fontSize = 10.5.sp,
          fontWeight = if (isPayment) FontWeight.Bold else FontWeight.Medium,
          maxLines = 1,
          softWrap = false,
          letterSpacing = (-0.4).sp
        )
      },
      alwaysShowLabel = true,
      selected = isPayment,
      onClick = { onSelectScreen(AppScreen.PaymentsList) },
      colors = navColors,
      modifier = Modifier.testTag("nav_payments")
    )

    // 5. Pengaturan
    val isSettings = currentScreen is AppScreen.Settings
    NavigationBarItem(
      icon = {
        Icon(
          imageVector = if (isSettings) Icons.Default.Settings else Icons.Outlined.Settings,
          contentDescription = "Pengaturan",
          modifier = Modifier.size(22.dp)
        )
      },
      label = {
        Text(
          text = "Pengaturan",
          fontSize = 10.5.sp,
          fontWeight = if (isSettings) FontWeight.Bold else FontWeight.Medium,
          maxLines = 1,
          softWrap = false,
          letterSpacing = (-0.4).sp
        )
      },
      alwaysShowLabel = true,
      selected = isSettings,
      onClick = { onSelectScreen(AppScreen.Settings) },
      colors = navColors,
      modifier = Modifier.testTag("nav_settings")
    )
  }
}
