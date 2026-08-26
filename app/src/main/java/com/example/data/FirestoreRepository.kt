package com.example.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import com.google.firebase.firestore.Query

class FirestoreRepository {

    // Cart Synchronization
    fun syncCart(userId: String, items: List<CartItem>) {
        db.collection("carts").document(userId).set(CartDocument(items))
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

    private val db = FirebaseFirestore.getInstance().apply {
        val settings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(
                PersistentCacheSettings.newBuilder()
                    .setSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                    .build()
            )
            .build()
        firestoreSettings = settings
    }

    fun saveUser(user: User) {
        db.collection("users").document(user.uid).set(user)
    }

    suspend fun getUser(uid: String): User? {
        val doc = db.collection("users").document(uid).get().await()
        return doc.toObject(User::class.java)
    }

    fun getUsersFlow(): Flow<List<User>> = callbackFlow {
        val listener = db.collection("users").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val users = snapshot.documents.mapNotNull { it.toObject(User::class.java) }
                trySend(users)
            }
        }
        awaitClose { listener.remove() }
    }

    fun updateUserRole(uid: String, role: UserRole) {
        db.collection("users").document(uid).update("role", role)
    }

    // Products
    fun addProduct(product: Product) {
        val ref = db.collection("products").document()
        val productWithId = product.copy(id = ref.id)
        ref.set(productWithId)
    }
    
    fun updateProduct(product: Product) {
        if (product.id.isNotEmpty()) {
            db.collection("products").document(product.id).set(product)
        }
    }
    
    fun deleteProduct(productId: String) {
        if (productId.isNotEmpty()) {
            db.collection("products").document(productId).delete()
        }
    }

    fun getProductsFlow(): Flow<List<Product>> = callbackFlow {
        val listener = db.collection("products").addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val products = snapshot.documents.mapNotNull { it.toObject(Product::class.java) }
                trySend(products)
            }
        }
        awaitClose { listener.remove() }
    }


    // Expenses
    fun addExpense(expense: Expense): String {
        val ref = db.collection("expenses").document()
        val expenseWithId = expense.copy(id = ref.id)
        ref.set(expenseWithId)
        return ref.id
    }

    fun getExpensesFlow(): Flow<List<Expense>> = callbackFlow {
        val listener = db.collection("expenses")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val expenses = snapshot.documents.mapNotNull { it.toObject(Expense::class.java) }
                    trySend(expenses)
                }
            }
        awaitClose { listener.remove() }
    }

    // Orders
    fun createOrder(order: Order): String {
        val ref = db.collection("orders").document()
        val orderWithId = order.copy(id = ref.id)
        ref.set(orderWithId)
        return ref.id
    }

    fun getOrdersFlow(): Flow<List<Order>> = callbackFlow {
        val listener = db.collection("orders")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val orders = snapshot.documents.mapNotNull { it.toObject(Order::class.java) }
                    trySend(orders)
                }
            }
        awaitClose { listener.remove() }
    }
    
    fun getOrdersForUser(userId: String): Flow<List<Order>> = callbackFlow {
        val listener = db.collection("orders")
            .whereEqualTo("customerId", userId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val orders = snapshot.documents.mapNotNull { it.toObject(Order::class.java) }
                    trySend(orders)
                }
            }
        awaitClose { listener.remove() }
    }

    fun updateOrderStatus(orderId: String, status: OrderStatus, driverId: String? = null) {
        val updates = mutableMapOf<String, Any>("status" to status)
        if (driverId != null) {
            updates["deliveryDriverId"] = driverId
        }
        db.collection("orders").document(orderId).update(updates)
    }
    
    fun preRegisterStaff(identifier: String, role: UserRole) {
        val data = mapOf("identifier" to identifier, "role" to role.name)
        db.collection("pre_staff").document(identifier).set(data)
    }
    
    suspend fun checkPreRegisteredStaff(identifier: String): UserRole? {
        val doc = db.collection("pre_staff").document(identifier).get().await()
        if (doc.exists()) {
            val roleStr = doc.getString("role")
            return if (roleStr != null) UserRole.valueOf(roleStr) else null
        }
        return null
    }
}
