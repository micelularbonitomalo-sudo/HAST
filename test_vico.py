import re

with open('gradle/libs.versions.toml', 'r') as f:
    content = f.read()

content = content.replace('[versions]\n', '[versions]\nvico = "1.14.0"\n')
content = content + '''
vico-compose = { group = "com.patrykandpatrick.vico", name = "compose", version.ref = "vico" }
vico-compose-m3 = { group = "com.patrykandpatrick.vico", name = "compose-m3", version.ref = "vico" }
vico-core = { group = "com.patrykandpatrick.vico", name = "core", version.ref = "vico" }
'''

with open('gradle/libs.versions.toml', 'w') as f:
    f.write(content)
