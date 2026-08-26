import re

with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'r') as f:
    content = f.read()

new_inventory = """
@Composable
fun AdminInventoryScreen(viewModel: AppViewModel) {
    val products by viewModel.localProducts.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<com.example.data.local.ProductEntity?>(null) }
    
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            item {
                Text("Inventario de Productos (Local)", style = MaterialTheme.typography.titleLarge)
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
                            Text("Stock: ${product.stock}", style = MaterialTheme.typography.bodyMedium)
                            Text("Precio: $${"%.2f".format(product.price)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        Row {
                            IconButton(onClick = { productToEdit = product }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { viewModel.deleteLocalProduct(product) }) {
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
                    viewModel.updateLocalProduct(productToEdit!!.copy(name = name, price = price, stock = stock))
                } else {
                    viewModel.addLocalProduct(name, price, stock)
                }
                showAddDialog = false
                productToEdit = null
            }
        )
    }
}

@Composable
fun ProductDialog(initialProduct: com.example.data.local.ProductEntity? = null, onDismiss: () -> Unit, onSave: (String, Double, Double) -> Unit) {
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
"""

content = re.sub(r'@Composable\s*fun AdminInventoryScreen.*?\}', new_inventory, content, flags=re.DOTALL)
# It's possible the regex doesn't match if ProductDialog was already modified or there's another ProductDialog. Let's do a strict replace
with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'w') as f:
    f.write(content)
