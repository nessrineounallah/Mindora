package com.example.mindora.ai.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Conversation
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class LiteRtLmLocalAiRepository(context: Context) {
    private val modelManager = LocalModelManager(context)
    private val engineExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "mindora-local-ai").apply { priority = Thread.NORM_PRIORITY - 1 }
    }
    private val engineDispatcher = engineExecutor.asCoroutineDispatcher()
    private val released = AtomicBoolean(false)

    private var engine: Engine? = null
    private var conversation: Conversation? = null

    fun isModelInstalled(): Boolean = modelManager.isModelInstalled()

    suspend fun importModel(
        resolver: ContentResolver,
        uri: Uri,
        onProgress: (Float?) -> Unit
    ): File = modelManager.importModel(resolver, uri, onProgress)

    suspend fun loadModel() = withContext(engineDispatcher) {
        check(!released.get()) { "The local AI engine has been closed." }
        check(modelManager.isModelInstalled()) {
            "Import the verified " + LocalModelManager.MODEL_FILE_NAME + " model before loading the assistant."
        }

        val activeEngine: Engine? = engine
        if (activeEngine != null) {
            if (conversation == null) {
                conversation = activeEngine.createConversation()
            }
            return@withContext
        }

        val cacheDir = File(modelManager.modelFile.parentFile, "cache")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }

        val candidate = Engine(
            EngineConfig(
                modelPath = modelManager.modelFile.absolutePath,
                backend = Backend.CPU(),
                cacheDir = cacheDir.absolutePath
            )
        )
        // An engine whose initialize() failed must not be closed. Store it only on success.
        candidate.initialize()
        engine = candidate
        conversation = candidate.createConversation()
    }

    suspend fun sendMessage(text: String, onChunk: (String) -> Unit) = withContext(engineDispatcher) {
        check(!released.get()) { "The local AI engine has been closed." }
        val activeConversation = checkNotNull(conversation) { "Load the model before sending a message." }
        activeConversation.sendMessageAsync(text).collect { chunk ->
            onChunk(chunk.toString())
        }
    }

    suspend fun startNewConversation() = withContext(engineDispatcher) {
        check(!released.get()) { "The local AI engine has been closed." }
        val activeEngine = checkNotNull(engine) { "Load the model before starting a conversation." }
        val oldConversation = conversation
        conversation = null
        oldConversation?.close()
        conversation = activeEngine.createConversation()
    }

    /**
     * Called from ViewModel.onCleared. Native close calls are queued behind any in-flight
     * inference and run on the same single-thread executor that owns the engine.
     */
    fun release() {
        if (!released.compareAndSet(false, true)) return
        runCatching {
            engineExecutor.execute {
                val activeConversation = conversation
                conversation = null
                runCatching { activeConversation?.close() }
                val activeEngine = engine
                engine = null
                runCatching { activeEngine?.close() }
                engineExecutor.shutdown()
            }
        }.onFailure {
            engineExecutor.shutdown()
        }
    }
}
