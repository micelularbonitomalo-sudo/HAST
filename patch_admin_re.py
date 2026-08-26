import re

with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'r') as f:
    content = f.read()

# I will replace from @Composable fun ReportsScreen(viewModel: AppViewModel) { ... } all the way down to @Composable fun PosScreen(viewModel: AppViewModel) { ... } and the rest of the file
# Since we have the code for InventoryMovementScreen, ReportsScreen and PosScreen, let's just assemble the bottom of the file.

match = re.search(r'(@Composable\s*fun ReportsScreen\(viewModel: AppViewModel\)\s*\{)', content)
if match:
    # Truncate content right before ReportsScreen
    content = content[:match.start()]

reports_and_pos = """
@Composable
fun ReportsScreen(viewModel: AppViewModel) {
    val totalRevenue by viewModel.totalIncome.collectAsState()
    val totalExpenses by viewModel.totalExpenses.collectAsState()
    val transactions by viewModel.localTransactions.collectAsState()
    
    val rev = totalRevenue ?: 0.0
    val exp = totalExpenses ?: 0.0
    val netProfit = rev - exp
    
    var showExpenseDialog by remember { mutableStateOf(false) }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(), 
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Balance General (Local)", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Ingresos Totales:", style = MaterialTheme.typography.bodyLarge)
                    Text("$${"%.2f".format(rev)}", style = MaterialTheme.typography.bodyLarge, color = androidx.compose.ui.graphics.Color(0xFF4CAF50))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Egresos Totales:", style = MaterialTheme.typography.bodyLarge)
                    Text("$${"%.2f".format(exp)}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
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
        Text("Historial de Movimientos", style = MaterialTheme.typography.titleMedium)
        
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(transactions) { tx ->
                val isIncome = tx.type == "INGRESO"
                val color = if (isIncome) androidx.compose.ui.graphics.Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                val sign = if (isIncome) "+" else "-"
                
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text(tx.description, style = MaterialTheme.typography.bodyLarge)
                            Text(tx.type, style = MaterialTheme.typography.bodySmall, color = androidx.compose.ui.graphics.Color.Gray)
                        }
                        Text("$sign$${"%.2f".format(tx.amount)}", color = color, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    }
                }
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
                        viewModel.addTransaction(type = "EGRESO", amount = amt, description = description)
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
    val products by viewModel.localProducts.collectAsState()
    val cart by viewModel.localCart.collectAsState()
    
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
                        onClick = { viewModel.addToLocalCart(product) }
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
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${item.quantity}x ${item.product.name}")
                        Text("$${"%.2f".format(item.product.price * item.quantity)}")
                    }
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            val total = cart.sumOf { it.product.price * it.quantity }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total:", style = MaterialTheme.typography.headlineSmall)
                Text("$${"%.2f".format(total)}", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.localPosCheckout() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = cart.isNotEmpty()
            ) {
                Text("Cobrar", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { viewModel.clearLocalCart() },
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
