import re

with open('app/src/main/java/com/example/data/FirestoreRepository.kt', 'r') as f:
    content = f.read()

import_lines = "import com.google.firebase.firestore.FirebaseFirestore\nimport com.google.firebase.firestore.FirebaseFirestoreSettings\nimport com.google.firebase.firestore.PersistentCacheSettings"
content = content.replace("import com.google.firebase.firestore.FirebaseFirestore", import_lines)

db_old = "private val db = FirebaseFirestore.getInstance()"
db_new = """private val db = FirebaseFirestore.getInstance().apply {
        val settings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(
                PersistentCacheSettings.newBuilder()
                    .setSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                    .build()
            )
            .build()
        firestoreSettings = settings
    }"""

content = content.replace(db_old, db_new)

with open('app/src/main/java/com/example/data/FirestoreRepository.kt', 'w') as f:
    f.write(content)
