// defines the visual appearance and direct interaction logic for the application's bottom navigation bar.
//UI component that renders the bottom bar and captures user interaction.
// Child of AppNavHose

package com.example.crowdscopeandroid

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.runtime.*
import androidx.compose.ui.layout.positionInWindow
import androidx.navigation.NavGraph.Companion.findStartDestination

@Composable
fun BottomNavigationBar(
    navController: NavController,
    onDayButtonClick: (Offset) -> Unit,
    onHourButtonClick: () -> Unit // This callback is used by AppNavHost
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Map,
        BottomNavItem.Day,
        BottomNavItem.Hour,
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val CustomGreenColor = Color(0xFF27504A)

    NavigationBar(
        containerColor = CustomGreenColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route ||
                    (item.route == BottomNavItem.Map.route && (currentRoute == BottomNavItem.Day.route || currentRoute == BottomNavItem.Hour.route))
            var itemOffset by remember { mutableStateOf(Offset.Zero) }

            NavigationBarItem(
                icon = { /* No icon, just empty lambda */ },
                label = {
                    Text(
                        text = item.label,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                selected = isSelected,
                onClick = {
                    when (item) {
                        BottomNavItem.Home -> {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                                // saveState = false // COMMENTED OUT: Removed to resolve compilation error
                            }
                        }
                        BottomNavItem.Day -> {
                            onDayButtonClick(itemOffset)
                        }
                        BottomNavItem.Hour -> {
                            onHourButtonClick()
                        }
                        else -> {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                }
                                launchSingleTop = true
                                // restoreState = true // COMMENTED OUT: Removed to resolve compilation error
                            }
                        }
                    }
                },
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    itemOffset = Offset(
                        coordinates.positionInWindow().x,
                        coordinates.positionInWindow().y
                    )
                }
            )
        }
    }
}