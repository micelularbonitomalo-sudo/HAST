with open('app/src/main/java/com/example/viewmodel/AppViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace("""    fun addToCart(cartItem: CartItem) {
        val current = _cart.value.toMutableList()
        current.add(cartItem)
        _cart.value = current
    }""", """    fun addToCart(cartItem: CartItem) {
        val current = _cart.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.product?.id == cartItem.product?.id && it.product != null }
        if (existingIndex >= 0) {
            current[existingIndex] = current[existingIndex].copy(quantity = current[existingIndex].quantity + cartItem.quantity)
        } else {
            current.add(cartItem)
        }
        _cart.value = current
    }""")

with open('app/src/main/java/com/example/viewmodel/AppViewModel.kt', 'w') as f:
    f.write(content)
