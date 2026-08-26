package com.example.ui.screens

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.viewmodel.AppViewModel
import com.example.data.UserRole
import androidx.compose.ui.window.DialogProperties
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext


@Composable
fun MainScreen(viewModel: AppViewModel) {
    val user by viewModel.currentUser.collectAsState()

    val updateRequired by viewModel.updateRequired.collectAsState()
    val updateUrl by viewModel.updateUrl.collectAsState()
    val context = LocalContext.current
    
    if (updateRequired && updateUrl.isNotEmpty()) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { },
            title = { androidx.compose.material3.Text("Actualización Disponible") },
            text = { androidx.compose.material3.Text("Hay una nueva versión de Casa Campo. Es necesario actualizar para continuar usando la aplicación.") },
            confirmButton = {
                androidx.compose.material3.Button(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl))
                    context.startActivity(intent)
                }) {
                    androidx.compose.material3.Text("Descargar e Instalar")
                }
            },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        )
    }

    
    androidx.compose.material3.Surface(modifier = Modifier.fillMaxSize(), color = androidx.compose.material3.MaterialTheme.colorScheme.background) {
        if (user == null) {
            LoginScreen(viewModel)
        } else {
            when (user?.role) {
                UserRole.ADMIN -> AdminScreen(viewModel)
                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        androidx.compose.material3.Text(
                            "Acceso Denegado", 
                            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        androidx.compose.material3.Text(
                            "Tu cuenta (${user?.email ?: user?.name}) no tiene permisos de administrador.",
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        androidx.compose.material3.Button(onClick = { viewModel.signOut() }) {
                            androidx.compose.material3.Text("Cerrar Sesión")
                        }
                    }
                }
            }
        }
    }
}
