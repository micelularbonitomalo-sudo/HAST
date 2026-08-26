with open('app/src/main/java/com/example/data/Models.kt', 'r') as f:
    content = f.read()

if "data class CartDocument" not in content:
    content += "\n\ndata class CartDocument(val items: List<CartItem> = emptyList())\n"
    with open('app/src/main/java/com/example/data/Models.kt', 'w') as f:
        f.write(content)
