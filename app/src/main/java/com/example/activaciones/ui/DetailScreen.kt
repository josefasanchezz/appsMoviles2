package com.example.activaciones.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.activaciones.data.OpportunityRow
import com.example.activaciones.data.Priority
import com.example.activaciones.data.Role
import com.example.activaciones.data.Status
import com.example.activaciones.vm.DetailViewModel

private val followTypes = listOf("Llamada", "Correo", "Reunión", "Mensaje", "Propuesta")
private val nextActions = listOf("Seguimiento", "Reunión", "Respuesta", "Ninguna")

@Composable
fun DetailScreen(id: Long, onBack: () -> Unit, onActivation: () -> Unit, onResult: () -> Unit) {
    val vm = appViewModel(key = "detail$id") { DetailViewModel(it, id) }
    val row by vm.row.collectAsStateWithLifecycle()
    val follow by vm.followUps.collectAsStateWithLifecycle()
    val history by vm.history.collectAsStateWithLifecycle()
    val activation by vm.activation.collectAsStateWithLifecycle()
    val result by vm.result.collectAsStateWithLifecycle()
    val execs by vm.executives.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }

    AppScaffold("Detalle de oportunidad", onBack = onBack) { pad ->
        val r = row
        if (r == null) {
            Box(Modifier.padding(pad).fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@AppScaffold
        }
        LazyColumn(
            Modifier.padding(pad).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { InfoCard(r) }

            if (!r.closed && r.status.nextOptions().isNotEmpty()) {
                item {
                    Section("Avanzar estado") {
                        r.status.nextOptions().forEach { s ->
                            Button(onClick = { vm.changeStatus(s) }, modifier = Modifier.fillMaxWidth(),
                                colors = if (s == Status.DESCARTADA)
                                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                else ButtonDefaults.buttonColors()) { Text("Pasar a: ${s.label}") }
                        }
                    }
                }
            }

            if (vm.user.role == Role.SUPERVISOR && !r.closed) {
                item {
                    Section("Supervisión") {
                        Picker("Prioridad", r.priority.label, Priority.entries, { it.label }, { vm.setPriority(it) })
                        Picker("Responsable", r.responsibleName ?: "—", execs, { it.fullName }, { vm.reassign(it) })
                    }
                }
            }

            item {
                Section("Acciones") {
                    OutlinedButton(onClick = { showDialog = true }, enabled = !r.closed,
                        modifier = Modifier.fillMaxWidth()) { Text("Registrar gestión / propuesta") }
                    OutlinedButton(onClick = onActivation, enabled = r.status != Status.DESCARTADA,
                        modifier = Modifier.fillMaxWidth()) { Text("Coordinar participación") }
                    OutlinedButton(onClick = onResult, enabled = r.status.isFinal,
                        modifier = Modifier.fillMaxWidth()) { Text("Registrar resultados / cerrar") }
                    activation?.let {
                        Text("Participación: ${it.space} · ${it.date} · costo $${it.cost}",
                            style = MaterialTheme.typography.bodySmall)
                    }
                    result?.let {
                        Text("Resultado: ${it.outcome} · evaluación ${it.evaluation}/5",
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            item { Text("Gestiones y propuestas", style = MaterialTheme.typography.titleMedium) }
            if (follow.isEmpty()) item { Text("Aún no hay gestiones registradas.") }
            items(follow, key = { "f${it.id}" }) { f ->
                Card { Column(Modifier.padding(12.dp)) {
                    Text("${f.type}${if (f.proposalSent) " · propuesta enviada" else ""}",
                        style = MaterialTheme.typography.titleSmall)
                    if (f.notes.isNotBlank()) Text(f.notes)
                    Text("Próxima acción: ${f.nextAction} · ${fmtTs(f.createdAt)}",
                        style = MaterialTheme.typography.bodySmall)
                } }
            }

            item { Text("Historial de cambios", style = MaterialTheme.typography.titleMedium) }
            items(history.size) { i ->
                val h = history[i]
                Column {
                    Text(h.description)
                    Text("${h.authorName ?: "Sistema"} · ${fmtTs(h.createdAt)}", style = MaterialTheme.typography.bodySmall)
                    HorizontalDivider(Modifier.padding(top = 6.dp))
                }
            }
        }
    }

    if (showDialog) {
        FollowUpDialog(
            onDismiss = { showDialog = false },
            onSave = { type, notes, proposal, next ->
                vm.addFollowUp(type, notes, proposal, next); showDialog = false
            }
        )
    }
}

@Composable
private fun InfoCard(r: OpportunityRow) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(r.eventName, style = MaterialTheme.typography.titleLarge)
            Text("Organización: ${r.organizationName}")
            Text("Contacto: ${r.contactName}")
            Text("Fecha: ${r.eventDate} · Comuna: ${r.comuna}")
            Text("Público esperado: ${r.expectedAudience}")
            Text("Responsable: ${r.responsibleName ?: "—"}")
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Tag(r.status.label, statusColor(r.status))
                Tag("Prioridad ${r.priority.label}", priorityColor(r.priority))
                if (r.closed) Tag("Cerrada", androidx.compose.ui.graphics.Color.DarkGray)
            }
        }
    }
}

@Composable
private fun FollowUpDialog(onDismiss: () -> Unit, onSave: (String, String, Boolean, String) -> Unit) {
    var type by remember { mutableStateOf(followTypes.first()) }
    var notes by remember { mutableStateOf("") }
    var proposal by remember { mutableStateOf(false) }
    var next by remember { mutableStateOf(nextActions.first()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar gestión") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Picker("Tipo", type, followTypes, { it }, { type = it })
                OutlinedTextField(notes, { notes = it }, label = { Text("Detalle de la gestión") })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(proposal, { proposal = it }); Text("Se envió una propuesta")
                }
                Picker("Próxima acción", next, nextActions, { it }, { next = it })
            }
        },
        confirmButton = { TextButton(onClick = { onSave(type, notes.trim(), proposal, next) }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
