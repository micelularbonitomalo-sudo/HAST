import re

with open('app/src/main/java/com/example/viewmodel/AppViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace('import com.example.data.Order\nimport com.example.data.Expense\nimport com.example.data.Order\nimport com.example.data.ExpenseStatus', 'import com.example.data.Order\nimport com.example.data.Expense\nimport com.example.data.OrderStatus')

with open('app/src/main/java/com/example/viewmodel/AppViewModel.kt', 'w') as f:
    f.write(content)
