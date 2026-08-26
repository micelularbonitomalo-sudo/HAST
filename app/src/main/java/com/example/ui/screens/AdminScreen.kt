package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import com.example.viewmodel.AppViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import com.example.data.Product
import com.example.data.Order
import com.example.data.OrderStatus
import java.util.Calendar
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.example.data.UserRole
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.example.data.CartItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(viewModel: AppViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    val tabs = listOf("Inventario", "Punto de Venta", "Flujos y Gastos", "Historial", "Usuarios")

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)) {
                // Header Casa Campo
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Default.Eco, contentDescription = null, tint = Color.White, modifier = Modifier.padding(12.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Casa Campo", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                                Spacer(modifier = Modifier.width(12.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.secondary,
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text("FRUTERÍA SOLIDARIA", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                                }
                            }
                            Text("DEL CAMPO A TU MESA · PRECIOS JUSTOS", style = MaterialTheme.typography.bodySmall, color = Color.Gray, letterSpacing = 1.sp)
                        }
                    }
                    IconButton(onClick = { viewModel.signOut() }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Salir", tint = MaterialTheme.colorScheme.onBackground)
                    }
                }
                
                // Chip Navigation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.surface,
                            border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.clickable { selectedTab = index }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val icon = when(index) {
                                    0 -> Icons.Default.Inventory2
                                    1 -> Icons.Default.PointOfSale
                                    2 -> Icons.Default.AccountBalanceWallet
                                    else -> Icons.Default.ReceiptLong
                                }
                                Icon(icon, contentDescription = null, tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = title,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (isSelected) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(modifier = Modifier.size(6.dp).background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(3.dp)))
                                }
                            }
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp, modifier = Modifier.padding(top = 8.dp))
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            when (selectedTab) {
                0 -> AdminInventoryScreen(viewModel)
                1 -> PosScreen(viewModel)
                2 -> FlujosGastosScreen(viewModel)
                3 -> HistorialScreen(viewModel)
                4 -> UsuariosScreen(viewModel)
            }
        }
    }
}

@Composable
fun AdminInventoryScreen(viewModel: AppViewModel) {
    val products by viewModel.products.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    
    var searchQuery by remember { mutableStateOf("") }
    val filteredProducts = products.filter { it.name.contains(searchQuery, ignoreCase = true) }
    
    val totalCosto = products.sumOf { it.cost * it.stock }
    val totalVenta = products.sumOf { it.price * it.stock }
    val totalMerma = totalVenta * 0.15
    val totalOperativo = totalVenta * 0.20
    val totalGanancia = totalCosto * 0.30

    LazyColumn(contentPadding = PaddingValues(16.dp), modifier = Modifier.fillMaxSize()) {
        item {
            // Dashboard Cards
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardCard("PRODUCTOS", "${products.size}", "activos", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardCard("COSTO INVENTARIO", "$${"%.2f".format(totalCosto)}", "invertido", MaterialTheme.colorScheme.primary, Color.White, modifier = Modifier.weight(1f))
                DashboardCard("VENTA PROYECTADA", "$${"%.2f".format(totalVenta)}", "si todo se vende", MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardCard("FONDO MERMA", "$${"%.2f".format(totalMerma)}", "15 % teórico", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.weight(1f))
                DashboardCard("FONDO OPERATIVO", "$${"%.2f".format(totalOperativo)}", "20 % teórico", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DashboardCard("GANANCIA ESTIMADA", "$${"%.2f".format(totalGanancia)}", "30 % sobre costo", MaterialTheme.colorScheme.onBackground, Color.White, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            // Add Product Button
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().clickable { showAddDialog = true }
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onBackground)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Agregar / Resurtir", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            // Search
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Productos\ndisponibles", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(16.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Buscar fruta...") },
                        modifier = Modifier.weight(1f),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = RoundedCornerShape(32.dp),
                        singleLine = true
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        items(filteredProducts) { product ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.background, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Default.LocalMall, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(8.dp))
                            }
                            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                                Text("${product.stock} ${product.unitType}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(product.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
                        Text("$${"%.2f".format(product.cost)} costo prom", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("$${"%.2f".format(product.price)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                            
                            Row {
                                IconButton(onClick = { productToEdit = product }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.onBackground)
                                }
                                IconButton(onClick = { viewModel.deleteProduct(product.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (showAddDialog || productToEdit != null) {
        ProductDialog(
            initialProduct = productToEdit,
            onDismiss = { 
                showAddDialog = false
                productToEdit = null
            },
            onSave = { name, cost, price, stock ->
                if (productToEdit != null) {
                    viewModel.updateProduct(productToEdit!!.copy(name = name, cost = cost, price = price, stock = stock))
                } else {
                    viewModel.addProduct(Product(name = name, cost = cost, price = price, stock = stock))
                }
                showAddDialog = false
                productToEdit = null
            }
        )
    }
}

@Composable
fun DashboardCard(title: String, value: String, subtitle: String, bgColor: Color, contentColor: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        modifier = modifier.height(120.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = contentColor.copy(alpha = 0.8f))
            Column {
                Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = contentColor)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = contentColor.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
fun ProductDialog(initialProduct: Product? = null, onDismiss: () -> Unit, onSave: (String, Double, Double, Double) -> Unit) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var stockStr by remember { mutableStateOf(if (initialProduct?.stock != null && initialProduct.stock > 0) initialProduct.stock.toString() else "") }
    
    val initialTotalPaid = if (initialProduct != null) initialProduct.stock * initialProduct.cost else 0.0
    var totalPaidStr by remember { mutableStateOf(if (initialTotalPaid > 0) "%.2f".format(initialTotalPaid).replace(",", ".") else "") }
    
    val parsedStock = stockStr.toDoubleOrNull() ?: 0.0
    val parsedTotalPaid = totalPaidStr.toDoubleOrNull() ?: 0.0
    
    val unitCost = if (parsedStock > 0) parsedTotalPaid / parsedStock else 0.0
    val autoGeneratedPrice = unitCost + (unitCost * 0.30) + (unitCost * 0.15) + (unitCost * 0.20)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialProduct == null) "Agregar / Resurtir Producto" else "Editar Producto", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del Producto") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = stockStr,
                    onValueChange = { stockStr = it },
                    label = { Text("¿Cuántos kilos o piezas son?") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = totalPaidStr,
                    onValueChange = { totalPaidStr = it },
                    label = { Text("¿Cuánto pagaste al proveedor? ($)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Matemática Automática", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Costo Unitario:")
                            Text("$${"%.2f".format(unitCost)}", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Precio de Venta:")
                            Text("$${"%.2f".format(autoGeneratedPrice)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Incluye: 30% Ganancia, 15% Merma, 20% Operativo", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && parsedStock > 0 && parsedTotalPaid > 0) {
                        onSave(name, unitCost, autoGeneratedPrice, parsedStock)
                    }
                },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Guardar Cambios")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = MaterialTheme.colorScheme.onBackground)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun PosScreen(viewModel: AppViewModel) {
    val products by viewModel.products.collectAsState()
    val cart by viewModel.cart.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    val filteredProducts = products.filter { it.name.contains(searchQuery, ignoreCase = true) }
    
    Row(modifier = Modifier.fillMaxSize()) {
        // Products List (Left side)
        Column(modifier = Modifier.weight(1.5f).padding(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar Producto para cobrar") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(32.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                items(filteredProducts) { product ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable {
                            viewModel.addToCart(CartItem(product = product, quantity = 1))
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(48.dp)) {
                                    Icon(Icons.Default.LocalMall, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(12.dp))
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("Stock: ${product.stock}", color = if (product.stock > 0) Color.Gray else MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            Text("$${"%.2f".format(product.price)}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        
        // Ticket / Cart (Right side)
        Column(modifier = Modifier.weight(1f).padding(16.dp).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp)).padding(24.dp)) {
            Text("Ticket Actual", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(cart) { item ->
                    if (item.product != null) {
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(item.product.name, fontWeight = FontWeight.Medium)
                                Text("${item.quantity}x $${"%.2f".format(item.product.price)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            Text("$${"%.2f".format(item.product.price * item.quantity)}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
            val total = cart.sumOf { (it.product?.price ?: 0.0) * it.quantity }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Total a Cobrar:", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                Text("$${"%.2f".format(total)}", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { viewModel.posCheckout() },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(32.dp),
                enabled = cart.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Cobrar Ticket", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { viewModel.clearCart() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(32.dp),
                enabled = cart.isNotEmpty(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text("Limpiar Ticket", color = MaterialTheme.colorScheme.onBackground)
            }
        }
    }
}

@Composable
fun FlujosGastosScreen(viewModel: AppViewModel) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Agregar gasto operativo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = "", onValueChange = {}, label = { Text("Concepto (Ej: Renta, Luz)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = "", onValueChange = {}, label = { Text("Monto $") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = "26/08/2026", onValueChange = {}, label = { Text("Fecha") }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { /* TODO */ },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Ahorrar gasto", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun HistorialScreen(viewModel: AppViewModel) {
    val orders by viewModel.orders.collectAsState()
    val totalVendido = orders.sumOf { it.totalAmount }
    
    // Generar datos para la gráfica (últimos 7 "días" o tickets)
    // Para simplificar, mostraremos los últimos 7 tickets
    val recentOrders = orders.sortedBy { it.timestamp }.takeLast(7)
    val chartEntries = if (recentOrders.isNotEmpty()) {
        recentOrders.mapIndexed { index, order -> 
            com.patrykandpatrick.vico.core.entry.FloatEntry(x = index.toFloat(), y = order.totalAmount.toFloat())
        }
    } else {
        listOf(
            com.patrykandpatrick.vico.core.entry.FloatEntry(0f, 0f),
            com.patrykandpatrick.vico.core.entry.FloatEntry(1f, 100f),
            com.patrykandpatrick.vico.core.entry.FloatEntry(2f, 50f),
            com.patrykandpatrick.vico.core.entry.FloatEntry(3f, 200f)
        )
    }
    
    val chartEntryModel = com.patrykandpatrick.vico.core.entry.entryModelOf(chartEntries)

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Historial de ventas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(16.dp)) {
                                Text("Total vendido $${"%.2f".format(totalVendido)}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text("Ventas Recientes", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Chart(
                            chart = columnChart(),
                            model = chartEntryModel,
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis(),
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        if (orders.isEmpty()) {
                            Text("Aún no hay ventas. Ve a Punto de Venta.", style = MaterialTheme.typography.bodyLarge, color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally).padding(32.dp))
                        }
                    }
                }
            }
            
            items(orders.sortedByDescending { it.timestamp }) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Ticket #${order.id.takeLast(6).uppercase()}", fontWeight = FontWeight.Bold)
                            Text(order.items.joinToString(", "), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Text("$${"%.2f".format(order.totalAmount)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun UsuariosScreen(viewModel: AppViewModel) {
    val users by viewModel.allUsers.collectAsState()
    var emailInput by remember { mutableStateOf("") }
    
    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                        Text("Pre-autorizar Nuevo Usuario", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Correo electrónico") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (emailInput.isNotBlank()) {
                                    viewModel.preRegisterStaff(emailInput.trim(), com.example.data.UserRole.ADMIN)
                                    emailInput = ""
                                }
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Autorizar Correo")
                        }
                    }
                }
            }
            
            item {
                Text(
                    "Usuarios Registrados", 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            items(users) { user ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(user.name, fontWeight = FontWeight.Bold)
                            Text(user.email.ifEmpty { "Sin correo" }, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = if (user.role == com.example.data.UserRole.ADMIN) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    user.role.name, 
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (user.role == com.example.data.UserRole.ADMIN) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                        
                        if (user.role != com.example.data.UserRole.ADMIN) {
                            Button(onClick = { viewModel.changeUserRole(user.uid, com.example.data.UserRole.ADMIN) }) {
                                Text("Autorizar")
                            }
                        } else {
                            OutlinedButton(onClick = { viewModel.changeUserRole(user.uid, com.example.data.UserRole.CUSTOMER) }) {
                                Text("Revocar")
                            }
                        }
                    }
                }
            }
        }
    }
}
