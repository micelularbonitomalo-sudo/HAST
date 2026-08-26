package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

class ElenaViewModel : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        _messages.value = listOf(
            ChatMessage("¡Hola! Soy Elena, tu asistente en Casa Campo. ¿En qué te puedo ayudar hoy?", false)
        )
    }

    fun sendMessage(text: String, executeCommand: ((String) -> Unit)? = null) {
        if (text.isBlank()) return

        val userMessage = ChatMessage(text, true)
        _messages.value = _messages.value + userMessage
        _isLoading.value = true
        
        if (text.lowercase() == "casa campo escobedo") {
            _messages.value = _messages.value + ChatMessage("¡Comando de administrador activado! Recarga la aplicación para ver los cambios. ¿Qué deseas realizar?", false)
            executeCommand?.invoke("admin")
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                
                // Build history
                val contents = _messages.value.map { msg ->
                    Content(parts = listOf(Part(text = msg.text)))
                }

                val request = GenerateContentRequest(
                    contents = contents,
                    systemInstruction = Content(
                        parts = listOf(Part(text = "Eres Elena, la asistente virtual amable, servicial y profesional de 'Casa Campo Escobedo', una tienda especializada en la venta y entrega a domicilio de productos frescos (frutas, verduras, abarrotes). Tu misión es ayudar a los clientes ofreciendo información detallada sobre los productos, resolviendo dudas sobre pedidos y recogiendo sugerencias o quejas para canalizarlas a la administración. Si te dicen 'casa campo escobedo' u otro comando de sistema, confirma la acción administrativamente."))
                    )
                )

                val response = RetrofitClient.service.generateContent(apiKey, request)
                val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Lo siento, no pude procesar tu solicitud."
                
                _messages.value = _messages.value + ChatMessage(replyText, false)
            } catch (e: Exception) {
                _messages.value = _messages.value + ChatMessage("Ocurrió un error al conectar con el soporte.", false)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
