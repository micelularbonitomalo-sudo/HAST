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
                title = { Text("Administrador - Casa Campo") },
                actions = {
                    IconButton(onClick = { viewModel.signOut() }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Salir")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Inventory, contentDescription = "Inventario") },
                    label = { Text("Inventario") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Ventas") },
                    label = { Text("Ventas") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.People, contentDescription = "Personal") },
                    label = { Text("Personal") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                0 -> AdminInventoryScreen(viewModel)
                1 -> AdminSalesScreen(viewModel)
                2 -> AdminUsersScreen(viewModel)
            }
        }
    }
}

@Composable
fun AdminInventoryScreen(viewModel: AppViewModel) {
    val products by viewModel.products.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            item {
                Text("Inventario de Productos", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
            }
            items(products) { product ->
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
                            Text("Stock: ${product.stock} ${product.unitType} - ${product.category}", style = MaterialTheme.typography.bodyMedium)
                            Text("Costo: $${"%.2f".format(product.cost)} | Precio: $${"%.2f".format(product.price)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
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
            onSave = { product ->
                if (productToEdit != null) {
                    viewModel.updateProduct(product.copy(id = productToEdit!!.id))
                } else {
                    viewModel.addProduct(product)
                }
                showAddDialog = false
                productToEdit = null
            }
        )
    }
}

@Composable
fun ProductDialog(initialProduct: Product? = null, onDismiss: () -> Unit, onSave: (Product) -> Unit) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var totalPaid by remember { mutableStateOf(if (initialProduct != null) initialProduct.totalPaidToSupplier.toString() else "") }
    var quantity by remember { mutableStateOf(if (initialProduct != null) initialProduct.quantityReceived.toString() else "") }
    var unitType by remember { mutableStateOf(initialProduct?.unitType ?: "kg") }
    var profitMargin by remember { mutableStateOf(if (initialProduct != null) initialProduct.profitMarginPercent.toString() else "30") }
    var solidarityMargin by remember { mutableStateOf(if (initialProduct != null) initialProduct.solidarityMarginPercent.toString() else "10") }
    var category by remember { mutableStateOf(initialProduct?.category ?: "frutas") }
    
    var expandedUnit by remember { mutableStateOf(false) }
    var expandedCategory by remember { mutableStateOf(false) }
    
    val currentTotalPaid = totalPaid.toDoubleOrNull() ?: 0.0
    val currentQuantity = quantity.toDoubleOrNull() ?: 0.0
    val currentProfitMargin = profitMargin.toDoubleOrNull() ?: 0.0
    val currentSolidarityMargin = solidarityMargin.toDoubleOrNull() ?: 0.0
    
    val unitCost = if (currentQuantity > 0) currentTotalPaid / currentQuantity else 0.0
    val exactFinalPrice = unitCost * (1 + (currentProfitMargin / 100.0) + (currentSolidarityMargin / 100.0))
    val finalPrice = kotlin.math.ceil(exactFinalPrice)
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialProduct == null) "Nuevo Producto" else "Editar Producto") },
        text = {
            LazyColumn {
                item {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre del Producto") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(onClick = { expandedUnit = true }, modifier = Modifier.fillMaxWidth()) {
                                Text("Unidad: $unitType")
                            }
                            DropdownMenu(expanded = expandedUnit, onDismissRequest = { expandedUnit = false }) {
                                DropdownMenuItem(text = { Text("kg") }, onClick = { unitType = "kg"; expandedUnit = false })
                                DropdownMenuItem(text = { Text("pieza") }, onClick = { unitType = "pieza"; expandedUnit = false })
                            }
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(onClick = { expandedCategory = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(category)
                            }
                            DropdownMenu(expanded = expandedCategory, onDismissRequest = { expandedCategory = false }) {
                                listOf("frutas", "verduras", "avicolas", "ovinos", "porcinos").forEach { cat ->
                                    DropdownMenuItem(text = { Text(cat) }, onClick = { category = cat; expandedCategory = false })
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Detalles de Compra (Proveedor)", style = MaterialTheme.typography.titleSmall)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = totalPaid, onValueChange = { totalPaid = it }, label = { Text("Monto Pagado ($)") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Cantidad Recibida") }, modifier = Modifier.weight(1f))
                    }
                    if (unitCost > 0) {
                        Text("Costo Unitario: $${"%.2f".format(unitCost)} por $unitType", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Márgenes", style = MaterialTheme.typography.titleSmall)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = profitMargin, onValueChange = { profitMargin = it }, label = { Text("Ganancia (%)") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = solidarityMargin, onValueChange = { solidarityMargin = it }, label = { Text("Merma/Ops (%)") }, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                            Text("Precio al Público", style = MaterialTheme.typography.titleMedium)
                            Text("$${"%.2f".format(finalPrice)} por $unitType", style = MaterialTheme.typography.headlineSmall, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(Product(
                    name = name, 
                    price = finalPrice, 
                    cost = unitCost, 
                    stock = currentQuantity, 
                    category = category,
                    unitType = unitType,
                    totalPaidToSupplier = currentTotalPaid,
                    quantityReceived = currentQuantity,
                    profitMarginPercent = currentProfitMargin,
                    solidarityMarginPercent = currentSolidarityMargin
                ))
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
fun AdminSalesScreen(viewModel: AppViewModel) {
    val orders by viewModel.orders.collectAsState()
    
    val activeOrders = orders.filter { it.status != OrderStatus.CANCELLED }
    val totalRevenue = activeOrders.sumOf { it.totalAmount }
    
    // Process Daily Sales (Last 7 Days)
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val todayMillis = calendar.timeInMillis
    val dayMillis = 24 * 60 * 60 * 1000L
    
    val dailyTotals = FloatArray(7) { 0f }
    activeOrders.forEach { order ->
        val daysAgo = ((todayMillis + dayMillis - 1 - order.timestamp) / dayMillis).toInt()
        if (daysAgo in 0..6) {
            dailyTotals[6 - daysAgo] += order.totalAmount.toFloat()
        }
    }
    
    // Process Monthly Sales (Last 6 Months)
    val monthlyTotals = FloatArray(6) { 0f }
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)
    activeOrders.forEach { order ->
        val cal = Calendar.getInstance()
        cal.timeInMillis = order.timestamp
        val month = cal.get(Calendar.MONTH)
        val year = cal.get(Calendar.YEAR)
        
        val monthsAgo = (currentYear - year) * 12 + (currentMonth - month)
        if (monthsAgo in 0..5) {
            monthlyTotals[5 - monthsAgo] += order.totalAmount.toFloat()
        }
    }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(), 
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Ingresos Totales", style = MaterialTheme.typography.titleMedium)
                Text("$${"%.2f".format(totalRevenue)}", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                Text("Pedidos Exitosos: ${activeOrders.size}", style = MaterialTheme.typography.bodyMedium)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            item {
                Text("Ventas Últimos 7 Días", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Box(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                        if (dailyTotals.any { it > 0 }) {
                            Chart(
                                chart = columnChart(),
                                model = entryModelOf(*dailyTotals.toTypedArray()),
                                startAxis = rememberStartAxis(),
                                bottomAxis = rememberBottomAxis()
                            )
                        } else {
                            Text("No hay ventas en los últimos 7 días", modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Ventas Últimos 6 Meses", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Box(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                        if (monthlyTotals.any { it > 0 }) {
                            Chart(
                                chart = columnChart(),
                                model = entryModelOf(*monthlyTotals.toTypedArray()),
                                startAxis = rememberStartAxis(),
                                bottomAxis = rememberBottomAxis()
                            )
                        } else {
                            Text("No hay ventas en los últimos 6 meses", modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Historial de Pedidos", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            items(orders) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(order.customerName, style = MaterialTheme.typography.titleSmall)
                            Text(order.status.name, color = MaterialTheme.colorScheme.secondary, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Total: $${"%.2f".format(order.totalAmount)}", color = MaterialTheme.colorScheme.primary)
                        Text(order.items.joinToString(), style = MaterialTheme.typography.bodySmall, color = androidx.compose.ui.graphics.Color.Gray)
                    }
                }
            }
        }
    }
}
@Composable
fun AdminUsersScreen(viewModel: AppViewModel) {
    val users by viewModel.allUsers.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var showAddStaffDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Gestión de Personal", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn {
                items(users) { user ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(user.name, style = MaterialTheme.typography.titleMedium)
                            Text(user.email.ifBlank { "Sin email (Teléfono)" }, style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Text("Rol: ${user.role.name}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                
                                // Prevent the master admin from demoting themselves
                                if (user.uid != currentUser?.uid) {
                                    var expanded by remember { mutableStateOf(false) }
                                    Box {
                                        OutlinedButton(onClick = { expanded = true }) {
                                            Text("Cambiar Rol")
                                        }
                                        DropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Administrador") },
                                                onClick = {
                                                    viewModel.changeUserRole(user.uid, UserRole.ADMIN)
                                                    expanded = false
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Staff (Repartidor)") },
                                                onClick = {
                                                    viewModel.changeUserRole(user.uid, UserRole.STAFF)
                                                    expanded = false
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Cliente") },
                                                onClick = {
                                                    viewModel.changeUserRole(user.uid, UserRole.CUSTOMER)
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                } else {
                                    Text("(Tú)", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
        
        FloatingActionButton(
            onClick = { showAddStaffDialog = true },
            modifier = Modifier.align(androidx.compose.ui.Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = "Agregar Personal")
        }
    }
    
    if (showAddStaffDialog) {
        AddStaffDialog(
            onDismiss = { showAddStaffDialog = false },
            onAdd = { phone, role ->
                viewModel.preRegisterStaff(phone, role)
                showAddStaffDialog = false
            }
        )
    }
}

@Composable
fun AddStaffDialog(onDismiss: () -> Unit, onAdd: (String, UserRole) -> Unit) {
    var phone by remember { mutableStateOf("+52") }
    var selectedRole by remember { mutableStateOf(UserRole.STAFF) }
    var expanded by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pre-registrar Personal") },
        text = {
            Column {
                Text("Cuando este número inicie sesión, se le asignará el rol automáticamente.", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Teléfono (+52...)") })
                Spacer(modifier = Modifier.height(16.dp))
                Text("Rol:")
                Box {
                    OutlinedButton(onClick = { expanded = true }) {
                        Text(selectedRole.name)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text("Administrador") },
                            onClick = { selectedRole = UserRole.ADMIN; expanded = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Staff (Repartidor)") },
                            onClick = { selectedRole = UserRole.STAFF; expanded = false }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onAdd(phone, selectedRole)
            }) {
                Text("Pre-registrar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )

}
