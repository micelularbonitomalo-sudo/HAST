import re

with open('app/src/main/java/com/example/viewmodel/AppViewModel.kt', 'r') as f:
    content = f.read()

new_logic = """
                    if (isMaster && user.role != UserRole.ADMIN) {
                        changeUserRole(user.uid, UserRole.ADMIN)
                        _currentUser.value = user.copy(role = UserRole.ADMIN)
                    } else {
                        _currentUser.value = user
                    }
                    cachedFcmToken?.let { token ->
                        if (_currentUser.value?.fcmToken != token) {
                            val updated = _currentUser.value!!.copy(fcmToken = token)
                            repository?.saveUser(updated)
                            _currentUser.value = updated
                        }
                    }
"""

content = content.replace("""                    if (isMaster && user.role != UserRole.ADMIN) {
                        changeUserRole(user.uid, UserRole.ADMIN)
                        _currentUser.value = user.copy(role = UserRole.ADMIN)
                    } else {
                        _currentUser.value = user
                    }""", new_logic)

with open('app/src/main/java/com/example/viewmodel/AppViewModel.kt', 'w') as f:
    f.write(content)
