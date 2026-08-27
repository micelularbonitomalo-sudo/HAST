import re

with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)\n                )                Spacer',
    'keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)\n                )\n                Spacer'
)

# And remove the duplicate imageUrl field if there is one.
# But it's easier to just do:
content = content.replace(')                Spacer', ')\n                Spacer')

with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'w') as f:
    f.write(content)
