import re

with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'r') as f:
    content = f.read()

# Add imports
imports = """
import java.util.Calendar
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
"""

content = content.replace('import com.example.data.OrderStatus', 'import com.example.data.OrderStatus' + imports)

# Replace AdminSalesScreen
old_sales = """@Composable
fun AdminSalesScreen(viewModel: AppViewModel) {
    val orders by viewModel.orders.collectAsState()
    
    val totalRevenue = orders.filter { it.status != OrderStatus.CANCELLED }.sumOf { it.totalAmount }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(), 
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Ingresos Totales", style = MaterialTheme.typography.titleMedium)
                Text("$$totalRevenue", style = MaterialTheme.typography.headlineMedium)
                Text("Pedidos: ${orders.size}", style = MaterialTheme.typography.bodyMedium)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Text("Historial de Pedidos", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn {
            items(orders) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Cliente: ${order.customerName}", style = MaterialTheme.typography.titleSmall)
                            Text(order.status.name, color = MaterialTheme.colorScheme.secondary)
                        }
                        Text("Total: $${order.totalAmount}")
                        Text("Artículos: ${order.items.joinToString()}")
                    }
                }
            }
        }
    }
}"""

new_sales = """@Composable
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
}"""

content = content.replace(old_sales, new_sales)

with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'w') as f:
    f.write(content)
