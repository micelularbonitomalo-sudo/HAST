package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.example.viewmodel.AppViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.data.CartItem
import com.example.data.defaultBaskets
import com.example.data.Product
import com.example.data.BasketOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerScreen(viewModel: AppViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Casa Campo", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF7F4EB),
                    titleContentColor = Color(0xFF3E2723),
                    actionIconContentColor = Color(0xFF8B5A2B)
                ),
                actions = {
                    IconButton(onClick = { viewModel.signOut() }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Salir")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White, contentColor = Color(0xFF8B5A2B)) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.ShoppingBasket, contentDescription = "Canastas") },
                    label = { Text("Canastas") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFF7F4EB),
                        selectedTextColor = Color(0xFF8B5A2B),
                        indicatorColor = Color(0xFF8B5A2B),
                        unselectedIconColor = Color(0xFF795548),
                        unselectedTextColor = Color(0xFF795548)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Storefront, contentDescription = "Productos") },
                    label = { Text("Productos") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFF7F4EB),
                        selectedTextColor = Color(0xFF8B5A2B),
                        indicatorColor = Color(0xFF8B5A2B),
                        unselectedIconColor = Color(0xFF795548),
                        unselectedTextColor = Color(0xFF795548)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito") },
                    label = { Text("Carrito") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFF7F4EB),
                        selectedTextColor = Color(0xFF8B5A2B),
                        indicatorColor = Color(0xFF8B5A2B),
                        unselectedIconColor = Color(0xFF795548),
                        unselectedTextColor = Color(0xFF795548)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.History, contentDescription = "Historial") },
                    label = { Text("Mis Pedidos") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFFF7F4EB),
                        selectedTextColor = Color(0xFF8B5A2B),
                        indicatorColor = Color(0xFF8B5A2B),
                        unselectedIconColor = Color(0xFF795548),
                        unselectedTextColor = Color(0xFF795548)
                    )
                )
            }
        },
        containerColor = Color(0xFFF7F4EB)
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                0 -> BasketsList(viewModel)
                1 -> ProductsList(viewModel)
                2 -> CartScreen(viewModel)
                3 -> CustomerOrdersScreen(viewModel)
            }
        }
    }
}

@Composable
fun BasketsList(viewModel: AppViewModel) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.fillMaxSize().background(Color(0xFFF7F4EB))
    ) {
        items(defaultBaskets) { basket ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color(0xFFE8E5D9), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ShoppingBasket,
                            contentDescription = "Canasta",
                            tint = Color(0xFF8B5A2B),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(basket.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF3E2723))
                        Text(basket.description, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF795548))
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("$${basket.price}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                            Button(
                                onClick = { viewModel.addToCart(CartItem(basket = basket)) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5A2B)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("Agregar", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductsList(viewModel: AppViewModel) {
    val products by viewModel.products.collectAsState()
    
    val categories = remember(products) {
        listOf("Todos") + products.map { it.category }.distinct().sorted()
    }
    var selectedCategory by remember { mutableStateOf("Todos") }
    
    val filteredProducts = remember(products, selectedCategory) {
        if (selectedCategory == "Todos") products
        else products.filter { it.category == selectedCategory }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F4EB))) {
        if (categories.size > 1) {
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
                containerColor = Color.White,
                contentColor = Color(0xFF8B5A2B),
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEach { category ->
                    Tab(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        text = { 
                            Text(
                                category.replaceFirstChar { 
                                    if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() 
                                },
                                fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal
                            ) 
                        }
                    )
                }
            }
        }

        if (filteredProducts.isEmpty()) {
             Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                 Text(
                     if (products.isEmpty()) "No hay productos disponibles por ahora." else "No hay productos en esta categoría.", 
                     color = Color(0xFF5D4037)
                 )
             }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredProducts) { product ->
                    ProductCatalogCard(product, onAdd = { viewModel.addToCart(CartItem(product = product)) })
                }
            }
        }
    }
}

@Composable
fun ProductCatalogCard(product: Product, onAdd: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(260.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFFE8E5D9))
            ) {
                AsyncImage(
                    model = product.imageUrl.ifEmpty { "https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=400&q=80" },
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                Surface(
                    color = Color(0xFF4CAF50).copy(alpha = 0.9f),
                    shape = RoundedCornerShape(bottomEnd = 16.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = "$${product.price}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF3E2723),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.category.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF795548),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Button(
                    onClick = onAdd,
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5A2B)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.AddShoppingCart, contentDescription = "Agregar", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Agregar", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun CartScreen(viewModel: AppViewModel) {
    val cart by viewModel.cart.collectAsState()
    var address by remember { mutableStateOf("") }
    val mpUrl by viewModel.mercadoPagoUrl.collectAsState()
    val context = LocalContext.current
    
    LaunchedEffect(mpUrl) {
        if (mpUrl != null) {
            val builder = CustomTabsIntent.Builder()
            val customTabsIntent = builder.build()
            try { customTabsIntent.launchUrl(context, Uri.parse(mpUrl)) } catch (e: Exception) { try { context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, Uri.parse(mpUrl))) } catch (e2: Exception) {} }
            viewModel.clearMercadoPagoUrl()
        }
    }
    
    if (cart.isEmpty()) {
         Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
             Text("El carrito está vacío.")
         }
    } else {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            val subtotal = cart.sumOf { item -> 
                (item.product?.price ?: item.basket?.price ?: 0.0) * item.quantity 
            }
            val shippingCost = if (subtotal >= 500.0) 0.0 else 50.0
            val total = subtotal + shippingCost

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(cart) { item ->
                    val name = item.product?.name ?: item.basket?.name ?: "Item"
                    val price = item.product?.price ?: item.basket?.price ?: 0.0
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${item.quantity}x $name")
                        Text("$${"%.2f".format(price * item.quantity)}")
                    }
                }
            }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal:", style = MaterialTheme.typography.bodyMedium)
                        Text("$${"%.2f".format(subtotal)}", style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Envío:", style = MaterialTheme.typography.bodyMedium)
                        Text(if (shippingCost == 0.0) "Gratis" else "$${"%.2f".format(shippingCost)}", style = MaterialTheme.typography.bodyMedium, color = if (shippingCost == 0.0) Color(0xFF4CAF50) else Color.Unspecified)
                    }
                    if (shippingCost > 0) {
                        Text("¡Agrega $${"%.2f".format(500.0 - subtotal)} más para envío gratis!", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFFA000))
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("$${"%.2f".format(total)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Dirección de envío") },
                modifier = Modifier.fillMaxWidth()
            )
            

            Spacer(modifier = Modifier.height(16.dp))
            val appError by viewModel.authError.collectAsState()
            if (appError != null) {
                Text(text = appError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
            }
            Button(

                onClick = { viewModel.checkoutWithMercadoPago(address, shippingCost) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = address.isNotBlank()
            ) {
                Text("Pagar con Mercado Pago")
            }
        }
    }
}

@Composable
fun CustomerOrdersScreen(viewModel: AppViewModel) {
    val orders by viewModel.userOrders.collectAsState()

    if (orders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF7F4EB)), contentAlignment = Alignment.Center) {
            Text("Aún no tienes pedidos.", color = Color(0xFF5D4037))
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.fillMaxSize().background(Color(0xFFF7F4EB))
        ) {
            items(orders) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                "Pedido #${order.id.takeLast(6)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3E2723)
                            )
                            val (statusText, statusColor) = when(order.status) {
                                com.example.data.OrderStatus.PENDING -> "Pendiente de pago" to Color(0xFFFFA000)
                                com.example.data.OrderStatus.PAID -> "Pagado, en espera" to Color(0xFF1976D2)
                                com.example.data.OrderStatus.PREPARING -> "Preparando" to Color(0xFF1976D2)
                                com.example.data.OrderStatus.OUT_FOR_DELIVERY -> "En camino" to Color(0xFF4CAF50)
                                com.example.data.OrderStatus.DELIVERED -> "Entregado" to Color(0xFF8B5A2B)
                                com.example.data.OrderStatus.CANCELLED -> "Cancelado" to Color(0xFFD32F2F)
                            }
                            Text(
                                statusText,
                                style = MaterialTheme.typography.labelLarge,
                                color = statusColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Total: $${"%.2f".format(order.totalAmount)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            order.items.joinToString(separator = "\n"),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF795548)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val date = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(order.timestamp))
                        Text("Fecha: $date", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
            }
        }
    }
}
