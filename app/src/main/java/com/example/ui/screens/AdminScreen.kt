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
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush

import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.example.data.CartItem

@OptIn(ExperimentalMaterial3Api::class)

fun getProductImage(name: String, url: String): String {
    if (url.isNotEmpty()) return url
    val n = name.lowercase()
    return when {
        n.contains("plátano") || n.contains("platano") || n.contains("banana") -> "https://images.unsplash.com/photo-1481349518771-20055b2a7b24?w=500&q=80"
        n.contains("manzana") -> "https://images.unsplash.com/photo-1560806887-1e4cd0b6f447?w=500&q=80"
        n.contains("naranja") -> "https://images.unsplash.com/photo-1547514701-42782101795e?w=500&q=80"
        n.contains("fresa") -> "https://images.unsplash.com/photo-1464965911861-746a04b4bca6?w=500&q=80"
        n.contains("sandía") || n.contains("sandia") -> "https://images.unsplash.com/photo-1587049352847-4d4b12405407?w=500&q=80"
        n.contains("melón") || n.contains("melon") -> "https://images.unsplash.com/photo-1589739900243-4b52cd9b104e?w=500&q=80"
        n.contains("tomate") || n.contains("jitomate") -> "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=500&q=80"
        n.contains("cebolla") -> "https://images.unsplash.com/photo-1618512496248-a0bfe71ada8c?w=500&q=80"
        n.contains("limón") || n.contains("limon") -> "https://images.unsplash.com/photo-1590502593747-422987994667?w=500&q=80"
        n.contains("aguacate") -> "https://images.unsplash.com/photo-1523049673857-eb18f1d7b578?w=500&q=80"
        n.contains("papa") -> "https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=500&q=80"
        n.contains("huevo") -> "https://images.unsplash.com/photo-1587486913049-53fc88980cfc?w=500&q=80"
        n.contains("pollo") -> "https://images.unsplash.com/photo-1587593810167-a84920ea0781?w=500&q=80"
        n.contains("carne") || n.contains("res") || n.contains("cerdo") -> "https://images.unsplash.com/photo-1603048297172-c92544798d5e?w=500&q=80"
        else -> "https://images.unsplash.com/photo-1610832958506-aa56368176cf?w=500&q=80" // Default fresh produce
    }
}

@Composable
fun AdminScreen(viewModel: AppViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    val tabs = listOf("Inventario", "Punto de Venta", "Armar Cajas", "Flujos y Gastos", "Historial", "Usuarios")

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)) {
                // Header Casa Campo
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(Icons.Default.Eco, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(12.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Casa Campo", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("FRUTERÍA SOLIDARIA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, letterSpacing = 1.5.sp)
                            }
                        }
                    }
                    
                    var showUpdateDialog by remember { mutableStateOf(false) }
                    
                    if (showUpdateDialog) {
                        var newVersion by remember { mutableStateOf((com.example.BuildConfig.VERSION_CODE + 1).toString()) }
                        var url by remember { mutableStateOf("") }
                        
                        AlertDialog(
                            onDismissRequest = { showUpdateDialog = false },
                            title = { Text("Lanzar Actualización") },
                            text = {
                                Column {
                                    Text("Los empleados verán un bloqueo en su pantalla obligándolos a descargar esta nueva versión.")
                                    Spacer(modifier = Modifier.height(16.dp))
                                    OutlinedTextField(
                                        value = newVersion,
                                        onValueChange = { newVersion = it },
                                        label = { Text("Nuevo Version Code (actual: ${com.example.BuildConfig.VERSION_CODE})") },
                                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = url,
                                        onValueChange = { url = it },
                                        label = { Text("URL de Descarga (Link de Drive)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            },
                            confirmButton = {
                                Button(onClick = {
                                    val vc = newVersion.toIntOrNull() ?: (com.example.BuildConfig.VERSION_CODE + 1)
                                    viewModel.setAppConfig(vc, url)
                                    showUpdateDialog = false
                                }) {
                                    Text("Forzar Actualización")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showUpdateDialog = false }) { Text("Cancelar") }
                            }
                        )
                    }
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.clickable { showUpdateDialog = true }
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SystemUpdateAlt, contentDescription = "Actualizar", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
                        }
                    }
                    
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { viewModel.signOut() }
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Salir", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Salir", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                        }
                    }
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
                                    2 -> Icons.Default.CardGiftcard
                                    3 -> Icons.Default.AccountBalanceWallet
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
                2 -> ArmarCajasScreen(viewModel)
                3 -> FlujosGastosScreen(viewModel)
                4 -> HistorialScreen(viewModel)
                5 -> UsuariosScreen(viewModel)
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
                            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.background, modifier = Modifier.size(56.dp), shadowElevation = 2.dp) {
                                AsyncImage(
                                    model = getProductImage(product.name, product.imageUrl),
                                    contentDescription = product.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
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
            onSave = { name, cost, price, stock, imageUrl ->
                if (productToEdit != null) {
                    viewModel.updateProduct(productToEdit!!.copy(name = name, cost = cost, price = price, stock = stock, imageUrl = imageUrl))
                } else {
                    viewModel.addProduct(Product(name = name, cost = cost, price = price, stock = stock, imageUrl = imageUrl))
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
        shape = RoundedCornerShape(24.dp),
        color = bgColor,
        modifier = modifier.height(130.dp),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = contentColor.copy(alpha = 0.8f), letterSpacing = 1.sp)
            Column {
                Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = contentColor)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = contentColor.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
fun ProductDialog(initialProduct: Product? = null, onDismiss: () -> Unit, onSave: (String, Double, Double, Double, String) -> Unit) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var imageUrl by remember { mutableStateOf(initialProduct?.imageUrl ?: "") }
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
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("URL de la Foto (Opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
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
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("URL de la Foto (Opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
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
                        onSave(name, unitCost, autoGeneratedPrice, parsedStock, imageUrl)
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
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Products List (Top side)
        Column(modifier = Modifier.weight(1f).padding(16.dp)) {
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
                                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(56.dp), shadowElevation = 4.dp) {
                                    AsyncImage(
                                        model = getProductImage(product.name, product.imageUrl),
                                        contentDescription = product.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
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
        
        // Ticket / Cart (Bottom side)
        Surface(
            modifier = Modifier.fillMaxWidth().heightIn(min = 250.dp, max = 350.dp).padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArmarCajasScreen(viewModel: AppViewModel) {
    val products by viewModel.products.collectAsState()
    
    data class BoxOption(val price: Double, val productLimit: Int)
    val boxOptions = listOf(
        BoxOption(250.0, 10),
        BoxOption(300.0, 13),
        BoxOption(350.0, 16),
        BoxOption(400.0, 19)
    )
    
    var selectedOption by remember { mutableStateOf(boxOptions[0]) }
    val selectedProducts = remember { androidx.compose.runtime.mutableStateListOf<Product>() }
    
    val targetCost = selectedOption.price - 100.0
    
    val budgetPerProduct = if (selectedProducts.isNotEmpty()) targetCost / selectedProducts.size else 0.0
    
    val calculatedBoxItems = selectedProducts.associateWith { product ->
        if (product.cost > 0) budgetPerProduct / product.cost else 0.0
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Products List (Left side)
        Column(modifier = Modifier.weight(1.3f).padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text("Selecciona el tamaño de la caja:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                boxOptions.forEach { option ->
                    val isSelected = selectedOption == option
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.clickable {
                            selectedOption = option
                            selectedProducts.clear()
                        }
                    ) {
                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Caja $${"%.0f".format(option.price)}", fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground)
                            Text("Máx. ${option.productLimit} productos", style = MaterialTheme.typography.bodySmall, color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color.Gray)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Productos Disponibles (Haz clic para seleccionar)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn {
                items(products) { product ->
                    val isSelected = selectedProducts.contains(product)
                    val isDisabled = !isSelected && selectedProducts.size >= selectedOption.productLimit
                    
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable(enabled = !isDisabled) {
                            if (isSelected) {
                                selectedProducts.remove(product)
                            } else {
                                selectedProducts.add(product)
                            }
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(12.dp), modifier = Modifier.size(48.dp), shadowElevation = 2.dp) {
                                    AsyncImage(
                                        model = getProductImage(product.name, product.imageUrl),
                                        contentDescription = product.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                                    Text("Costo Prov: $${"%.2f".format(product.cost)}/kg", color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha=0.7f) else Color.Gray, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Seleccionado", tint = MaterialTheme.colorScheme.primary)
                            } else if (isDisabled) {
                                Icon(Icons.Default.Block, contentDescription = "Límite alcanzado", tint = Color.Gray)
                            } else {
                                Icon(Icons.Default.AddCircleOutline, contentDescription = "Agregar", tint = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
        
        // Box Configuration (Right side)
        Surface(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Armando Caja $${"%.0f".format(selectedOption.price)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Productos Seleccionados:", style = MaterialTheme.typography.bodyMedium)
                            Text("${selectedProducts.size} / ${selectedOption.productLimit}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Costo Exacto de la Caja:", style = MaterialTheme.typography.bodyMedium)
                            Text("$${"%.2f".format(if (selectedProducts.isEmpty()) 0.0 else targetCost)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Ganancia Fija:", style = MaterialTheme.typography.bodyMedium)
                            Text("$${"%.2f".format(100.0)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("Instrucciones de Armado:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                if (selectedProducts.isEmpty()) {
                    Text("Selecciona productos de la lista para ver las cantidades exactas que debes poner en la caja.", color = Color.Gray)
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(calculatedBoxItems.entries.toList()) { (product, quantity) ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(product.name, fontWeight = FontWeight.Medium)
                                    Text("Presupuesto: $${"%.2f".format(budgetPerProduct)}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(8.dp)) {
                                    Text("PONER: ${"%.2f".format(quantity)} kg", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                
                Button(
                    onClick = {
                        viewModel.boxCheckout(selectedOption.price, "Caja ${"%.0f".format(selectedOption.price)}", calculatedBoxItems)
                        selectedProducts.clear()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(32.dp),
                    enabled = selectedProducts.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Finalizar y Vender Caja", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
