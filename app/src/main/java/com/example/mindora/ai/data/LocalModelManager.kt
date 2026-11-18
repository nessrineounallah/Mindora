package com.example.mindora.ai.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.StatFs
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest

class LocalModelManager(context: Context) {
    private val appContext = context.applicationContext
    private val modelDirectory = File(appContext.filesDir, "local-ai")
    val modelFile: File
        get() = File(modelDirectory, MODEL_FILE_NAME)

    fun isModelInstalled(): Boolean =
        modelFile.isFile && modelFile.length() >= MIN_MODEL_BYTES

    suspend fun importModel(
        resolver: ContentResolver,
        uri: Uri,
        onProgress: (Float?) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val sourceName = queryDisplayName(resolver, uri)
            ?: uri.lastPathSegment.orEmpty().substringAfterLast('/')
        require(sourceName == MODEL_FILE_NAME) {
            "Choose the official $MODEL_FILE_NAME file."
        }

        val expectedLength = querySize(resolver, uri)
        if (expectedLength != null && expectedLength < MIN_MODEL_BYTES) {
            throw IOException("The selected file is too small to be Qwen3-0.6B.")
        }
        if (!modelDirectory.exists() && !modelDirectory.mkdirs()) {
            throw IOException("Mindora could not create private model storage.")
        }

        val temporaryFile = File(modelDirectory, "$MODEL_FILE_NAME.importing")
        val destination = modelFile
        val backupFile = File(modelDirectory, "$MODEL_FILE_NAME.previous")
        checkAvailableSpace(expectedLength, destination.length())
        temporaryFile.delete()
        onProgress(if (expectedLength != null) 0f else null)

        try {
            val digest = MessageDigest.getInstance("SHA-256")
            var copied = 0L
            val input = resolver.openInputStream(uri)
                ?: throw IOException("Android could not open the selected model file.")

            val coroutineContext = currentCoroutineContext()
            input.use { source ->
                FileOutputStream(temporaryFile).use { fileOutput ->
                    BufferedOutputStream(fileOutput).use { output ->
                        val buffer = ByteArray(COPY_BUFFER_BYTES)
                        while (true) {
                            coroutineContext.ensureActive()
                            val count = source.read(buffer)
                            if (count < 0) break
                            output.write(buffer, 0, count)
                            digest.update(buffer, 0, count)
                            copied += count

                            if (expectedLength != null) {
                                onProgress((copied.toFloat() / expectedLength).coerceIn(0f, 1f))
                            }
                        }
                        output.flush()
                        fileOutput.fd.sync()
                    }
                }
            }

            if (expectedLength != null && copied != expectedLength) {
                throw IOException(
                    "The model import was incomplete ($copied of $expectedLength bytes). Please select the file again."
                )
            }
            if (copied < MIN_MODEL_BYTES) {
                throw IOException("The selected file is incomplete or is not Qwen3-0.6B.")
            }

            val actualSha256 = digest.digest().toHexString()
            if (!actualSha256.equals(EXPECTED_SHA256, ignoreCase = true)) {
                throw IOException(
                    "This file does not match the official Qwen3-0.6B INT8 model. Re-download the complete .litertlm file and try again."
                )
            }

            backupFile.delete()
            if (destination.exists() && !destination.renameTo(backupFile)) {
                throw IOException("Mindora could not safely replace the installed model.")
            }
            if (!temporaryFile.renameTo(destination)) {
                if (backupFile.exists()) backupFile.renameTo(destination)
                throw IOException("Mindora could not finish installing the model.")
            }
            backupFile.delete()
            onProgress(1f)
            destination
        } catch (error: Throwable) {
            temporaryFile.delete()
            if (!destination.exists() && backupFile.exists()) backupFile.renameTo(destination)
            throw error
        }
    }

    private fun checkAvailableSpace(expectedLength: Long?, oldModelLength: Long) {
        if (expectedLength == null) return
        val required = expectedLength + oldModelLength + STORAGE_SAFETY_MARGIN
        val available = StatFs(modelDirectory.absolutePath).availableBytes
        if (available < required) {
            throw IOException(
                "There is not enough free storage to import this 586 MB model. Free up space and try again."
            )
        }
    }

    private fun queryDisplayName(resolver: ContentResolver, uri: Uri): String? =
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
        }

    private fun querySize(resolver: ContentResolver, uri: Uri): Long? =
        resolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (index >= 0 && cursor.moveToFirst()) cursor.getLong(index).takeIf { it > 0L } else null
        }

    private fun ByteArray.toHexString(): String = joinToString(separator = "") { byte ->
        (byte.toInt() and 0xff).toString(16).padStart(2, '0')
    }

    companion object {
        const val MODEL_FILE_NAME = "Qwen3-0.6B.litertlm"
        private const val MIN_MODEL_BYTES = 500L * 1024L * 1024L
        private const val COPY_BUFFER_BYTES = 128 * 1024
        private const val STORAGE_SAFETY_MARGIN = 32L * 1024L * 1024L
        private const val EXPECTED_SHA256 =
            "c5f39b4191926786c3e6668b62af4d6eb15248dfc7a5348252a9b406f33f2108"
    }
}
