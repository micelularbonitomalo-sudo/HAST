import re

with open('gradle/libs.versions.toml', 'r') as f:
    content = f.read()

# I need to place the vico libraries in the [libraries] section, not [plugins]
content = content.replace('''
vico-compose = { group = "com.patrykandpatrick.vico", name = "compose", version.ref = "vico" }
vico-compose-m3 = { group = "com.patrykandpatrick.vico", name = "compose-m3", version.ref = "vico" }
vico-core = { group = "com.patrykandpatrick.vico", name = "core", version.ref = "vico" }
''', '')

content = content.replace('[plugins]', '''
vico-compose = { group = "com.patrykandpatrick.vico", name = "compose", version.ref = "vico" }
vico-compose-m3 = { group = "com.patrykandpatrick.vico", name = "compose-m3", version.ref = "vico" }
vico-core = { group = "com.patrykandpatrick.vico", name = "core", version.ref = "vico" }

[plugins]''')

with open('gradle/libs.versions.toml', 'w') as f:
    f.write(content)
