package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.viewmodel.AppViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import kotlinx.coroutines.launch
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

@Composable
fun LoginScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val authError by viewModel.authError.collectAsState()
    val isCodeSent by viewModel.isCodeSent.collectAsState()
    val scope = rememberCoroutineScope()
    
    var phoneNumber by remember { mutableStateOf("") }
    var smsCode by remember { mutableStateOf("") }
    var usePhoneAuth by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Login",
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text("Bienvenido a Casa Campo", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Frutas, verduras y más a domicilio.", style = MaterialTheme.typography.bodyLarge)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (!usePhoneAuth) {
            Button(
                onClick = {
                    scope.launch { viewModel.signInWithGoogle(context) }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Iniciar sesión con Google")
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = { usePhoneAuth = true },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Iniciar sesión con Teléfono")
            }
        } else {
            if (!isCodeSent) {
                var expanded by remember { mutableStateOf(false) }
                val countryCodes = listOf("+52" to "🇲🇽 MX", "+1" to "🇺🇸/🇨🇦", "+34" to "🇪🇸 ES", "+57" to "🇨🇴 CO", "+54" to "🇦🇷 AR", "+56" to "🇨🇱 CL", "+51" to "🇵🇪 PE")
                var selectedCountryCode by remember { mutableStateOf(countryCodes[0].first) }

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(0.35f)) {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth().height(64.dp),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(selectedCountryCode)
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            countryCodes.forEach { (code, name) ->
                                DropdownMenuItem(
                                    text = { Text("$name $code") },
                                    onClick = {
                                        selectedCountryCode = code
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it.filter { char -> char.isDigit() } },
                        label = { Text("Número") },
                        modifier = Modifier.weight(0.65f),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val activity = context.findActivity()
                        if (activity != null) {
                            val fullPhoneNumber = "$selectedCountryCode$phoneNumber"
                            viewModel.sendPhoneVerification(fullPhoneNumber, activity)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = phoneNumber.isNotBlank()
                ) {
                    Text("Enviar SMS de verificación")
                }
            } else {
                OutlinedTextField(
                    value = smsCode,
                    onValueChange = { smsCode = it },
                    label = { Text("Código SMS") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        viewModel.verifyPhoneCode(smsCode)
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = smsCode.isNotBlank()
                ) {
                    Text("Verificar Código")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = { usePhoneAuth = false }) {
                Text("Volver")
            }
        }
        
        if (authError != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = authError!!, color = MaterialTheme.colorScheme.error)
        }
    }
}
