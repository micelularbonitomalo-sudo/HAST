package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.UserRole
import com.example.viewmodel.AppViewModel

@Composable
fun RoleSelectionScreen(viewModel: AppViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Seleccionar Rol (Solo Demo)", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { viewModel.setRoleForDemo(UserRole.CUSTOMER) },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Entrar como Cliente")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { viewModel.setRoleForDemo(UserRole.ADMIN) },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Entrar como Administrador")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { viewModel.setRoleForDemo(UserRole.STAFF) },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Entrar como Staff (Repartidor)")
        }
    }
}
