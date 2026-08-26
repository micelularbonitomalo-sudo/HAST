package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import com.example.viewmodel.AppViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.data.Order
import com.example.data.OrderStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryScreen(viewModel: AppViewModel) {
    val orders by viewModel.orders.collectAsState()
    
    // Solo mostramos pedidos pendientes de entrega
    val activeOrders = orders.filter { it.status == OrderStatus.PENDING || it.status == OrderStatus.PAID || it.status == OrderStatus.OUT_FOR_DELIVERY }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Staff (Repartidor) - Entregas") },
                actions = {
                    IconButton(onClick = { viewModel.signOut() }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Salir")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            if (activeOrders.isEmpty()) {
                 Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                     Text("No hay pedidos para entregar en este momento.")
                 }
            } else {
                LazyColumn {
                    items(activeOrders) { order ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Pedido para: ${order.customerName}", style = MaterialTheme.typography.titleMedium)
                                Text("Dirección: ${order.address}", style = MaterialTheme.typography.bodyMedium)
                                Text("Estado: ${order.status.name}", color = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    if (order.status == OrderStatus.PENDING || order.status == OrderStatus.PAID) {
                                        Button(onClick = { viewModel.updateOrderStatus(order.id, OrderStatus.OUT_FOR_DELIVERY) }) {
                                            Text("En Camino")
                                        }
                                    } else if (order.status == OrderStatus.OUT_FOR_DELIVERY) {
                                        Button(onClick = { viewModel.updateOrderStatus(order.id, OrderStatus.DELIVERED) }) {
                                            Text("Marcar Entregado")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
