package com.playground.test_prep_cu.navigation

/**
 * Navigation routes for the app
 */
sealed class Screen(val route: String) {
    object Upload : Screen("upload")
    object Quiz : Screen("quiz")
    object Results : Screen("results")
    object Review : Screen("review")
}
