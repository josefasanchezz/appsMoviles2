package com.example.activaciones.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.roundToInt
import com.example.activaciones.data.ActivationEntity
import com.example.activaciones.data.ResultEntity
import com.example.activaciones.vm.DetailViewModel

@Composable
fun ActivationScreen(id: Long, onBack: () -> Unit) {
    val vm = appViewModel(key = "act$id") { DetailViewModel(it, id) }
    val ctx = LocalContext.current
    val execs by vm.executives.collectAsStateWithLifecycle()

    var space by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var req by remember { mutableStateOf("") }
    var respId by remember { mutableStateOf<Long?>(null) }
    var evidence by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        vm.activationOnce()?.let {
            space = it.space; date = it.date; cost = it.cost.toString()
            req = it.requirements; respId = it.responsibleId; evidence = it.evidenceUri
        }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            try { ctx.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            catch (_: Exception) { }
            evidence = uri.toString()
        }
    }

    fun build() = ActivationEntity(id, space.trim(), date, cost.toIntOrNull() ?: 0, req.trim(), respId, evidence)
    fun valid(): Boolean {
        error = when {
            space.isBlank() -> "Indica el espacio asignado"
            date.isBlank() -> "Selecciona la fecha"
            cost.isBlank() -> "Indica el costo (0 si no tiene)"
            else -> null
        }
        return error == null
    }

    AppScaffold("Coordinar participación", onBack = onBack) { pad ->
        Column(Modifier.padding(pad).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(space, { space = it }, label = { Text("Espacio asignado") }, modifier = Modifier.fillMaxWidth())
            DateField("Fecha de participación", date, { date = it })
            OutlinedTextField(cost, { cost = it.filter(Char::isDigit) }, label = { Text("Costo (CLP)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(req, { req = it }, label = { Text("Requisitos") }, modifier = Modifier.fillMaxWidth())
            Picker("Responsable", execs.firstOrNull { it.id == respId }?.fullName ?: "Sin asignar",
                execs, { it.fullName }, { respId = it.id })
            OutlinedButton(onClick = { picker.launch(arrayOf("image/*", "application/pdf", "*/*")) },
                modifier = Modifier.fillMaxWidth()) { Text("Adjuntar evidencia (foto / documento)") }
            evidence?.let { Text("Adjunto: ${Uri.parse(it).lastPathSegment}", style = MaterialTheme.typography.bodySmall) }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = { if (valid()) vm.saveActivation(build(), false, onBack) },
                modifier = Modifier.fillMaxWidth()) { Text("Guardar condiciones") }
            Button(onClick = { if (valid()) vm.saveActivation(build(), true, onBack) },
                colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF2E7D32)),
                modifier = Modifier.fillMaxWidth()) { Text("Guardar y confirmar participación") }
        }
    }
}

private val outcomes = listOf("Exitosa", "Parcialmente exitosa", "Sin resultados", "No realizada")

@Composable
fun ResultScreen(id: Long, onBack: () -> Unit) {
    val vm = appViewModel(key = "res$id") { DetailViewModel(it, id) }
    val row by vm.row.collectAsStateWithLifecycle()
    var outcome by remember { mutableStateOf(outcomes.first()) }
    var obs by remember { mutableStateOf("") }
    var impact by remember { mutableStateOf("") }
    var eval by remember { mutableIntStateOf(3) }
    var error by remember { mutableStateOf<String?>(null) }
    val closed = row?.closed == true

    LaunchedEffect(Unit) {
        vm.resultOnce()?.let { outcome = it.outcome; obs = it.observations; impact = it.impact; eval = it.evaluation }
    }

    fun save(close: Boolean) {
        if (obs.isBlank()) { error = "Agrega observaciones"; return }
        error = null
        vm.saveResult(ResultEntity(id, outcome, obs.trim(), impact.trim(), eval, System.currentTimeMillis()), close, onBack)
    }

    AppScaffold("Registrar resultados", onBack = onBack) { pad ->
        Column(Modifier.padding(pad).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (closed) Text("Esta oportunidad ya está cerrada.", color = MaterialTheme.colorScheme.error)
            Picker("Resultado", outcome, outcomes, { it }, { outcome = it }, enabled = !closed)
            OutlinedTextField(obs, { obs = it }, label = { Text("Observaciones") }, enabled = !closed, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(impact, { impact = it }, label = { Text("Impacto") }, enabled = !closed, modifier = Modifier.fillMaxWidth())
            Text("Evaluación: $eval / 5", style = MaterialTheme.typography.titleSmall)
            Slider(eval.toFloat(), { eval = it.roundToInt() }, valueRange = 1f..5f, steps = 3, enabled = !closed)
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onClick = { save(false) }, enabled = !closed, modifier = Modifier.fillMaxWidth()) { Text("Guardar resultado") }
            Button(onClick = { save(true) }, enabled = !closed, modifier = Modifier.fillMaxWidth()) { Text("Guardar y cerrar oportunidad") }
        }
    }
}
