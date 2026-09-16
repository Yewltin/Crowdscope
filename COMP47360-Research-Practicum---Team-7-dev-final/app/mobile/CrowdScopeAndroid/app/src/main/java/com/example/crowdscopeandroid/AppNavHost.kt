// Handles navigation between screen, defines the overall structure.

package com.example.crowdscopeandroid

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(navController: NavHostController) {
    println("Rendering AppNavHost")

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute == BottomNavItem.Home.route ||
            currentRoute == BottomNavItem.Map.route ||
            currentRoute == BottomNavItem.Day.route ||
            currentRoute == BottomNavItem.Hour.route

    // --- HOISTED STATES ---
    var showDayPicker by remember { mutableStateOf(false) }
    var dayButtonOffset by remember { mutableStateOf(Offset.Zero) }
    var isTimeSliderPanelVisible by remember { mutableStateOf(false) }

    // --- NEW HOISTED STATES for handling navigation and opening panels ---
    var pendingOpenDayPicker by remember { mutableStateOf(false) }
    var pendingOpenTimeSlider by remember { mutableStateOf(false) }
    var mapKey by remember { mutableStateOf(0) }
    // ----------------------

    // ADDED: System UI Controller usage
    val systemUiController = rememberSystemUiController()
    val crowdScopeThemeColor = Color(0xFF27504A)

    LaunchedEffect(systemUiController) {
        // Set status bar color to transparent
        systemUiController.setStatusBarColor(
            color = Color.Transparent, // Set to transparent
            darkIcons = true // Set to true for dark icons (visible on lighter backgrounds)
        )
        systemUiController.setNavigationBarColor(
            color = crowdScopeThemeColor,
            darkIcons = false
        )
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    navController = navController,
                    onDayButtonClick = { offset ->
                        dayButtonOffset = offset // Store the button's position
                        if (currentRoute != BottomNavItem.Map.route) {
                            pendingOpenDayPicker = true
                            navController.navigate(BottomNavItem.Map.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        } else {
                            showDayPicker = !showDayPicker
                            if (showDayPicker) {
                                isTimeSliderPanelVisible = false
                            }
                        }
                    },
                    onHourButtonClick = {
                        if (currentRoute != BottomNavItem.Map.route) {
                            pendingOpenTimeSlider = true
                            navController.navigate(BottomNavItem.Map.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        } else {
                            isTimeSliderPanelVisible = !isTimeSliderPanelVisible
                            if (isTimeSliderPanelVisible) {
                                showDayPicker = false
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(BottomNavItem.Home.route) {
                // LaunchedEffect to reset flags and increment mapKey when Home is composed
                LaunchedEffect(Unit) {
                    pendingOpenDayPicker = false
                    pendingOpenTimeSlider = false
                    isTimeSliderPanelVisible = false
                    showDayPicker = false
                    mapKey += 1
                }
                HomeScreen(
                    onExploreMapClick = {
                        navController.navigate(BottomNavItem.Map.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(BottomNavItem.Map.route) {
                LaunchedEffect(currentRoute) {
                    if (currentRoute != BottomNavItem.Map.route) {
                        showDayPicker = false
                        isTimeSliderPanelVisible = false
                    }
                }

                MapPage(
                    contentPadding = innerPadding,
                    onNavigateToHome = { // PASSED TO MAP PAGE
                        navController.navigate(BottomNavItem.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    showDayPicker = showDayPicker, // PASSED TO MAP PAGE
                    onDismissDayPicker = { showDayPicker = false }, // PASSED TO MAP PAGE
                    dayButtonOffset = dayButtonOffset, // PASSED TO MAP PAGE
                    isTimeSliderPanelVisible = isTimeSliderPanelVisible, // PASSED TO MAP PAGE
                    onTimeSliderVisibilityChange = { isVisible -> // PASSED TO MAP PAGE
                        isTimeSliderPanelVisible = isVisible
                        if (!isVisible) {
                            showDayPicker = false // Ensure day picker is hidden if time slider is dismissed
                        }
                    },
                    mapKey = mapKey, // PASSED TO MAP PAGE
                    onMapUIReady = { // NEW: Callback to signal when MapPage's UI is ready
                        // This block executes when MapPage's showFullUI becomes true
                        if (pendingOpenDayPicker) {
                            showDayPicker = true
                            pendingOpenDayPicker = false
                        }
                        if (pendingOpenTimeSlider) {
                            isTimeSliderPanelVisible = true
                            pendingOpenTimeSlider = false
                        }
                    }
                )
            }


            // These composable blocks only serve to redirect to Map.route
            composable(BottomNavItem.Day.route) {
                LaunchedEffect(Unit) {
                    pendingOpenDayPicker = true
                    navController.navigate(BottomNavItem.Map.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }

            composable(BottomNavItem.Hour.route) {
                LaunchedEffect(Unit) {
                    pendingOpenTimeSlider = true
                    navController.navigate(BottomNavItem.Map.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        }
    }
}
