package com.example.mindora.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mindora.ui.theme.AppSurface
import com.example.mindora.ui.theme.DeepMidnightPurple
import com.example.mindora.ui.theme.ElectricLavender
import com.example.mindora.ui.theme.RichViolet
import com.example.mindora.ui.theme.SoftCream
import com.example.mindora.ui.theme.SurfaceBorder
import com.example.mindora.ui.theme.TextMuted

private val navScreens = listOf(
    NavScreen.Home,
    NavScreen.Subjects,
    NavScreen.Tasks,
    NavScreen.Progress,
    NavScreen.Assistant,
    NavScreen.Settings
)

@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SoftCream)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = AppSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
            shadowElevation = 4.dp
        ) {
            NavigationBar(
                containerColor = Color.Transparent,
                tonalElevation = 0.dp,
                modifier = Modifier.clip(RoundedCornerShape(24.dp))
            ) {
                navScreens.forEach { screen ->
                    val selected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = { navController.navigateToMindoraScreen(screen, currentRoute) },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 10.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DeepMidnightPurple,
                            selectedTextColor = RichViolet,
                            indicatorColor = ElectricLavender.copy(alpha = .55f),
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun MindoraNavigationRail(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationRail(
        modifier = modifier.fillMaxHeight(),
        containerColor = SoftCream,
        header = {
            Box(
                modifier = Modifier
                    .padding(top = 20.dp, bottom = 24.dp)
                    .clip(CircleShape)
                    .background(DeepMidnightPurple)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("m", color = ElectricLavender, fontSize = 25.sp, fontWeight = FontWeight.Black)
            }
        }
    ) {
        navScreens.forEach { screen ->
            val selected = currentRoute == screen.route
            NavigationRailItem(
                selected = selected,
                onClick = { navController.navigateToMindoraScreen(screen, currentRoute) },
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title, fontSize = 11.sp, maxLines = 1) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = DeepMidnightPurple,
                    selectedTextColor = RichViolet,
                    indicatorColor = ElectricLavender.copy(alpha = .55f),
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                )
            )
        }
    }
}

private fun NavController.navigateToMindoraScreen(screen: NavScreen, currentRoute: String?) {
    if (currentRoute == screen.route) return
    navigate(screen.route) {
        popUpTo(NavScreen.Home.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
