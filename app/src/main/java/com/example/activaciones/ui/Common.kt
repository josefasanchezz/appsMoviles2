package com.example.activaciones.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.activaciones.App
import com.example.activaciones.data.Priority
import com.example.activaciones.data.Status
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF0B3D91),
            secondary = Color(0xFF1E88E5),
            primaryContainer = Color(0xFFD6E4FF),
            surfaceVariant = Color(0xFFF1F5FB)
        ),
        content = content
    )
}

/** Crea un ViewModel con acceso a la Application (sin librerías de inyección). */
@Composable
inline fun <reified T : ViewModel> appViewModel(key: String? = null, crossinline build: (App) -> T): T {
    val app = LocalContext.current.applicationContext as App
    return viewModel(key = key, factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <V : ViewModel> create(modelClass: Class<V>): V = build(app) as V
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    floating: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (onBack != null) IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = actions,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = floating,
        content = content
    )
}

@Composable
fun Tag(text: String, color: Color) {
    Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(50)) {
        Text(text, color = color, style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
    }
}

fun statusColor(s: Status): Color = when (s) {
    Status.CONFIRMADA -> Color(0xFF2E7D32)
    Status.DESCARTADA -> Color(0xFFC62828)
    Status.IDENTIFICADO, Status.CONTACTO_PENDIENTE -> Color(0xFF546E7A)
    else -> Color(0xFF1565C0)
}

fun priorityColor(p: Priority): Color = when (p) {
    Priority.ALTA -> Color(0xFFC62828)
    Priority.MEDIA -> Color(0xFFEF6C00)
    Priority.BAJA -> Color(0xFF2E7D32)
}

@Composable
fun <T> Picker(
    label: String, value: String, options: List<T>,
    text: (T) -> String, onSelect: (T) -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true
) {
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedButton(onClick = { open = true }, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
            Text("$label: $value", maxLines = 1, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { o ->
                DropdownMenuItem(text = { Text(text(o)) }, onClick = { open = false; onSelect(o) })
            }
        }
    }
}

@Composable
fun DateField(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    Box(modifier) {
        OutlinedTextField(
            value = value, onValueChange = {}, readOnly = true, label = { Text(label) },
            trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        Box(Modifier.matchParentSize().clickable {
            val c = Calendar.getInstance()
            DatePickerDialog(ctx, { _, y, m, d -> onChange("%02d/%02d/%04d".format(d, m + 1, y)) },
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        })
    }
}

fun fmtTs(ts: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "CL")).format(Date(ts))

@Composable
fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            content()
        }
    }
}
