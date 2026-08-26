import re

with open('app/src/main/java/com/example/data/FirestoreRepository.kt', 'r') as f:
    content = f.read()

# Replace .await() on write operations
writes_to_patch = [
    (r'set\(CartDocument\(items\)\)\.await\(\)', r'set(CartDocument(items))'),
    (r'set\(user\)\.await\(\)', r'set(user)'),
    (r'update\("role", role\)\.await\(\)', r'update("role", role)'),
    (r'set\(productWithId\)\.await\(\)', r'set(productWithId)'),
    (r'set\(product\)\.await\(\)', r'set(product)'),
    (r'delete\(\)\.await\(\)', r'delete()'),
    (r'set\(expenseWithId\)\.await\(\)', r'set(expenseWithId)'),
    (r'set\(orderWithId\)\.await\(\)', r'set(orderWithId)'),
    (r'update\(updates\)\.await\(\)', r'update(updates)'),
    (r'set\(data\)\.await\(\)', r'set(data)')
]

for old, new in writes_to_patch:
    content = re.sub(old, new, content)

with open('app/src/main/java/com/example/data/FirestoreRepository.kt', 'w') as f:
    f.write(content)
