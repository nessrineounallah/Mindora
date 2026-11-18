package com.example.mindora.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mindora.ai.ui.LocalAiScreen
import com.example.mindora.ai.viewmodel.LocalAiViewModel
import com.example.mindora.ui.screens.HomeScreen
import com.example.mindora.ui.screens.ProgressScreen
import com.example.mindora.ui.screens.SettingsScreen
import com.example.mindora.ui.screens.SubjectsScreen
import com.example.mindora.ui.screens.TasksScreen
import com.example.mindora.viewmodel.HomeViewModel
import com.example.mindora.viewmodel.ProgressViewModel
import com.example.mindora.viewmodel.SubjectViewModel
import com.example.mindora.viewmodel.TaskViewModel
import com.example.mindora.viewmodel.StudentProfileViewModel

@Composable
fun MindoraAppNavHost(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    subjectViewModel: SubjectViewModel,
    taskViewModel: TaskViewModel,
    progressViewModel: ProgressViewModel,
    studentProfileViewModel: StudentProfileViewModel,
    localAiViewModel: LocalAiViewModel,
    isDarkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavScreen.Home.route,
        modifier = modifier
    ) {
        composable(NavScreen.Home.route) {
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToTasks = { navController.navigate(NavScreen.Tasks.route) },
                onAddTaskClick = { navController.navigate(NavScreen.Tasks.route) }
            )
        }
        composable(NavScreen.Subjects.route) {
            SubjectsScreen(viewModel = subjectViewModel)
        }
        composable(NavScreen.Tasks.route) {
            TasksScreen(viewModel = taskViewModel)
        }
        composable(NavScreen.Progress.route) {
            ProgressScreen(viewModel = progressViewModel)
        }
        composable(NavScreen.Assistant.route) {
            LocalAiScreen(viewModel = localAiViewModel)
        }
        composable(NavScreen.Settings.route) {
            SettingsScreen(
                isDarkMode = isDarkMode,
                onDarkModeChange = onDarkModeChange,
                viewModel = studentProfileViewModel
            )
        }
    }
}
