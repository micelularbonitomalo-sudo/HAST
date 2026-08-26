package com.example.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import com.google.firebase.firestore.Query

class FirestoreRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun saveUser(user: User) {
        db.collection("users").document(user.uid).set(user).await()
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

    suspend fun updateUserRole(uid: String, role: UserRole) {
        db.collection("users").document(uid).update("role", role).await()
    }

    // Products
    suspend fun addProduct(product: Product) {
        val ref = db.collection("products").document()
        val productWithId = product.copy(id = ref.id)
        ref.set(productWithId).await()
    }
    
    suspend fun updateProduct(product: Product) {
        if (product.id.isNotEmpty()) {
            db.collection("products").document(product.id).set(product).await()
        }
    }
    
    suspend fun deleteProduct(productId: String) {
        if (productId.isNotEmpty()) {
            db.collection("products").document(productId).delete().await()
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

    // Orders
    suspend fun createOrder(order: Order): String {
        val ref = db.collection("orders").document()
        val orderWithId = order.copy(id = ref.id)
        ref.set(orderWithId).await()
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

    suspend fun updateOrderStatus(orderId: String, status: OrderStatus, driverId: String? = null) {
        val updates = mutableMapOf<String, Any>("status" to status)
        if (driverId != null) {
            updates["deliveryDriverId"] = driverId
        }
        db.collection("orders").document(orderId).update(updates).await()
    }
    
    suspend fun preRegisterStaff(phoneNumber: String, role: UserRole) {
        val data = mapOf("phoneNumber" to phoneNumber, "role" to role.name)
        db.collection("pre_staff").document(phoneNumber).set(data).await()
    }
    
    suspend fun checkPreRegisteredStaff(phoneNumber: String): UserRole? {
        val doc = db.collection("pre_staff").document(phoneNumber).get().await()
        if (doc.exists()) {
            val roleStr = doc.getString("role")
            return if (roleStr != null) UserRole.valueOf(roleStr) else null
        }
        return null
    }
}
