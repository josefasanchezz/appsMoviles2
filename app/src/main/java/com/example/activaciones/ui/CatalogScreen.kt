package com.example.activaciones.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.activaciones.vm.CatalogViewModel

@Composable
fun CatalogScreen(onBack: () -> Unit) {
    val vm = appViewModel { CatalogViewModel() }
    AppScaffold("Catálogo de eventos externos", onBack = onBack) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {
            if (vm.offline) {
                Text("Sin conexión: mostrando datos ficticios locales.",
                    color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(12.dp))
            }
            if (vm.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                LazyColumn(contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(vm.events, key = { it.id }) { e ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Text("#${e.id} · ${e.name}", style = MaterialTheme.typography.titleSmall)
                                Text("${e.date} · ${e.comuna}")
                                Text(e.organization, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
