package com.example.mindora.ai.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindora.ai.viewmodel.LocalAiMessage
import com.example.mindora.ai.viewmodel.LocalAiMessageRole
import com.example.mindora.ai.viewmodel.LocalAiViewModel
import com.example.mindora.ui.theme.AppSurface
import com.example.mindora.ui.theme.DeepMidnightPurple
import com.example.mindora.ui.theme.ElectricLavender
import com.example.mindora.ui.theme.ErrorRed
import com.example.mindora.ui.theme.ErrorRedLight
import com.example.mindora.ui.theme.RichViolet
import com.example.mindora.ui.theme.SoftCream
import com.example.mindora.ui.theme.SurfaceBorder
import com.example.mindora.ui.theme.TextMuted
import com.example.mindora.ui.theme.TextPrimary
import com.example.mindora.ui.theme.TextSecondary

@Composable
fun LocalAiScreen(viewModel: LocalAiViewModel) {
    val state by viewModel.uiState.collectAsState()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val modelPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) viewModel.importModel(uri)
    }

    LaunchedEffect(state.messages.size, state.messages.lastOrNull()?.text) {
        if (state.messages.isNotEmpty()) listState.animateScrollToItem(state.messages.lastIndex)
    }

    Column(
        modifier = Modifier.fillMaxSize().background(SoftCream).padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp),
                color = DeepMidnightPurple
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ElectricLavender)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Mindora AI", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text("Private · runs on this device", color = TextSecondary, fontSize = 12.sp)
            }
            IconButton(
                onClick = { viewModel.startNewConversation() },
                enabled = !state.generating && !state.modelLoading
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Start new conversation", tint = RichViolet)
            }
        }

        Spacer(Modifier.height(14.dp))

        if (state.modelInstalled) {
            ModelStatusCard(
                ready = state.modelReady,
                loading = state.modelLoading,
                onLoad = viewModel::loadModel
            )
        } else {
            ModelInstallCard(
                importing = state.modelImporting,
                progress = state.importProgress,
                onImport = { modelPicker.launch(arrayOf("*/*")) }
            )
        }

        state.error?.let { message ->
            Spacer(Modifier.height(10.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = ErrorRedLight
            ) {
                Text(
                    text = message,
                    color = ErrorRed,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        if (state.messages.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (state.modelReady) "What would you like to study?" else "Your study companion, on device.",
                        color = TextPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (state.modelReady) "Ask a question to start a private conversation."
                        else "Install the Qwen model below to begin an offline conversation.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.messages, key = LocalAiMessage::id) { message ->
                    ChatMessageBubble(message, state.generating && message == state.messages.lastOrNull())
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                enabled = state.modelReady && !state.generating,
                placeholder = { Text(if (state.modelReady) "Message your study assistant" else "Load the local model to chat") },
                maxLines = 4,
                shape = RoundedCornerShape(20.dp)
            )
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = if (state.modelReady && !state.generating && input.isNotBlank()) DeepMidnightPurple else SurfaceBorder
            ) {
                IconButton(
                    onClick = {
                        viewModel.sendMessage(input)
                        input = ""
                    },
                    enabled = state.modelReady && !state.generating && input.isNotBlank()
                ) {
                    if (state.generating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = RichViolet
                        )
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send message", tint = AppSurface)
                    }
                }
            }
        }
    }
}

@Composable
private fun ModelInstallCard(
    importing: Boolean,
    progress: Float?,
    onImport: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = AppSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Install the local model", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                "Download Qwen3-0.6B.litertlm from the LiteRT Community Qwen3-0.6B model page (about 586 MB), then choose it here. Mindora copies it into private app storage and never downloads it automatically.",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
            Spacer(Modifier.height(12.dp))
            if (importing) {
                if (progress != null) {
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                    Text("Copying model ${(progress * 100).toInt()}%", color = TextMuted, fontSize = 11.sp)
                } else {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Text("Checking and copying model…", color = TextMuted, fontSize = 11.sp)
                }
            } else {
                Button(onClick = onImport, shape = RoundedCornerShape(14.dp)) {
                    Text("Choose model file")
                }
            }
        }
    }
}

@Composable
private fun ModelStatusCard(
    ready: Boolean,
    loading: Boolean,
    onLoad: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = if (ready) ElectricLavender.copy(alpha = .28f) else AppSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (loading) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = RichViolet)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Loading Qwen3 on CPU…", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("The first load can take a little while.", color = TextSecondary, fontSize = 11.sp)
                }
            } else {
                Column(Modifier.weight(1f)) {
                    Text(
                        if (ready) "Model ready · CPU · offline" else "Qwen3 model installed",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Text(
                        if (ready) "Conversation stays on this device."
                        else "Load the model to start a conversation.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
                if (!ready) {
                    Button(onClick = onLoad, colors = ButtonDefaults.buttonColors(containerColor = DeepMidnightPurple)) {
                        Text("Load")
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(message: LocalAiMessage, generating: Boolean) {
    val isUser = message.role == LocalAiMessageRole.USER
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 320.dp),
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp
            ),
            color = if (isUser) DeepMidnightPurple else AppSurface,
            border = if (isUser) null else androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
        ) {
            if (message.text.isEmpty() && generating) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(Modifier.size(15.dp), strokeWidth = 2.dp, color = RichViolet)
                    Spacer(Modifier.width(8.dp))
                    Text("Thinking…", color = TextSecondary, fontSize = 13.sp)
                }
            } else {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                    color = if (isUser) AppSurface else TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
