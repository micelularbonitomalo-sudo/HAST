import re

with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'r') as f:
    content = f.read()

# Replace localProducts with products
content = content.replace("viewModel.localProducts", "viewModel.products")
# Replace ProductEntity with Product
content = content.replace("com.example.data.local.ProductEntity", "Product")

# Update ProductDialog in AdminScreen
def dialog_replace(match):
    return """
        confirmButton = {
            Button(onClick = {
                val price = priceStr.toDoubleOrNull() ?: 0.0
                val stock = stockStr.toDoubleOrNull() ?: 0.0
                if (name.isNotBlank()) {
                    val p = initialProduct?.copy(name = name, price = price, stock = stock) 
                            ?: Product(name = name, price = price, stock = stock)
                    if (initialProduct != null) {
                        viewModel.updateProduct(p)
                    } else {
                        viewModel.addProduct(p)
                    }
                    onDismiss()
                }
            }) {
                Text("Guardar")
            }
        },
"""

content = re.sub(r'''confirmButton = \{\s*Button\(onClick = \{\s*val price = priceStr\.toDoubleOrNull\(\) \?: 0\.0\s*val stock = stockStr\.toDoubleOrNull\(\) \?: 0\.0\s*if \(name\.isNotBlank\(\)\) \{\s*onSave\(name, price, stock\)\s*\}\s*\}\) \{\s*Text\("Guardar"\)\s*\}\s*\},''', 
    """        confirmButton = {
            Button(onClick = {
                val price = priceStr.toDoubleOrNull() ?: 0.0
                val stock = stockStr.toDoubleOrNull() ?: 0.0
                if (name.isNotBlank()) {
                    onSave(name, price, stock)
                }
            }) {
                Text("Guardar")
            }
        },""", content)

# But wait, we need to change how AdminInventoryScreen calls onSave.
admin_inv_call = r'''onSave = \{ name, price, stock ->
                if \(productToEdit != null\) \{
                    viewModel\.updateLocalProduct\(productToEdit\!\!\.copy\(name = name, price = price, stock = stock\)\)
                \} else \{
                    viewModel\.addLocalProduct\(name, price, stock\)
                \}
                showAddDialog = false
                productToEdit = null
            \}'''

admin_inv_repl = """onSave = { name, price, stock ->
                if (productToEdit != null) {
                    viewModel.updateProduct(productToEdit!!.copy(name = name, price = price, stock = stock))
                } else {
                    viewModel.addProduct(Product(name = name, price = price, stock = stock))
                }
                showAddDialog = false
                productToEdit = null
            }"""

content = re.sub(admin_inv_call, admin_inv_repl, content)

# InventoryMovementScreen changes
inv_mov_update = r'''viewModel\.updateLocalProduct\(selectedProduct\!\!\.copy\(stock = newStock\)\)'''
inv_mov_repl = """viewModel.updateProduct(selectedProduct!!.copy(stock = newStock))"""
content = re.sub(inv_mov_update, inv_mov_repl, content)

inv_mov_tx = r'''viewModel\.addTransaction\(type = type, amount = amt, description = finalDesc\)'''
inv_mov_tx_repl = """if (isEntry) {
                                        viewModel.addExpense(finalDesc, amt)
                                    } else {
                                        viewModel.checkout(address = finalDesc) // we can reuse checkout or just create an order manually
                                        // Wait, the viewModel has a cart that needs to be checked out, but here we can just create an order directly if needed.
                                        // Let's just create an expense with negative amount or add a dummy order.
                                    }"""
# Wait, let's look at how ReportsScreen calculates totals, it uses orders and expenses.
# For simplicity, let's inject a direct repository call or a new viewModel method for direct sales.
