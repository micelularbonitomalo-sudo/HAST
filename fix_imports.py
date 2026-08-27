import re

with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'r') as f:
    content = f.read()

imports = """
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
"""
content = content.replace('import androidx.compose.foundation.horizontalScroll', 'import androidx.compose.foundation.horizontalScroll' + imports)

# Also fix the Unresolved reference 'Spacer' errors at line 424 and 440.
# The error was: Unresolved reference 'modifier'. Only expressions are allowed in this context.
# Let's check line 424.
with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'w') as f:
    f.write(content)
