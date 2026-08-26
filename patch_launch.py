with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'r') as f:
    content = f.read()

if 'import kotlinx.coroutines.launch' not in content:
    content = content.replace('import androidx.compose.ui.Modifier', 'import androidx.compose.ui.Modifier\nimport kotlinx.coroutines.launch')

with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'w') as f:
    f.write(content)
