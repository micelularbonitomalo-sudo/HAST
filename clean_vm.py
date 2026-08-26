import re

with open('app/src/main/java/com/example/viewmodel/AppViewModel.kt', 'r') as f:
    content = f.read()

# Remove imports
content = re.sub(r'import com\.example\.data\.local\..*\n', '', content)
content = re.sub(r'import androidx\.room\..*\n', '', content)

# Replace class definition and remove local variables
# We will match from class AppViewModel(application: Application) : AndroidViewModel(application) { down to fun addLocalProduct
# Actually it's easier to just do a regex sub
old_start = r'''class AppViewModel\(application: Application\) : AndroidViewModel\(application\) \{
    private val localDb = AppDatabase\.getDatabase\(application\)
    private val localRepository = LocalRepository\(localDb\)
    
    val localProducts: StateFlow<List<ProductEntity>> = localRepository\.allProducts
        \.stateIn\(viewModelScope, kotlinx\.coroutines\.flow\.SharingStarted\.WhileSubscribed\(5000\), emptyList\(\)\)
        
    val localTransactions: StateFlow<List<TransactionEntity>> = localRepository\.allTransactions
        \.stateIn\(viewModelScope, kotlinx\.coroutines\.flow\.SharingStarted\.WhileSubscribed\(5000\), emptyList\(\)\)
        
    val totalIncome: StateFlow<Double\?> = localRepository\.totalIncome
        \.stateIn\(viewModelScope, kotlinx\.coroutines\.flow\.SharingStarted\.WhileSubscribed\(5000\), 0\.0\)
        
    val totalExpenses: StateFlow<Double\?> = localRepository\.totalExpenses
        \.stateIn\(viewModelScope, kotlinx\.coroutines\.flow\.SharingStarted\.WhileSubscribed\(5000\), 0\.0\)

    fun addLocalProduct\(name: String, price: Double, stock: Double\) \{
        viewModelScope\.launch \{
            localRepository\.insertProduct\(ProductEntity\(name = name, price = price, stock = stock\)\)
        \}
    \}
    
    fun updateLocalProduct\(product: ProductEntity\) \{
        viewModelScope\.launch \{
            localRepository\.updateProduct\(product\)
        \}
    \}
    
    fun deleteLocalProduct\(product: ProductEntity\) \{
        viewModelScope\.launch \{
            localRepository\.deleteProduct\(product\)
        \}
    \}
    
    data class LocalCartItem\(val product: ProductEntity, val quantity: Int = 1\)

    private val _localCart = MutableStateFlow<List<LocalCartItem>>\(emptyList\(\)\)
    val localCart: StateFlow<List<LocalCartItem>> = _localCart\.asStateFlow\(\)

    fun addToLocalCart\(product: ProductEntity\) \{
        val current = _localCart\.value\.toMutableList\(\)
        val existingIndex = current\.indexOfFirst \{ it\.product\.id == product\.id \}
        if \(existingIndex >= 0\) \{
            current\[existingIndex\] = current\[existingIndex\]\.copy\(quantity = current\[existingIndex\]\.quantity \+ 1\)
        \} else \{
            current\.add\(LocalCartItem\(product, 1\)\)
        \}
        _localCart\.value = current
    \}

    fun clearLocalCart\(\) \{
        _localCart\.value = emptyList\(\)
    \}

    fun localPosCheckout\(\) \{
        val items = _localCart\.value
        if \(items\.isEmpty\(\)\) return
        
        var total = 0\.0
        val descriptions = mutableListOf<String>\(\)
        
        items\.forEach \{ item ->
            total \+= item\.product\.price \* item\.quantity
            descriptions\.add\("$\{item\.quantity\}x $\{item\.product\.name\}"\)
            
            // Deduct stock
            viewModelScope\.launch \{
                val newStock = item\.product\.stock - item\.quantity
                localRepository\.updateProduct\(item\.product\.copy\(stock = newStock\)\)
            \}
        \}
        
        viewModelScope\.launch \{
            localRepository\.insertTransaction\(TransactionEntity\(
                type = "INGRESO",
                amount = total,
                description = "Venta POS: " \+ descriptions\.joinToString\(", "\)
            \)\)
            clearLocalCart\(\)
        \}
    \}

    fun addTransaction\(type: String, amount: Double, description: String\) \{
        viewModelScope\.launch \{
            localRepository\.insertTransaction\(TransactionEntity\(type = type, amount = amount, description = description\)\)
        \}
    \}'''

# Just make it a standard ViewModel again
new_start = """class AppViewModel(application: Application) : AndroidViewModel(application) {"""
content = re.sub(old_start, new_start, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/viewmodel/AppViewModel.kt', 'w') as f:
    f.write(content)
