import re

with open('app/src/main/java/com/example/data/Models.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'val role: UserRole = UserRole.CUSTOMER',
    'val role: UserRole = UserRole.CUSTOMER,\n    val fcmToken: String = ""'
)

with open('app/src/main/java/com/example/data/Models.kt', 'w') as f:
    f.write(content)
