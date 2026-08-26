import re

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'Box(modifier = Modifier.fillMaxSize()) {', 
    'androidx.compose.material3.Surface(modifier = Modifier.fillMaxSize(), color = androidx.compose.material3.MaterialTheme.colorScheme.background) {'
)

with open('app/src/main/java/com/example/ui/screens/MainScreen.kt', 'w') as f:
    f.write(content)
