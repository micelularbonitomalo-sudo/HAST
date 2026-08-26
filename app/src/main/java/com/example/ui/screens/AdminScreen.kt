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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(viewModel: AppViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Punto de Venta - Casa Campo") },
                actions = {
                    IconButton(onClick = { viewModel.signOut() }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Salir")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.PointOfSale, contentDescription = "Punto de Venta") },
                    label = { Text("POS") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Inventory, contentDescription = "Inventario") },
                    label = { Text("Inventario") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Reportes") },
                    label = { Text("Reportes") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.SyncAlt, contentDescription = "Movimientos") },
                    label = { Text("Movimientos") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                0 -> PosScreen(viewModel)
                1 -> AdminInventoryScreen(viewModel)
                2 -> ReportsScreen(viewModel)
                3 -> InventoryMovementScreen(viewModel)
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
    
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            item {
                Text("Inventario de Productos (Local)", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Buscar Producto") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            items(filteredProducts) { product ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(product.name, style = MaterialTheme.typography.titleMedium)
                            Text("Stock: ${product.stock}", style = MaterialTheme.typography.bodyMedium)
                            Text("Precio: $${"%.2f".format(product.price)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        Row {
                            IconButton(onClick = { productToEdit = product }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { viewModel.deleteProduct(product.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
        
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.align(androidx.compose.ui.Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Agregar Producto")
        }
    }
    
    if (showAddDialog || productToEdit != null) {
        ProductDialog(
            initialProduct = productToEdit,
            onDismiss = { 
                showAddDialog = false
                productToEdit = null
            },
            onSave = { name, price, stock ->
                if (productToEdit != null) {
                    viewModel.updateProduct(productToEdit!!.copy(name = name, price = price, stock = stock))
                } else {
                    viewModel.addProduct(Product(name = name, price = price, stock = stock))
                }
                showAddDialog = false
                productToEdit = null
            }
        )
    }
}

@Composable
fun ProductDialog(initialProduct: Product? = null, onDismiss: () -> Unit, onSave: (String, Double, Double) -> Unit) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var priceStr by remember { mutableStateOf(initialProduct?.price?.toString() ?: "") }
    var stockStr by remember { mutableStateOf(initialProduct?.stock?.toString() ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialProduct == null) "Nuevo Producto" else "Editar Producto") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { priceStr = it },
                    label = { Text("Precio al Público ($)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = stockStr,
                    onValueChange = { stockStr = it },
                    label = { Text("Stock") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val price = priceStr.toDoubleOrNull() ?: 0.0
                val stock = stockStr.toDoubleOrNull() ?: 0.0
                if (name.isNotBlank()) {
                    onSave(name, price, stock)
                }
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}




@Composable
fun ReportsScreen(viewModel: AppViewModel) {
    val orders by viewModel.orders.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    
    val activeOrders = orders.filter { it.status != OrderStatus.CANCELLED }
    val totalRevenue = activeOrders.sumOf { it.totalAmount }
    val totalExpenses = expenses.sumOf { it.amount }
    val netProfit = totalRevenue - totalExpenses
    
    var showExpenseDialog by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(), 
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Balance General (Nube)", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Ingresos Totales:", style = MaterialTheme.typography.bodyLarge)
                    Text("$${"%.2f".format(totalRevenue)}", style = MaterialTheme.typography.bodyLarge, color = androidx.compose.ui.graphics.Color(0xFF4CAF50))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Egresos Totales:", style = MaterialTheme.typography.bodyLarge)
                    Text("$${"%.2f".format(totalExpenses)}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Ganancia Neta:", style = MaterialTheme.typography.titleMedium)
                    Text("$${"%.2f".format(netProfit)}", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { showExpenseDialog = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Registrar Gasto Operativo")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Historial de Movimientos (Nube)", style = MaterialTheme.typography.titleMedium)
        
        val allTx = mutableListOf<Pair<Long, @Composable () -> Unit>>()
        
        activeOrders.forEach { order ->
            allTx.add(order.timestamp to {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("Venta: ${order.items.joinToString(", ")}", style = MaterialTheme.typography.bodyLarge)
                            Text("INGRESO", style = MaterialTheme.typography.bodySmall, color = androidx.compose.ui.graphics.Color.Gray)
                        }
                        Text("+$${"%.2f".format(order.totalAmount)}", color = androidx.compose.ui.graphics.Color(0xFF4CAF50), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    }
                }
            })
        }
        
        expenses.forEach { expense ->
            allTx.add(expense.timestamp to {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(expense.description, style = MaterialTheme.typography.bodyLarge)
                            Text("EGRESO", style = MaterialTheme.typography.bodySmall, color = androidx.compose.ui.graphics.Color.Gray)
                        }
                        Text("-$${"%.2f".format(expense.amount)}", color = MaterialTheme.colorScheme.error, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    }
                }
            })
        }
        
        val sortedTx = allTx.sortedByDescending { it.first }
        
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(sortedTx.size) { idx ->
                sortedTx[idx].second()
            }
        }
    }
    
    if (showExpenseDialog) {
        var description by remember { mutableStateOf("") }
        var amount by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showExpenseDialog = false },
            title = { Text("Registrar Gasto") },
            text = {
                Column {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripción del gasto") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Monto ($)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    if (description.isNotBlank() && amt > 0) {
                        viewModel.addExpense(description, amt)
                        showExpenseDialog = false
                    }
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { showExpenseDialog = false }) { Text("Cancelar") }
            }
        )
    }
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
                label = { Text("Buscar Producto") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                items(filteredProducts) { product ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable {
                            viewModel.addToCart(com.example.data.CartItem(product = product, quantity = 1))
                        }
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(product.name, style = MaterialTheme.typography.titleMedium)
                                Text("Stock: ${product.stock}", color = if (product.stock > 0) androidx.compose.ui.graphics.Color.Gray else MaterialTheme.colorScheme.error)
                            }
                            Text("$${"%.2f".format(product.price)}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
        
        // Ticket / Cart (Right side)
        Column(modifier = Modifier.weight(1f).padding(16.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)).padding(16.dp)) {
            Text("Ticket Actual", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(cart) { item ->
                    if (item.product != null) {
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${item.quantity}x ${item.product.name}")
                            Text("$${"%.2f".format(item.product.price * item.quantity)}")
                        }
                    }
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            val total = cart.sumOf { (it.product?.price ?: 0.0) * it.quantity }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total:", style = MaterialTheme.typography.headlineSmall)
                Text("$${"%.2f".format(total)}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.posCheckout() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = cart.isNotEmpty()
            ) {
                Text("Cobrar", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { viewModel.clearCart() },
                modifier = Modifier.fillMaxWidth(),
                enabled = cart.isNotEmpty()
            ) {
                Text("Limpiar Ticket")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryMovementScreen(viewModel: AppViewModel) {
    val products by viewModel.products.collectAsState()
    
    var expanded by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    
    var isEntry by remember { mutableStateOf(true) } // true = Entrada, false = Salida
    var quantityStr by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Registrar Movimiento de Inventario", style = MaterialTheme.typography.titleLarge)
            }
            
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        
                        // Selector de tipo
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                RadioButton(selected = isEntry, onClick = { isEntry = true })
                                Text("Entrada (Compra)", modifier = Modifier.padding(start = 4.dp))
                            }
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                RadioButton(selected = !isEntry, onClick = { isEntry = false })
                                Text("Salida (Venta/Merma)", modifier = Modifier.padding(start = 4.dp))
                            }
                        }
                        
                        // Selector de producto
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = selectedProduct?.name ?: "Seleccione un producto",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                label = { Text("Producto") }
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                products.forEach { product ->
                                    DropdownMenuItem(
                                        text = { Text("${product.name} (Stock: ${product.stock})") },
                                        onClick = {
                                            selectedProduct = product
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        // Inputs
                        OutlinedTextField(
                            value = quantityStr,
                            onValueChange = { quantityStr = it },
                            label = { Text("Cantidad a ${if (isEntry) "sumar" else "restar"} al stock") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        OutlinedTextField(
                            value = amountStr,
                            onValueChange = { amountStr = it },
                            label = { Text(if (isEntry) "Monto total pagado (Costo - Egreso)" else "Monto total cobrado (Ingreso)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Descripción (Opcional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Button(
                            onClick = {
                                val qty = quantityStr.toDoubleOrNull() ?: 0.0
                                val amt = amountStr.toDoubleOrNull() ?: 0.0
                                
                                if (selectedProduct != null && qty > 0 && amt >= 0) {
                                    // Update Stock
                                    val currentStock = selectedProduct!!.stock
                                    val newStock = if (isEntry) currentStock + qty else currentStock - qty
                                    viewModel.updateProduct(selectedProduct!!.copy(stock = newStock))
                                    
                                    // Register Transaction
                                    val defaultDesc = if (isEntry) "Compra de ${selectedProduct!!.name}" else "Venta/Salida de ${selectedProduct!!.name}"
                                    val finalDesc = if (description.isNotBlank()) description else defaultDesc
                                    
                                    if (isEntry) {
                                        viewModel.addExpense(finalDesc, amt)
                                    } else {
                                        viewModel.addDirectSale(amt, finalDesc)
                                    }
                                    
                                    // Reset fields
                                    quantityStr = ""
                                    amountStr = ""
                                    description = ""
                                    selectedProduct = null
                                    
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Movimiento registrado en la nube")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Text("Guardar Movimiento")
                        }
                    }
                }
            }
        }
    }
}
