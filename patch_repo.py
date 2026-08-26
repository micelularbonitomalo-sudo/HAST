import re

with open('app/src/main/java/com/example/data/FirestoreRepository.kt', 'r') as f:
    content = f.read()

new_methods = """
    // Cart Synchronization
    suspend fun syncCart(userId: String, items: List<CartItem>) {
        db.collection("carts").document(userId).set(CartDocument(items)).await()
    }
    
    fun getCartFlow(userId: String): Flow<List<CartItem>> = callbackFlow {
        val listener = db.collection("carts").document(userId).addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                val cartDoc = snapshot.toObject(CartDocument::class.java)
                trySend(cartDoc?.items ?: emptyList())
            } else {
                trySend(emptyList())
            }
        }
        awaitClose { listener.remove() }
    }
"""

if "fun syncCart" not in content:
    content = content.replace("class FirestoreRepository {", "class FirestoreRepository {\n" + new_methods)
    with open('app/src/main/java/com/example/data/FirestoreRepository.kt', 'w') as f:
        f.write(content)
