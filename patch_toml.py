import re

with open('gradle/libs.versions.toml', 'r') as f:
    content = f.read()

content = content.replace(
    'firebase-auth = { group = "com.google.firebase", name = "firebase-auth" }',
    'firebase-auth = { group = "com.google.firebase", name = "firebase-auth" }\nfirebase-messaging = { group = "com.google.firebase", name = "firebase-messaging" }'
)

with open('gradle/libs.versions.toml', 'w') as f:
    f.write(content)
