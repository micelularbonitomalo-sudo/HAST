import re

with open('app/src/main/java/com/example/ui/screens/CustomerScreen.kt', 'r') as f:
    content = f.read()

new_ui = """
            Spacer(modifier = Modifier.height(16.dp))
            val appError by viewModel.authError.collectAsState()
            if (appError != null) {
                Text(text = appError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
            }
            Button(
"""
content = content.replace("            Spacer(modifier = Modifier.height(16.dp))\n            Button(", new_ui)

with open('app/src/main/java/com/example/ui/screens/CustomerScreen.kt', 'w') as f:
    f.write(content)
