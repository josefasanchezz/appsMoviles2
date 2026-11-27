package com.example.activaciones.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.activaciones.data.NewOpportunity
import com.example.activaciones.data.Priority
import com.example.activaciones.data.Role
import com.example.activaciones.vm.FormViewModel

@Composable
fun FormScreen(onBack: () -> Unit, onSaved: (Long) -> Unit) {
    val vm = appViewModel { FormViewModel(it) }
    val execs by vm.executives.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var comuna by remember { mutableStateOf("") }
    var org by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var audience by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.MEDIA) }
    var responsibleId by remember { mutableStateOf(vm.user.id) }
    var error by remember { mutableStateOf<String?>(null) }

    AppScaffold("Registrar oportunidad", onBack = onBack) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(name, { name = it }, label = { Text("Evento / actividad") }, modifier = Modifier.fillMaxWidth())
            DateField("Fecha del evento", date, { date = it })
            OutlinedTextField(comuna, { comuna = it }, label = { Text("Comuna") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(org, { org = it }, label = { Text("Organización") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(contact, { contact = it }, label = { Text("Contacto (ficticio)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(audience, { audience = it.filter(Char::isDigit) }, label = { Text("Público esperado") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            Text("Prioridad", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Priority.entries.forEach { p ->
                    FilterChip(priority == p, { priority = p }, label = { Text(p.label) })
                }
            }
            if (vm.user.role == Role.SUPERVISOR) {
                Picker("Responsable", execs.firstOrNull { it.id == responsibleId }?.fullName ?: "—",
                    execs, { it.fullName }, { responsibleId = it.id })
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(
                onClick = {
                    val aud = audience.toIntOrNull()
                    when {
                        name.isBlank() || comuna.isBlank() || org.isBlank() || contact.isBlank() ->
                            error = "Completa todos los campos"
                        date.isBlank() -> error = "Selecciona la fecha"
                        aud == null || aud <= 0 -> error = "Ingresa un público esperado válido"
                        else -> {
                            error = null
                            vm.save(NewOpportunity(name, date, comuna, org, contact, aud, priority, responsibleId), onSaved)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar oportunidad") }
        }
    }
}
