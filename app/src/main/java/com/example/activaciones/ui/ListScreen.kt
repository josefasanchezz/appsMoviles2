package com.example.activaciones.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.activaciones.data.Priority
import com.example.activaciones.data.Role
import com.example.activaciones.data.Status
import com.example.activaciones.vm.ListViewModel

@Composable
fun ListScreen(onOpen: (Long) -> Unit, onNew: () -> Unit, onCatalog: () -> Unit, onLogout: () -> Unit) {
    val vm = appViewModel { ListViewModel(it) }
    val items by vm.items.collectAsStateWithLifecycle()
    val q by vm.query.collectAsStateWithLifecycle()
    val st by vm.status.collectAsStateWithLifecycle()
    val pr by vm.priority.collectAsStateWithLifecycle()
    val role = vm.user?.role

    AppScaffold(
        title = if (role == Role.SUPERVISOR) "Oportunidades (supervisor)" else "Mis oportunidades",
        actions = {
            IconButton(onClick = onCatalog) { Icon(Icons.Default.Public, "Catálogo externo") }
            IconButton(onClick = { vm.logout(); onLogout() }) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, "Cerrar sesión")
            }
        },
        floating = {
            FloatingActionButton(onClick = onNew) { Icon(Icons.Default.Add, "Nueva oportunidad") }
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            OutlinedTextField(
                q, { vm.query.value = it }, label = { Text("Buscar evento, organización, comuna…") },
                singleLine = true, modifier = Modifier.fillMaxWidth().padding(12.dp, 8.dp)
            )
            LazyRow(contentPadding = PaddingValues(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { FilterChip(st == null, { vm.status.value = null }, label = { Text("Todos") }) }
                items(Status.entries) { s ->
                    FilterChip(st == s, { vm.status.value = if (st == s) null else s }, label = { Text(s.label) })
                }
            }
            LazyRow(contentPadding = PaddingValues(12.dp, 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Priority.entries) { p ->
                    FilterChip(pr == p, { vm.priority.value = if (pr == p) null else p },
                        label = { Text("Prioridad ${p.label}") })
                }
            }
            if (items.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(24.dp)) { Text("No hay oportunidades con estos filtros.") }
            }
            LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(items, key = { it.id }) { r ->
                    Card(Modifier.fillMaxWidth().clickable { onOpen(r.id) }) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(r.eventName, style = MaterialTheme.typography.titleMedium)
                            Text("${r.organizationName} · ${r.comuna} · ${r.eventDate}",
                                style = MaterialTheme.typography.bodyMedium)
                            Text("Responsable: ${r.responsibleName ?: "—"}", style = MaterialTheme.typography.bodySmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Tag(r.status.label, statusColor(r.status))
                                Tag("Prioridad ${r.priority.label}", priorityColor(r.priority))
                                if (r.closed) Tag("Cerrada", androidx.compose.ui.graphics.Color.DarkGray)
                            }
                        }
                    }
                }
            }
        }
    }
}
