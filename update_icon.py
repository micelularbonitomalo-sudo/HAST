import os

os.makedirs('app/src/main/res/drawable', exist_ok=True)
os.makedirs('app/src/main/res/mipmap-anydpi-v26', exist_ok=True)

foreground = """<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">

    <!-- Outer Stamp Ring -->
    <path android:fillColor="#5D4037" android:pathData="M 54 10 C 29.699 10 10 29.699 10 54 C 10 78.301 29.699 98 54 98 C 78.301 98 98 78.301 98 54 C 98 29.699 78.301 10 54 10 Z M 54 16 C 74.987 16 92 33.013 92 54 C 92 74.987 74.987 92 54 92 C 33.013 92 16 74.987 16 54 C 16 33.013 33.013 16 54 16 Z"/>
    
    <!-- Inner Stamp Ring -->
    <path android:fillColor="#5D4037" android:pathData="M 54 19 C 34.67 19 19 34.67 19 54 C 19 73.33 34.67 89 54 89 C 73.33 89 89 73.33 89 54 C 89 34.67 73.33 19 54 19 Z M 54 21 C 72.225 21 87 35.775 87 54 C 87 72.225 72.225 87 54 87 C 35.775 87 21 72.225 21 54 C 21 35.775 35.775 21 54 21 Z"/>

    <!-- Center Star -->
    <path android:fillColor="#5D4037" android:pathData="M54,29.2 L60.18,48.21 H80.18 L64.0,59.96 L70.18,78.98 L54,67.23 L37.82,78.98 L44.0,59.96 L27.82,48.21 H47.82 Z"/>
</vector>
"""

background = """<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#ECD9B1"
        android:pathData="M0,0h108v108h-108z" />
</vector>
"""

launcher = """<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
"""

with open('app/src/main/res/drawable/ic_launcher_foreground.xml', 'w') as f:
    f.write(foreground)

with open('app/src/main/res/drawable/ic_launcher_background.xml', 'w') as f:
    f.write(background)

with open('app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml', 'w') as f:
    f.write(launcher)

with open('app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml', 'w') as f:
    f.write(launcher)
