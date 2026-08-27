import re

with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('fun AdminScreen(viewModel: AppViewModel) {', '@Composable\nfun AdminScreen(viewModel: AppViewModel) {')

with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'w') as f:
    f.write(content)
