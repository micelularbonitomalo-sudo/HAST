import re

with open('app/src/main/java/com/example/viewmodel/AppViewModel.kt', 'r') as f:
    content = f.read()

old_add = """    fun addToCart(cartItem: CartItem) {
        val current = _cart.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.product?.id == cartItem.product?.id && it.product != null }
        if (existingIndex >= 0) {
            current[existingIndex] = current[existingIndex].copy(quantity = current[existingIndex].quantity + cartItem.quantity)
        } else {
            current.add(cartItem)
        }
        _cart.value = current
    }
    
    fun clearCart() {
        _cart.value = emptyList()
    }"""

new_add = """    fun addToCart(cartItem: CartItem) {
        val current = _cart.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.product?.id == cartItem.product?.id && it.product != null }
        if (existingIndex >= 0) {
            current[existingIndex] = current[existingIndex].copy(quantity = current[existingIndex].quantity + cartItem.quantity)
        } else {
            current.add(cartItem)
        }
        _cart.value = current
        
        viewModelScope.launch {
            val uid = _currentUser.value?.uid
            if (uid != null) {
                repository?.syncCart(uid, current)
            }
        }
    }
    
    fun clearCart() {
        _cart.value = emptyList()
        viewModelScope.launch {
            val uid = _currentUser.value?.uid
            if (uid != null) {
                repository?.syncCart(uid, emptyList())
            }
        }
    }"""

content = content.replace(old_add, new_add)

with open('app/src/main/java/com/example/viewmodel/AppViewModel.kt', 'w') as f:
    f.write(content)
