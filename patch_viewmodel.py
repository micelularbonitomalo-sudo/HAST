import re

with open('app/src/main/java/com/example/viewmodel/AppViewModel.kt', 'r') as f:
    content = f.read()

update_token = """
    private var cachedFcmToken: String? = null
    
    fun updateFcmToken(token: String) {
        cachedFcmToken = token
        viewModelScope.launch {
            val user = _currentUser.value
            if (user != null && user.fcmToken != token) {
                val updatedUser = user.copy(fcmToken = token)
                repository?.saveUser(updatedUser)
                _currentUser.value = updatedUser
            }
        }
    }
"""

content = content.replace("    // Cart Actions", update_token + "\n    // Cart Actions")

with open('app/src/main/java/com/example/viewmodel/AppViewModel.kt', 'w') as f:
    f.write(content)
