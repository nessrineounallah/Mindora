package com.example.mindora.ai.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindora.ai.data.LiteRtLmLocalAiRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class LocalAiMessageRole { USER, ASSISTANT }

data class LocalAiMessage(
    val id: Long,
    val role: LocalAiMessageRole,
    val text: String
)

data class LocalAiUiState(
    val modelInstalled: Boolean = false,
    val modelLoading: Boolean = false,
    val modelImporting: Boolean = false,
    val importProgress: Float? = null,
    val modelReady: Boolean = false,
    val generating: Boolean = false,
    val messages: List<LocalAiMessage> = emptyList(),
    val error: String? = null
)

class LocalAiViewModel(
    application: Application,
    private val repository: LiteRtLmLocalAiRepository
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(
        LocalAiUiState(modelInstalled = repository.isModelInstalled())
    )
    val uiState: StateFlow<LocalAiUiState> = _uiState.asStateFlow()

    fun loadModel() {
        if (_uiState.value.modelLoading || _uiState.value.modelReady) return
        _uiState.update { it.copy(modelLoading = true, error = null) }
        viewModelScope.launch {
            try {
                repository.loadModel()
                _uiState.update { it.copy(modelInstalled = true, modelReady = true, modelLoading = false) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (memory: OutOfMemoryError) {
                _uiState.update {
                    it.copy(
                        modelLoading = false,
                        modelReady = false,
                        error = "This device does not have enough free memory to load Qwen3 0.6B. Close other apps and try again."
                    )
                }
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        modelLoading = false,
                        modelReady = false,
                        error = error.localizedMessage ?: "Mindora could not load the local model."
                    )
                }
            }
        }
    }

    fun importModel(uri: Uri) {
        if (_uiState.value.modelImporting) return
        _uiState.update { it.copy(modelImporting = true, importProgress = 0f, error = null) }
        viewModelScope.launch {
            try {
                repository.importModel(
                    resolver = getApplication<Application>().contentResolver,
                    uri = uri,
                    onProgress = { progress ->
                        _uiState.update { it.copy(importProgress = progress) }
                    }
                )
                _uiState.update {
                    it.copy(modelInstalled = true, modelImporting = false, importProgress = null)
                }
                loadModel()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        modelImporting = false,
                        importProgress = null,
                        error = error.localizedMessage ?: "Mindora could not import the model."
                    )
                }
            }
        }
    }

    fun sendMessage(input: String) {
        val prompt = input.trim()
        val current = _uiState.value
        if (prompt.isEmpty() || !current.modelReady || current.generating) return

        val userMessage = LocalAiMessage(System.currentTimeMillis(), LocalAiMessageRole.USER, prompt)
        val assistantMessage = LocalAiMessage(System.currentTimeMillis() + 1L, LocalAiMessageRole.ASSISTANT, "")
        _uiState.update {
            it.copy(
                messages = it.messages + userMessage + assistantMessage,
                generating = true,
                error = null
            )
        }

        viewModelScope.launch {
            try {
                repository.sendMessage(prompt) { chunk ->
                    _uiState.update { state ->
                        val lastAssistantIndex = state.messages.indexOfLast {
                            it.role == LocalAiMessageRole.ASSISTANT
                        }
                        if (lastAssistantIndex < 0) state
                        else state.copy(
                            messages = state.messages.toMutableList().also { messages ->
                                val last = messages[lastAssistantIndex]
                                messages[lastAssistantIndex] = last.copy(text = last.text + chunk)
                            }
                        )
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: OutOfMemoryError) {
                _uiState.update { it.copy(error = "The model ran out of memory while generating a reply.") }
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(error = error.localizedMessage ?: "Mindora could not generate a response.")
                }
            } finally {
                _uiState.update { it.copy(generating = false) }
            }
        }
    }

    fun startNewConversation() {
        if (_uiState.value.generating) return
        if (!_uiState.value.modelReady) {
            _uiState.update { it.copy(messages = emptyList(), error = null) }
            return
        }
        viewModelScope.launch {
            try {
                repository.startNewConversation()
                _uiState.update { it.copy(messages = emptyList(), error = null) }
            } catch (error: Exception) {
                _uiState.update { it.copy(error = error.localizedMessage ?: "Could not start a new conversation.") }
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        repository.release()
    }
}
