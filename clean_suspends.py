import re

with open('app/src/main/java/com/example/data/FirestoreRepository.kt', 'r') as f:
    content = f.read()

funcs_to_clean = [
    'fun syncCart',
    'fun saveUser',
    'fun updateUserRole',
    'fun addProduct',
    'fun updateProduct',
    'fun deleteProduct',
    'fun addExpense',
    'fun createOrder',
    'fun updateOrderStatus',
    'fun preRegisterStaff'
]

for func in funcs_to_clean:
    content = content.replace(f'suspend {func}', func)

with open('app/src/main/java/com/example/data/FirestoreRepository.kt', 'w') as f:
    f.write(content)
