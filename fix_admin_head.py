with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'r') as f:
    lines = f.readlines()

if lines[0].startswith('import'):
    lines.pop(0)

lines.insert(2, "import androidx.compose.foundation.clickable\n")

with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'w') as f:
    f.writelines(lines)
