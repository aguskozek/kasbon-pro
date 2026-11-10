package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.KasbonEntity
import com.example.ui.theme.StatusColors
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object RupiahHelper {
  fun format(amount: Long): String {
    val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
    return "Rp " + formatter.format(amount)
  }

  fun parseRaw(input: String): Long {
    val digits = input.filter { it.isDigit() }
    return digits.toLongOrNull() ?: 0L
  }

  fun formatDate(timestamp: Long, pattern: String = "dd MMM yyyy"): String {
    val sdf = SimpleDateFormat(pattern, Locale("id", "ID"))
    return sdf.format(Date(timestamp))
  }
}

@Composable
fun StatusBadge(
  status: String,
  modifier: Modifier = Modifier
) {
  val colors = StatusColors
  val (bgColor, textColor, label) = when (status) {
    KasbonEntity.STATUS_PAID -> Triple(colors.successContainer, colors.success, "Lunas")
    KasbonEntity.STATUS_PARTIAL -> Triple(colors.warningContainer, colors.warning, "Sebagian")
    else -> Triple(colors.dangerContainer, colors.danger, "Belum Lunas")
  }

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(bgColor)
      .padding(horizontal = 8.dp, vertical = 4.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      color = textColor,
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold
    )
  }
}

@Composable
fun MetricStatCard(
  title: String,
  value: String,
  subtitle: String? = null,
  icon: ImageVector,
  accentColor: Color,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
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
          .size(48.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(accentColor.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = accentColor,
          modifier = Modifier.size(26.dp)
        )
      }
      Spacer(modifier = Modifier.width(14.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = value,
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        if (!subtitle.isNullOrEmpty()) {
          Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = accentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }
    }
  }
}

@Composable
fun QuickActionButton(
  label: String,
  icon: ImageVector,
  backgroundColor: Color,
  contentColor: Color = Color.White,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String = "action_btn"
) {
  Card(
    modifier = modifier
      .testTag(testTag)
      .clickable { onClick() },
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = backgroundColor),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 14.dp, horizontal = 10.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(CircleShape)
          .background(Color.White.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = label,
          tint = contentColor,
          modifier = Modifier.size(22.dp)
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = label,
        color = contentColor,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
fun ConfirmationDialog(
  show: Boolean,
  title: String,
  message: String,
  confirmButtonText: String = "Ya, Lanjutkan",
  dismissButtonText: String = "Batal",
  isDanger: Boolean = false,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit
) {
  if (show) {
    AlertDialog(
      onDismissRequest = onDismiss,
      title = { Text(text = title, fontWeight = FontWeight.Bold) },
      text = { Text(text = message) },
      confirmButton = {
        Button(
          onClick = {
            onConfirm()
            onDismiss()
          },
          colors = if (isDanger) ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error
          ) else ButtonDefaults.buttonColors()
        ) {
          Text(confirmButtonText)
        }
      },
      dismissButton = {
        OutlinedButton(onClick = onDismiss) {
          Text(dismissButtonText)
        }
      }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KasbonTopBar(
  title: String,
  subtitle: String? = null,
  showBackButton: Boolean = false,
  onBackClick: (() -> Unit)? = null,
  actions: @Composable () -> Unit = {}
) {
  TopAppBar(
    title = {
      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        if (!subtitle.isNullOrEmpty()) {
          Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    },
    navigationIcon = {
      if (showBackButton && onBackClick != null) {
        IconButton(onClick = onBackClick) {
          Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Kembali",
            tint = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    },
    actions = { actions() },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.background
    )
  )
}

@Composable
fun AppSearchBar(
  query: String,
  onQueryChange: (String) -> Unit,
  placeholder: String,
  modifier: Modifier = Modifier
) {
  OutlinedTextField(
    value = query,
    onValueChange = onQueryChange,
    placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium) },
    leadingIcon = {
      Icon(
        imageVector = Icons.Default.Search,
        contentDescription = "Cari",
        tint = MaterialTheme.colorScheme.onSurfaceVariant
      )
    },
    trailingIcon = {
      if (query.isNotEmpty()) {
        IconButton(onClick = { onQueryChange("") }) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Hapus",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    },
    singleLine = true,
    shape = RoundedCornerShape(12.dp),
    modifier = modifier.fillMaxWidth()
  )
}
