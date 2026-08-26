import re

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'r') as f:
    content = f.read()

new_main = """
@Composable
fun MainScreen(viewModel: AppViewModel) {
    val user by viewModel.currentUser.collectAsState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (user == null) {
            LoginScreen(viewModel)
        } else {
            AdminScreen(viewModel)
        }
    }
}
"""

content = re.sub(r'@Composable\s*fun MainScreen.*?\}\n\}', new_main, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'w') as f:
    f.write(content)
