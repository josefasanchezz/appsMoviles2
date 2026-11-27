package com.example.activaciones.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.activaciones.vm.AuthViewModel

@Composable
fun LoginScreen(onLogged: () -> Unit) {
    val vm = appViewModel { AuthViewModel(it) }
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize().padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Gestión de Oportunidades", style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary)
        Text("Activaciones corporativas", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(28.dp))
        OutlinedTextField(user, { user = it }, label = { Text("Usuario (ficticio)") },
            singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(pass, { pass = it }, label = { Text("Contraseña (ficticia)") },
            singleLine = true, visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth())
        vm.error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.height(20.dp))
        Button(onClick = { vm.login(user, pass, onLogged) }, enabled = !vm.loading,
            modifier = Modifier.fillMaxWidth()) { Text("Ingresar") }
        Spacer(Modifier.height(24.dp))
        Text("Demo: ejecutivo / 1234  ·  supervisor / 1234", style = MaterialTheme.typography.bodySmall)
    }
}
