package com.example.mindora.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavScreen(val route: String, val title: String, val icon: ImageVector) {
    object Home : NavScreen("home", "Dashboard", Icons.Default.Home)
    object Subjects : NavScreen("subjects", "Subjects", Icons.Default.Book)
    object Tasks : NavScreen("tasks", "Tasks", Icons.Default.Assignment)
    object Progress : NavScreen("progress", "Progress", Icons.Default.TrendingUp)
    object Assistant : NavScreen("assistant", "AI", Icons.Default.AutoAwesome)
    object Settings : NavScreen("settings", "Profile", Icons.Default.AccountCircle)
}
