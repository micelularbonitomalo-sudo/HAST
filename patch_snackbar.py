with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('''                                    coroutineScope.launch {
                                        // Fire and forget since we don't have coroutine scope in context
                                    }''', '''                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Movimiento registrado exitosamente")
                                    }''')

with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'w') as f:
    f.write(content)
