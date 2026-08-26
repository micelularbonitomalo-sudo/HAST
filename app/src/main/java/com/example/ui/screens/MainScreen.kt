package com.example.ui.screens

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.viewmodel.AppViewModel
import com.example.data.UserRole

@Composable
fun MainScreen(viewModel: AppViewModel) {
    val user by viewModel.currentUser.collectAsState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (user == null) {
            LoginScreen(viewModel)
        } else {
            when (user?.role) {
                UserRole.CUSTOMER -> CustomerScreen(viewModel)
                UserRole.ADMIN -> AdminScreen(viewModel)
                UserRole.STAFF -> DeliveryScreen(viewModel)
                null -> LoginScreen(viewModel)
            }
        }
        
        // Show Elena on top of everything when logged in
        if (user != null) {
            ElenaChatOverlay(appViewModel = viewModel)
        }
    }
}
