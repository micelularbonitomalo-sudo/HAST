import re

with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'r') as f:
    content = f.read()

new_admin_shell = """
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(viewModel: AppViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Punto de Venta - Casa Campo") },
                actions = {
                    IconButton(onClick = { viewModel.signOut() }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Salir")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.PointOfSale, contentDescription = "Punto de Venta") },
                    label = { Text("POS") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Inventory, contentDescription = "Inventario") },
                    label = { Text("Inventario") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Reportes") },
                    label = { Text("Reportes") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (selectedTab) {
                0 -> PosScreen(viewModel)
                1 -> AdminInventoryScreen(viewModel)
                2 -> ReportsScreen(viewModel)
            }
        }
    }
}
"""

# Replace AdminScreen
admin_match = re.search(r'@OptIn\(ExperimentalMaterial3Api::class\)\s*@Composable\s*fun AdminScreen\(viewModel: AppViewModel\) \{.*?(?=@Composable\s*fun AdminInventoryScreen)', content, re.DOTALL)
if admin_match:
    content = content.replace(admin_match.group(0), new_admin_shell)

with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'w') as f:
    f.write(content)
