package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.example.ui.screens.MainScreen

import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppViewModel
import com.google.firebase.FirebaseApp
import android.util.Log

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        

        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
                Log.d("FirebaseInit", "Firebase initialized manually in MainActivity")
            }
        } catch (e: Exception) {
            Log.e("FirebaseInit", "Failed to initialize Firebase in MainActivity", e)
        }
        
        requestNotificationPermission()

        
        handleIntent(intent)
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel)
                }
            }
        }
    }


    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                retrieveFcmToken()
            } else {
                val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                    if (isGranted) {
                        retrieveFcmToken()
                    }
                }
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            retrieveFcmToken()
        }
    }

    private fun retrieveFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                viewModel.updateFcmToken(token)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val data = intent.data
        if (data != null && data.scheme == "casacampo" && data.host == "payment") {
            val status = data.pathSegments.firstOrNull() // e.g., "success", "failure", "pending"
            val orderId = data.getQueryParameter("external_reference")
            
            if (orderId != null && status == "success") {
                viewModel.processPayment(orderId)
                Log.d("MercadoPago", "Payment successful for order $orderId")
            } else {
                Log.d("MercadoPago", "Payment $status for order $orderId")
            }
        }
    }
}
