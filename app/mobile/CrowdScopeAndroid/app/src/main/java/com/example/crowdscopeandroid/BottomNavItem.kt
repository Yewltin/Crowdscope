//provides a type-safe and organized way to define the data model for each item in your bottom navigation bar, making your navigation code cleaner,
// less error-prone, and easier to manage.

package com.example.crowdscopeandroid

sealed class BottomNavItem(
    val route: String,
    val label: String
    // Removed @DrawableRes val icon: Int
) {
    data object Home : BottomNavItem("home", "Home")
    data object Map : BottomNavItem("map", "Map")
    data object Day : BottomNavItem("day", "Day")
    data object Hour : BottomNavItem("hour", "Hour")
}