import re

with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'r') as f:
    content = f.read()

# Replace AdminInventoryScreen
admin_inventory_old = """@Composable
fun AdminInventoryScreen(viewModel: AppViewModel) {
    val products by viewModel.products.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    
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
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(product.name, style = MaterialTheme.typography.titleMedium)
                            Text("Stock: ${product.stock} ${product.unitType} - ${product.category}", style = MaterialTheme.typography.bodyMedium)
                        }
                        Text("$${"%.2f".format(product.price)}", color = MaterialTheme.colorScheme.primary)
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
    
    if (showAddDialog) {
        AddProductDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { product ->
                viewModel.addProduct(product)
                showAddDialog = false
            }
        )
    }
}"""

admin_inventory_new = """@Composable
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
}"""

content = content.replace(admin_inventory_old, admin_inventory_new)

# Replace AddProductDialog
old_dialog_pattern = r"@Composable\nfun AddProductDialog.*?\}\)\n\s*\}\n\s*\)\n\}"
new_dialog = """@Composable
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
    }
}"""

content = re.sub(old_dialog_pattern, new_dialog, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'w') as f:
    f.write(content)
