package com.example.viewmodel

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FirestoreRepository
import com.example.data.Order
import com.example.data.OrderStatus
import com.example.data.Product
import com.example.data.User
import com.example.data.UserRole
import com.example.data.CartItem
import com.example.data.BasketOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.util.Log
import android.app.Activity
import java.util.concurrent.TimeUnit
import com.example.network.MPBackUrls
import com.example.network.MPItem
import com.example.network.MPPreferenceRequest
import com.example.network.RetrofitClient
import com.example.BuildConfig

class AppViewModel : ViewModel() {
    private var auth: FirebaseAuth? = null
    private var repository: FirestoreRepository? = null

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()
    
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()
    
    private val _userOrders = MutableStateFlow<List<Order>>(emptyList())
    val userOrders: StateFlow<List<Order>> = _userOrders.asStateFlow()
    
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()
    
    private val _authError = MutableStateFlow<String?>(null)
    val authError = _authError.asStateFlow()
    
    private var storedVerificationId: String? = null
    private val _isCodeSent = MutableStateFlow(false)
    val isCodeSent = _isCodeSent.asStateFlow()

    private var dataSyncJob: kotlinx.coroutines.Job? = null

    init {
        try {
            auth = FirebaseAuth.getInstance()
            repository = FirestoreRepository()
            checkCurrentUser()
            
            viewModelScope.launch {
                _currentUser.collect { user ->
                    if (user != null) {
                        startDataSync()
                    } else {
                        dataSyncJob?.cancel()
                    }
                }
            }
        } catch (e: Exception) {
            _authError.value = "Init error: ${e.message ?: e.javaClass.simpleName}"
            Log.e("FirebaseInit", "Firebase is not initialized", e)
        }
    }

    private fun startDataSync() {
        dataSyncJob?.cancel()
        dataSyncJob = viewModelScope.launch {
            launch {
                try {
                    repository?.getProductsFlow()?.collect { _products.value = it }
                } catch (e: Exception) {
                    Log.e("Sync", "Products sync error", e)
                }
            }
            launch {
                try {
                    repository?.getOrdersFlow()?.collect { _orders.value = it }
                } catch (e: Exception) {
                    Log.e("Sync", "Orders sync error", e)
                }
            }
            launch {
                try {
                    val uid = _currentUser.value?.uid
                    if (uid != null) {
                        repository?.getOrdersForUser(uid)?.collect { _userOrders.value = it }
                    }
                } catch (e: Exception) {
                    Log.e("Sync", "User orders sync error", e)
                }
            }
            launch {
                try {
                    repository?.getUsersFlow()?.collect { _allUsers.value = it }
                } catch (e: Exception) {
                    Log.e("Sync", "Users sync error", e)
                }
            }
        }
    }

    private suspend fun determineInitialRole(firebaseUser: com.google.firebase.auth.FirebaseUser): UserRole {
        val isMaster = firebaseUser.email == "gm521947@gmail.com" || firebaseUser.phoneNumber == "+528132641024"
        if (isMaster) return UserRole.ADMIN
        val phone = firebaseUser.phoneNumber
        if (phone != null) {
            val preRegisteredRole = repository?.checkPreRegisteredStaff(phone)
            if (preRegisteredRole != null) return preRegisteredRole
        }
        return UserRole.CUSTOMER
    }

    private fun checkCurrentUser() {
        val firebaseUser = auth?.currentUser
        if (firebaseUser != null) {
            viewModelScope.launch {
                val user = repository?.getUser(firebaseUser.uid)
                val isMaster = firebaseUser.email == "gm521947@gmail.com" || firebaseUser.phoneNumber == "+528132641024"
                if (user != null) {
                    if (isMaster && user.role != UserRole.ADMIN) {
                        changeUserRole(user.uid, UserRole.ADMIN)
                        _currentUser.value = user.copy(role = UserRole.ADMIN)
                    } else {
                        _currentUser.value = user
                    }
                } else {
                    val role = determineInitialRole(firebaseUser)
                    val newUser = User(
                        uid = firebaseUser.uid,
                        name = firebaseUser.displayName ?: firebaseUser.phoneNumber ?: "User",
                        email = firebaseUser.email ?: "",
                        role = role
                    )
                    repository?.saveUser(newUser)
                    _currentUser.value = newUser
                }
            }
        }
    }

    suspend fun signInWithGoogle(context: Context) {
        val credentialManager = CredentialManager.create(context)
        
        // This requires the Web Client ID from Firebase Console to work properly.
        // For prototyping, we'll try to fetch it from resources, but it often needs setup.
        // As a fallback for local testing without valid setup, we might fail gracefully.
        val webClientId = try {
            context.getString(com.example.R.string.default_web_client_id)
        } catch (e: Exception) {
            ""
        }
        
        if (webClientId.isEmpty() || webClientId == "0") {
             _authError.value = "Web client ID not found in resources. Check google-services.json."
             return
        }

        val googleIdOption = GetSignInWithGoogleOption.Builder(webClientId)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val result = credentialManager.getCredential(context, request)
            handleSignInResult(result)
        } catch (e: GetCredentialException) {
            _authError.value = "Google Sign-In failed: ${e.message}"
            Log.e("Auth", "SignIn Error", e)
        }
    }

    private suspend fun handleSignInResult(result: GetCredentialResponse) {
        val credential = result.credential
        if (credential is androidx.credentials.CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                
                val authResult = auth?.signInWithCredential(firebaseCredential)?.await()
                val firebaseUser = authResult?.user
                if (firebaseUser != null) {
                    var user = repository?.getUser(firebaseUser.uid)
                    val isMaster = firebaseUser.email == "gm521947@gmail.com" || firebaseUser.phoneNumber == "+528132641024"
                    if (user == null) {
                        val role = determineInitialRole(firebaseUser)
                        user = User(
                            uid = firebaseUser.uid,
                            name = firebaseUser.displayName ?: firebaseUser.phoneNumber ?: "User",
                            email = firebaseUser.email ?: "",
                            role = role
                        )
                        repository?.saveUser(user)
                    } else if (isMaster && user.role != UserRole.ADMIN) {
                        changeUserRole(user.uid, UserRole.ADMIN)
                        user = user.copy(role = UserRole.ADMIN)
                    }
                    _currentUser.value = user
                }
            } catch (e: Exception) {
                _authError.value = "Firebase Auth failed: ${e.message}"
            }
        } else {
            _authError.value = "Unexpected credential type"
        }
    }

    fun sendPhoneVerification(phoneNumber: String, activity: Activity) {
        val authInstance = auth
        if (authInstance == null) {
            _authError.value = "Firebase Auth not initialized (Check earlier Init Error)"
            return
        }
        
        val options = PhoneAuthOptions.newBuilder(authInstance)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneCredential(credential)
                }
                override fun onVerificationFailed(e: FirebaseException) {
                    _authError.value = "Phone Auth failed: ${e.message}"
                }
                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    storedVerificationId = verificationId
                    _isCodeSent.value = true
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyPhoneCode(code: String) {
        val verificationId = storedVerificationId
        if (verificationId != null) {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            signInWithPhoneCredential(credential)
        } else {
            _authError.value = "Please request a code first."
        }
    }

    private fun signInWithPhoneCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            try {
                val authResult = auth?.signInWithCredential(credential)?.await()
                val firebaseUser = authResult?.user
                if (firebaseUser != null) {
                    var user = repository?.getUser(firebaseUser.uid)
                    val isMaster = firebaseUser.phoneNumber == "+528132641024" || firebaseUser.email == "gm521947@gmail.com"
                    if (user == null) {
                        val role = determineInitialRole(firebaseUser)
                        user = User(
                            uid = firebaseUser.uid,
                            name = firebaseUser.phoneNumber ?: "User",
                            email = firebaseUser.email ?: "",
                            role = role
                        )
                        repository?.saveUser(user)
                    } else if (isMaster && user.role != UserRole.ADMIN) {
                        changeUserRole(user.uid, UserRole.ADMIN)
                        user = user.copy(role = UserRole.ADMIN)
                    }
                    _currentUser.value = user
                    _isCodeSent.value = false
                    storedVerificationId = null
                }
            } catch (e: Exception) {
                _authError.value = "Invalid code: ${e.message}"
            }
        }
    }

    fun signOut() {
        auth?.signOut()
        _currentUser.value = null
    }

    fun setRoleForDemo(role: UserRole) {
        val user = _currentUser.value ?: return
        val updatedUser = user.copy(role = role)
        viewModelScope.launch {
            repository?.saveUser(updatedUser)
            _currentUser.value = updatedUser
        }
    }
    
    // Admin Actions
    fun preRegisterStaff(phoneNumber: String, role: UserRole) {
        viewModelScope.launch {
            if (verifyAdminAccess()) {
                repository?.preRegisterStaff(phoneNumber, role)
            } else {
                _authError.value = "Acceso denegado: Requiere permisos de administrador"
            }
        }
    }

    fun changeUserRole(uid: String, role: UserRole) {
        viewModelScope.launch {
            if (verifyAdminAccess()) {
                repository?.updateUserRole(uid, role)
            } else {
                _authError.value = "Acceso denegado: Requiere permisos de administrador"
            }
        }
    }

    private suspend fun verifyAdminAccess(): Boolean {
        val uid = auth?.currentUser?.uid ?: return false
        val user = repository?.getUser(uid)
        return user?.role == UserRole.ADMIN
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            if (verifyAdminAccess()) {
                repository?.addProduct(product)
            } else {
                _authError.value = "Acceso denegado: Requiere permisos de administrador"
            }
        }
    }
    
    // Cart Actions
    fun addToCart(cartItem: CartItem) {
        val current = _cart.value.toMutableList()
        current.add(cartItem)
        _cart.value = current
    }
    
    fun clearCart() {
        _cart.value = emptyList()
    }
    
    private val _mercadoPagoUrl = MutableStateFlow<String?>(null)
    val mercadoPagoUrl: StateFlow<String?> = _mercadoPagoUrl.asStateFlow()

    fun clearMercadoPagoUrl() {
        _mercadoPagoUrl.value = null
    }

    // Checkout modified to use MP
    fun checkoutWithMercadoPago(address: String, shippingCost: Double = 0.0) {
        val user = _currentUser.value ?: return
        val items = _cart.value
        if (items.isEmpty()) return
        
        var total = 0.0
        val descriptions = mutableListOf<String>()
        val mpItems = mutableListOf<MPItem>()

        items.forEach { item ->
            if (item.product != null) {
                total += item.product.price * item.quantity
                descriptions.add("${item.quantity}x ${item.product.name}")
                mpItems.add(MPItem(
                    title = item.product.name,
                    quantity = item.quantity,
                    unit_price = item.product.price
                ))
            } else if (item.basket != null) {
                total += item.basket.price * item.quantity
                descriptions.add("${item.quantity}x ${item.basket.name}")
                mpItems.add(MPItem(
                    title = item.basket.name,
                    quantity = item.quantity,
                    unit_price = item.basket.price
                ))
            }
        }
        
        if (shippingCost > 0) {
            total += shippingCost
            descriptions.add("Envío a Domicilio ($${shippingCost})")
            mpItems.add(MPItem(
                title = "Envío a Domicilio",
                quantity = 1,
                unit_price = shippingCost
            ))
        }

        val order = Order(
            customerId = user.uid,
            customerName = user.name,
            address = address,
            totalAmount = total,
            status = OrderStatus.PENDING,
            items = descriptions
        )
        
        viewModelScope.launch {
            try {
                // 1. Guardar orden local
                val orderId = repository?.createOrder(order) ?: return@launch
                clearCart()

                // 2. Llamar API Mercado Pago
                val request = MPPreferenceRequest(
                    items = mpItems,
                    back_urls = MPBackUrls(
                        success = "casacampo://payment/success",
                        failure = "casacampo://payment/failure",
                        pending = "casacampo://payment/pending"
                    ),
                    external_reference = orderId
                )

                val token = BuildConfig.MERCADO_PAGO_ACCESS_TOKEN
                if (token.isNotEmpty()) {
                    val response = RetrofitClient.mercadoPagoService.createPreference(
                        authorization = "Bearer $token",
                        request = request
                    )
                    _mercadoPagoUrl.value = response.init_point
                } else {
                    _authError.value = "Falta configurar MERCADO_PAGO_ACCESS_TOKEN en los secretos"
                }

            } catch (e: Exception) {
                Log.e("MP", "Error creando preferencia", e)
                _authError.value = "Error al procesar el pago: ${e.message}"
            }
        }
    }
    
    // Original Checkout
    fun checkout(address: String) {
        val user = _currentUser.value ?: return
        val items = _cart.value
        if (items.isEmpty()) return
        
        var total = 0.0
        val descriptions = mutableListOf<String>()
        items.forEach { item ->
            if (item.product != null) {
                total += item.product.price * item.quantity
                descriptions.add("${item.quantity}x ${item.product.name}")
            } else if (item.basket != null) {
                total += item.basket.price * item.quantity
                descriptions.add("${item.quantity}x ${item.basket.name}")
            }
        }
        
        val order = Order(
            customerId = user.uid,
            customerName = user.name,
            address = address,
            totalAmount = total,
            status = OrderStatus.PENDING,
            items = descriptions
        )
        
        viewModelScope.launch {
            repository?.createOrder(order)
            clearCart()
        }
    }
    
    fun processPayment(orderId: String) {
        viewModelScope.launch {
             repository?.updateOrderStatus(orderId, OrderStatus.PAID)
        }
    }
    
    // Delivery Actions
    fun updateOrderStatus(orderId: String, status: OrderStatus) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
             repository?.updateOrderStatus(orderId, status, user.uid)
        }
    }
}
