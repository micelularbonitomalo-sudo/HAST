import re

with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'r') as f:
    content = f.read()

# 1. Add the 4th tab to NavigationBar
nav_item_3 = """                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.SyncAlt, contentDescription = "Movimientos") },
                    label = { Text("Movimientos") }
                )"""

if "selectedTab == 3" not in content:
    content = content.replace('label = { Text("Reportes") }\n                )', 'label = { Text("Reportes") }\n                )\n' + nav_item_3)

# 2. Add to `when (selectedTab)`
when_tab_3 = "                3 -> InventoryMovementScreen(viewModel)"
if "3 -> InventoryMovementScreen" not in content:
    content = content.replace('2 -> ReportsScreen(viewModel)', '2 -> ReportsScreen(viewModel)\n' + when_tab_3)

# 3. Add the actual composable
movement_screen = """
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryMovementScreen(viewModel: AppViewModel) {
    val products by viewModel.localProducts.collectAsState()
    
    var expanded by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<com.example.data.local.ProductEntity?>(null) }
    
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
                                    viewModel.updateLocalProduct(selectedProduct!!.copy(stock = newStock))
                                    
                                    // Register Transaction
                                    val type = if (isEntry) "EGRESO" else "INGRESO"
                                    val defaultDesc = if (isEntry) "Compra de ${selectedProduct!!.name}" else "Venta/Salida de ${selectedProduct!!.name}"
                                    val finalDesc = if (description.isNotBlank()) description else defaultDesc
                                    
                                    viewModel.addTransaction(type = type, amount = amt, description = finalDesc)
                                    
                                    // Reset fields
                                    quantityStr = ""
                                    amountStr = ""
                                    description = ""
                                    selectedProduct = null
                                    
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Movimiento registrado exitosamente")
                                    }
                                } else {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Por favor verifique los datos ingresados")
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
"""

if "fun InventoryMovementScreen" not in content:
    content = content + "\n" + movement_screen

# Add missing import for Icons.Default.SyncAlt if missing
if "Icons.Default.SyncAlt" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.*", "import androidx.compose.material.icons.filled.*\nimport androidx.compose.material.icons.filled.SyncAlt")

with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'w') as f:
    f.write(content)
