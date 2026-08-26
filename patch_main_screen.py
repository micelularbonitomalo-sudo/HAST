import re

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'r') as f:
    content = f.read()

new_content = """@Composable
fun MainScreen(viewModel: AppViewModel) {
    val user by viewModel.currentUser.collectAsState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (user == null) {
            LoginScreen(viewModel)
        } else {
            when (user?.role) {
                UserRole.ADMIN -> AdminScreen(viewModel)
                else -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        androidx.compose.material3.Text(
                            "Acceso Denegado", 
                            style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        androidx.compose.material3.Text(
                            "Tu cuenta (${user?.email ?: user?.name}) no tiene permisos de administrador.",
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        androidx.compose.material3.Button(onClick = { viewModel.signOut() }) {
                            androidx.compose.material3.Text("Cerrar Sesión")
                        }
                    }
                }
            }
        }
    }
}"""

content = re.sub(r'@Composable\s*fun MainScreen.*?\}', new_content, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'w') as f:
    f.write(content)
