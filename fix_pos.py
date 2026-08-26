import re

with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'r') as f:
    content = f.read()

old_card = """                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        onClick = { viewModel.addToCart(com.example.data.CartItem(product = product, quantity = 1)) }
                    ) {"""

new_card = """                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).androidx.compose.foundation.clickable {
                            viewModel.addToCart(com.example.data.CartItem(product = product, quantity = 1))
                        }
                    ) {"""

content = content.replace(old_card, new_card)

with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'w') as f:
    f.write(content)
