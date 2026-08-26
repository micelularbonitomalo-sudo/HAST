import re

with open('app/src/main/java/com/example/viewmodel/AppViewModel.kt', 'r') as f:
    content = f.read()

sync_old = """            launch {
                try {
                    repository?.getProductsFlow()?.collect { _products.value = it }
                try {
                    repository?.getExpensesFlow()?.collect { _expenses.value = it }
                } catch (e: Exception) {
                    Log.e("Sync", "Expenses sync error", e)
                }
                } catch (e: Exception) {
                    Log.e("Sync", "Products sync error", e)
                }
            }"""

sync_new = """            launch {
                try {
                    repository?.getProductsFlow()?.collect { _products.value = it }
                } catch (e: Exception) {
                    Log.e("Sync", "Products sync error", e)
                }
            }
            launch {
                try {
                    repository?.getExpensesFlow()?.collect { _expenses.value = it }
                } catch (e: Exception) {
                    Log.e("Sync", "Expenses sync error", e)
                }
            }
            launch {
                try {
                    val uid = _currentUser.value?.uid
                    if (uid != null) {
                        repository?.getCartFlow(uid)?.collect { _cart.value = it }
                    }
                } catch (e: Exception) {
                    Log.e("Sync", "Cart sync error", e)
                }
            }"""

if "repository?.getProductsFlow()" in content:
    content = content.replace(sync_old, sync_new)
else:
    print("Could not find sync_old")

with open('app/src/main/java/com/example/viewmodel/AppViewModel.kt', 'w') as f:
    f.write(content)
