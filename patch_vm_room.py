import re

with open('app/src/main/java/com/example/viewmodel/AppViewModel.kt', 'r') as f:
    content = f.read()

# Modify class signature
content = content.replace('class AppViewModel : ViewModel() {', '''import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.data.local.AppDatabase
import com.example.data.local.LocalRepository
import com.example.data.local.ProductEntity
import com.example.data.local.TransactionEntity

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val localDb = AppDatabase.getDatabase(application)
    private val localRepository = LocalRepository(localDb)
    
    val localProducts: StateFlow<List<ProductEntity>> = localRepository.allProducts
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptyList())
        
    val localTransactions: StateFlow<List<TransactionEntity>> = localRepository.allTransactions
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), emptyList())
        
    val totalIncome: StateFlow<Double?> = localRepository.totalIncome
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), 0.0)
        
    val totalExpenses: StateFlow<Double?> = localRepository.totalExpenses
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), 0.0)

    fun addLocalProduct(name: String, price: Double, stock: Double) {
        viewModelScope.launch {
            localRepository.insertProduct(ProductEntity(name = name, price = price, stock = stock))
        }
    }
    
    fun updateLocalProduct(product: ProductEntity) {
        viewModelScope.launch {
            localRepository.updateProduct(product)
        }
    }
    
    fun deleteLocalProduct(product: ProductEntity) {
        viewModelScope.launch {
            localRepository.deleteProduct(product)
        }
    }
    
    fun addTransaction(type: String, amount: Double, description: String) {
        viewModelScope.launch {
            localRepository.insertTransaction(TransactionEntity(type = type, amount = amount, description = description))
        }
    }
''')

if 'import kotlinx.coroutines.flow.stateIn' not in content:
    content = content.replace('import kotlinx.coroutines.flow.asStateFlow', 'import kotlinx.coroutines.flow.asStateFlow\nimport kotlinx.coroutines.flow.stateIn')

with open('app/src/main/java/com/example/viewmodel/AppViewModel.kt', 'w') as f:
    f.write(content)
