import re

with open('app/src/main/AndroidManifest.xml', 'r') as f:
    content = f.read()

perm_str = '<uses-permission android:name="android.permission.INTERNET" />\n    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />'
content = content.replace('<uses-permission android:name="android.permission.INTERNET" />', perm_str)

service_str = """
        <service
            android:name=".MyFirebaseMessagingService"
            android:exported="false">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>
    </application>
"""
content = content.replace('</application>', service_str)

with open('app/src/main/AndroidManifest.xml', 'w') as f:
    f.write(content)
