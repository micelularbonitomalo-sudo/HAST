import re

with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'r') as f:
    content = f.read()

new_header = """@Composable
fun AdminInventoryScreen(viewModel: AppViewModel) {
    val products by viewModel.localProducts.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<com.example.data.local.ProductEntity?>(null) }
    
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
            items(filteredProducts) { product ->"""

content = re.sub(r'''@Composable\s*fun AdminInventoryScreen\(viewModel: AppViewModel\)\s*\{\s*val products by viewModel\.localProducts\.collectAsState\(\)\s*var showAddDialog by remember \{ mutableStateOf\(false\) \}\s*var productToEdit by remember \{ mutableStateOf<com\.example\.data\.local\.ProductEntity\?>\(null\) \}\s*Box\(modifier = Modifier\.fillMaxSize\(\)\) \{\s*LazyColumn\(contentPadding = PaddingValues\(16\.dp\)\) \{\s*item \{\s*Text\("Inventario de Productos \(Local\)", style = MaterialTheme\.typography\.titleLarge\)\s*Spacer\(modifier = Modifier\.height\(16\.dp\)\)\s*\}\s*items\(products\) \{ product ->''', new_header, content)

with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'w') as f:
    f.write(content)
