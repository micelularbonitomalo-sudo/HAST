package com.example.data

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: UserRole = UserRole.CUSTOMER
)

enum class UserRole {
    CUSTOMER, ADMIN, STAFF
}

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val cost: Double = 0.0,
    val category: String = "frutas", // frutas, verduras, avicolas, ovinos, porcinos
    val imageUrl: String = "",
    val stock: Double = 0.0,
    val unitType: String = "kg",
    val totalPaidToSupplier: Double = 0.0,
    val quantityReceived: Double = 0.0,
    val profitMarginPercent: Double = 30.0,
    val solidarityMarginPercent: Double = 10.0
)

data class BasketOption(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val itemCount: Int = 0,
    val description: String = ""
)

val defaultBaskets = listOf(
    BasketOption("b1", "Canasta Pequeña", 250.0, 10, "10 artículos a elección"),
    BasketOption("b2", "Canasta Mediana", 300.0, 13, "13 artículos a elección"),
    BasketOption("b3", "Canasta Grande", 350.0, 16, "16 artículos a elección"),
    BasketOption("b4", "Canasta Extra Grande", 400.0, 19, "19 artículos a elección")
)

data class CartItem(
    val product: Product? = null,
    val basket: BasketOption? = null,
    val quantity: Int = 1
)

data class Order(
    val id: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val address: String = "",
    val totalAmount: Double = 0.0,
    val status: OrderStatus = OrderStatus.PENDING,
    val items: List<String> = emptyList(), // Store descriptions or references
    val deliveryDriverId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class OrderStatus {
    PENDING,
    PAID,
    PREPARING,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}
