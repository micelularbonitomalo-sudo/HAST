import json

with open('metadata.json', 'r') as f:
    data = json.load(f)

data['name'] = "Casa Campo"

with open('metadata.json', 'w') as f:
    json.dump(data, f, indent=2)

with open('settings.gradle.kts', 'r') as f:
    content = f.read()
content = content.replace('rootProject.name = "My Application"', 'rootProject.name = "Casa Campo"')
with open('settings.gradle.kts', 'w') as f:
    f.write(content)

with open('app/src/main/res/values/strings.xml', 'r') as f:
    content = f.read()
content = content.replace('<string name="app_name">My Application</string>', '<string name="app_name">Casa Campo</string>')
with open('app/src/main/res/values/strings.xml', 'w') as f:
    f.write(content)

