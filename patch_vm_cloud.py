with open('app/src/main/java/com/example/viewmodel/AppViewModel.kt', 'r') as f:
    content = f.read()

new_method = """
    fun addDirectSale(amount: Double, description: String) {
        val user = _currentUser.value
        val order = Order(
            customerId = user?.uid ?: "POS",
            customerName = "Venta/Movimiento",
            address = "Local",
            totalAmount = amount,
            status = OrderStatus.PAID,
            items = listOf(description)
        )
        viewModelScope.launch {
            repository?.createOrder(order)
        }
    }
"""

if "fun addDirectSale" not in content:
    content = content.replace('fun addExpense', new_method + '\n    fun addExpense')

with open('app/src/main/java/com/example/viewmodel/AppViewModel.kt', 'w') as f:
    f.write(content)
