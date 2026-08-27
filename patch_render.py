import re

with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'r') as f:
    content = f.read()

# Replace in AdminInventoryScreen
pattern1 = """                            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.background, modifier = Modifier.size(40.dp)) {
                                Icon(Icons.Default.LocalMall, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(8.dp))
                            }"""

replacement1 = """                            Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.background, modifier = Modifier.size(56.dp), shadowElevation = 2.dp) {
                                AsyncImage(
                                    model = getProductImage(product.name, product.imageUrl),
                                    contentDescription = product.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }"""
content = content.replace(pattern1, replacement1)

# Replace in PosScreen
pattern2 = """                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(48.dp)) {
                                    Icon(Icons.Default.LocalMall, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(12.dp))
                                }"""

replacement2 = """                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(56.dp), shadowElevation = 4.dp) {
                                    AsyncImage(
                                        model = getProductImage(product.name, product.imageUrl),
                                        contentDescription = product.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }"""
content = content.replace(pattern2, replacement2)

# Enhance ArmarCajasScreen items
pattern3 = """                            Column {
                                Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                                Text("Costo Prov: $${"%.2f".format(product.cost)}/kg", color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha=0.7f) else Color.Gray, style = MaterialTheme.typography.bodySmall)
                            }"""

replacement3 = """                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(12.dp), modifier = Modifier.size(48.dp), shadowElevation = 2.dp) {
                                    AsyncImage(
                                        model = getProductImage(product.name, product.imageUrl),
                                        contentDescription = product.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                                    Text("Costo Prov: $${"%.2f".format(product.cost)}/kg", color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha=0.7f) else Color.Gray, style = MaterialTheme.typography.bodySmall)
                                }
                            }"""
content = content.replace(pattern3, replacement3)

with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'w') as f:
    f.write(content)
