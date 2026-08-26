import re

with open('app/build.gradle.kts', 'r') as f:
    content = f.read()

content = content.replace('implementation(libs.androidx.core.ktx)', 'implementation(libs.androidx.core.ktx)\n    implementation(libs.vico.compose)\n    implementation(libs.vico.compose.m3)\n    implementation(libs.vico.core)')

with open('app/build.gradle.kts', 'w') as f:
    f.write(content)
