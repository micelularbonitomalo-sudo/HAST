import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

imports = """
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.example.ui.screens.MainScreen
"""
content = content.replace("import com.example.ui.screens.MainScreen", imports)

token_logic = """
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
                Log.d("FirebaseInit", "Firebase initialized manually in MainActivity")
            }
        } catch (e: Exception) {
            Log.e("FirebaseInit", "Failed to initialize Firebase in MainActivity", e)
        }
        
        requestNotificationPermission()
"""
content = content.replace("""        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
                Log.d("FirebaseInit", "Firebase initialized manually in MainActivity")
            }
        } catch (e: Exception) {
            Log.e("FirebaseInit", "Failed to initialize Firebase in MainActivity", e)
        }""", token_logic)


request_perm = """
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                retrieveFcmToken()
            } else {
                val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                    if (isGranted) {
                        retrieveFcmToken()
                    }
                }
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            retrieveFcmToken()
        }
    }

    private fun retrieveFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                viewModel.updateFcmToken(token)
            }
        }
    }
"""
content = content.replace('    override fun onNewIntent', request_perm + '\n    override fun onNewIntent')

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
