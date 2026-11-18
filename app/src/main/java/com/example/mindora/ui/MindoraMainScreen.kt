package com.example.mindora.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.mindora.ai.viewmodel.LocalAiViewModel
import com.example.mindora.ui.navigation.BottomNavigationBar
import com.example.mindora.ui.navigation.MindoraAppNavHost
import com.example.mindora.ui.navigation.MindoraNavigationRail
import com.example.mindora.ui.theme.MindoraTheme
import com.example.mindora.ui.theme.SoftCream
import com.example.mindora.viewmodel.HomeViewModel
import com.example.mindora.viewmodel.ProgressViewModel
import com.example.mindora.viewmodel.SubjectViewModel
import com.example.mindora.viewmodel.TaskViewModel
import com.example.mindora.viewmodel.StudentProfileViewModel

private const val PREFERENCES_NAME = "mindora_preferences"
private const val NIGHT_MODE_KEY = "night_mode_enabled"

@Composable
fun MindoraMainScreen(
    homeViewModel: HomeViewModel,
    subjectViewModel: SubjectViewModel,
    taskViewModel: TaskViewModel,
    progressViewModel: ProgressViewModel,
    studentProfileViewModel: StudentProfileViewModel,
    localAiViewModel: LocalAiViewModel
) {
    val context = LocalContext.current
    val preferences = remember(context) {
        context.getSharedPreferences(PREFERENCES_NAME, android.content.Context.MODE_PRIVATE)
    }
    var isDarkMode by rememberSaveable {
        mutableStateOf(preferences.getBoolean(NIGHT_MODE_KEY, false))
    }

    LaunchedEffect(isDarkMode) {
        preferences.edit().putBoolean(NIGHT_MODE_KEY, isDarkMode).apply()
    }

    MindoraTheme(darkTheme = isDarkMode) {
        val navController = rememberNavController()
        BoxWithConstraints(Modifier.fillMaxSize()) {
            if (maxWidth >= 840.dp) {
                Row(Modifier.fillMaxSize()) {
                    MindoraNavigationRail(navController = navController)
                    Box(Modifier.weight(1f).fillMaxHeight()) {
                        MindoraAppNavHost(
                            navController = navController,
                            homeViewModel = homeViewModel,
                            subjectViewModel = subjectViewModel,
                            taskViewModel = taskViewModel,
                            progressViewModel = progressViewModel,
                            studentProfileViewModel = studentProfileViewModel,
                            localAiViewModel = localAiViewModel,
                            isDarkMode = isDarkMode,
                            onDarkModeChange = { isDarkMode = it },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            } else {
                Scaffold(
                    containerColor = SoftCream,
                    bottomBar = { BottomNavigationBar(navController = navController) }
                ) { paddingValues ->
                    MindoraAppNavHost(
                        navController = navController,
                        homeViewModel = homeViewModel,
                        subjectViewModel = subjectViewModel,
                        taskViewModel = taskViewModel,
                        progressViewModel = progressViewModel,
                        studentProfileViewModel = studentProfileViewModel,
                        localAiViewModel = localAiViewModel,
                        isDarkMode = isDarkMode,
                        onDarkModeChange = { isDarkMode = it },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}
