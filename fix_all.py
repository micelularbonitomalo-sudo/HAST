import re

with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'r') as f:
    content = f.read()

# Fix the @Composable mixup
content = content.replace('@Composable\n\nfun getProductImage', '\nfun getProductImage')
content = content.replace('}\nfun AdminScreen', '}\n\n@Composable\nfun AdminScreen')

# Remove duplicate imports. We can just use a regex to keep only the first occurrence of each import or just clean up manually.
# Let's just fix the conflicting imports by removing the ones we added in fix_imports.py since they might have already been there from my * imports.
# In Jetpack Compose, `import androidx.compose.ui.Alignment`, `import androidx.compose.ui.graphics.Color`, etc. are common, but `import androidx.compose.ui.*` might not include them. 
# The error says "Conflicting import: imported name 'Alignment' is ambiguous." which means it was imported twice.
content = re.sub(r'import androidx\.compose\.ui\.Alignment\n', '', content, count=1)
content = re.sub(r'import androidx\.compose\.ui\.graphics\.Color\n', '', content, count=1)
content = re.sub(r'import androidx\.compose\.ui\.text\.font\.FontWeight\n', '', content, count=1)

with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'w') as f:
    f.write(content)
