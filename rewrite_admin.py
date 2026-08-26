import re

with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'r') as f:
    content = f.read()

# Replace product queries
content = content.replace("viewModel.localProducts", "viewModel.products")
content = content.replace("com.example.data.local.ProductEntity", "Product")
content = content.replace("viewModel.deleteLocalProduct", "viewModel.deleteProduct")

content = re.sub(r'viewModel\.updateLocalProduct\(productToEdit\!\!\.copy\(name = name, price = price, stock = stock\)\)', 
                 r'viewModel.updateProduct(productToEdit!!.copy(name = name, price = price, stock = stock))', content)

content = re.sub(r'viewModel\.addLocalProduct\(name, price, stock\)',
                 r'viewModel.addProduct(Product(name = name, price = price, stock = stock))', content)


# InventoryMovementScreen
inv_mov_tx = r'viewModel\.addTransaction\(type = type, amount = amt, description = finalDesc\)'
inv_mov_tx_repl = """if (isEntry) {
                                        viewModel.addExpense(finalDesc, amt)
                                    } else {
                                        viewModel.addDirectSale(amt, finalDesc)
                                    }"""
content = re.sub(inv_mov_tx, inv_mov_tx_repl, content)
content = content.replace("viewModel.updateLocalProduct(selectedProduct!!.copy(stock = newStock))", "viewModel.updateProduct(selectedProduct!!.copy(stock = newStock))")


# Strip everything from ReportsScreen onwards
match = re.search(r'(@Composable\s*fun ReportsScreen\(viewModel: AppViewModel\)\s*\{)', content)
if match:
    content = content[:match.start()]

reports_and_pos = """
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
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        onClick = { viewModel.addToCart(com.example.data.CartItem(product = product, quantity = 1)) }
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
"""

with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'w') as f:
    f.write(content + reports_and_pos)

