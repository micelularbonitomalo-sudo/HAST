import re

with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'r') as f:
    content = f.read()

# Fix ExitToApp
content = content.replace('Icons.Default.ExitToApp', 'Icons.AutoMirrored.Filled.ExitToApp')
if 'import androidx.compose.material.icons.automirrored.filled.ExitToApp' not in content:
    content = content.replace('import androidx.compose.material.icons.filled.*', 'import androidx.compose.material.icons.filled.*\nimport androidx.compose.material.icons.automirrored.filled.ExitToApp')

# Fix background import
if 'import androidx.compose.foundation.background' not in content:
    content = content.replace('import androidx.compose.foundation.layout.*', 'import androidx.compose.foundation.layout.*\nimport androidx.compose.foundation.background')

# Fix addToCart call
content = content.replace('viewModel.addToCart(product)', 'viewModel.addToCart(com.example.data.CartItem(product = product, quantity = 1))')

with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'w') as f:
    f.write(content)
