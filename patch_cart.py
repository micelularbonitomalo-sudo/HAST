import re

with open('app/src/main/java/com/example/viewmodel/AppViewModel.kt', 'r') as f:
    content = f.read()

local_cart_code = """
data class LocalCartItem(val product: ProductEntity, val quantity: Int = 1)

    private val _localCart = MutableStateFlow<List<LocalCartItem>>(emptyList())
    val localCart: StateFlow<List<LocalCartItem>> = _localCart.asStateFlow()

    fun addToLocalCart(product: ProductEntity) {
        val current = _localCart.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.product.id == product.id }
        if (existingIndex >= 0) {
            current[existingIndex] = current[existingIndex].copy(quantity = current[existingIndex].quantity + 1)
        } else {
            current.add(LocalCartItem(product, 1))
        }
        _localCart.value = current
    }

    fun clearLocalCart() {
        _localCart.value = emptyList()
    }

    fun localPosCheckout() {
        val items = _localCart.value
        if (items.isEmpty()) return
        
        var total = 0.0
        val descriptions = mutableListOf<String>()
        
        items.forEach { item ->
            total += item.product.price * item.quantity
            descriptions.add("${item.quantity}x ${item.product.name}")
            
            // Deduct stock
            viewModelScope.launch {
                val newStock = item.product.stock - item.quantity
                localRepository.updateProduct(item.product.copy(stock = newStock))
            }
        }
        
        viewModelScope.launch {
            localRepository.insertTransaction(TransactionEntity(
                type = "INGRESO",
                amount = total,
                description = "Venta POS: " + descriptions.joinToString(", ")
            ))
            clearLocalCart()
        }
    }
"""

if "data class LocalCartItem" not in content:
    content = content.replace('fun addTransaction', local_cart_code + '\n    fun addTransaction')

with open('app/src/main/java/com/example/viewmodel/AppViewModel.kt', 'w') as f:
    f.write(content)
