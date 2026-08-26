import re

with open('app/src/main/java/com/example/viewmodel/AppViewModel.kt', 'r') as f:
    content = f.read()

# Add Expense import
content = content.replace('import com.example.data.Order', 'import com.example.data.Order\nimport com.example.data.Expense')

# Add Expenses state
expenses_state = """
    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()
"""
content = content.replace('    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())', expenses_state + '\n    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())')


sync_expenses = """
                try {
                    repository?.getExpensesFlow()?.collect { _expenses.value = it }
                } catch (e: Exception) {
                    Log.e("Sync", "Expenses sync error", e)
                }
"""
content = content.replace('repository?.getProductsFlow()?.collect { _products.value = it }', 'repository?.getProductsFlow()?.collect { _products.value = it }\n' + sync_expenses)

# Add addExpense method
add_expense_method = """
    fun addExpense(description: String, amount: Double, category: String = "General") {
        viewModelScope.launch {
            repository?.addExpense(Expense(description = description, amount = amount, category = category))
        }
    }
"""

content = content.replace('    // Cart Actions', add_expense_method + '\n    // Cart Actions')

# Replace checkout with POS checkout
pos_checkout = """
    fun posCheckout() {
        val items = _cart.value
        if (items.isEmpty()) return
        
        var total = 0.0
        val descriptions = mutableListOf<String>()
        
        items.forEach { item ->
            if (item.product != null) {
                total += item.product.price * item.quantity
                descriptions.add("${item.quantity}x ${item.product.name}")
                // Update stock
                viewModelScope.launch {
                    val updatedProduct = item.product.copy(stock = item.product.stock - item.quantity)
                    repository?.updateProduct(updatedProduct)
                }
            }
        }
        
        val user = _currentUser.value
        
        val order = Order(
            customerId = user?.uid ?: "POS",
            customerName = "Venta en Mostrador",
            address = "Local",
            totalAmount = total,
            status = OrderStatus.PAID,
            items = descriptions
        )
        
        viewModelScope.launch {
            repository?.createOrder(order)
            clearCart()
        }
    }
"""

content = content.replace('    // Cart Actions', '    // POS Checkout\n' + pos_checkout + '\n    // Cart Actions')

with open('app/src/main/java/com/example/viewmodel/AppViewModel.kt', 'w') as f:
    f.write(content)
