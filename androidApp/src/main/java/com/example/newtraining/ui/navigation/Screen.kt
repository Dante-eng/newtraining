package com.example.newtraining.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object PlayerList : Screen("playerList")
    object AddPlayer : Screen("add_player")
    object PlayerDetails : Screen("player_details/{playerId}") {
        fun createRoute(playerId: Int) = "player_details/$playerId"
    }
    object Plans : Screen("plans")
    object Workouts : Screen("workouts")
    object AddWorkout : Screen("add_workout/{groupId}") {
        fun createRoute(groupId: Int) = "add_workout/$groupId"
    }
    object Supplements : Screen("supplements")
    object SupplementsList : Screen("supplements_list")
    object HormonesList : Screen("hormones_list")
    object Antibiotics : Screen("antibiotics")
    object Vitamins : Screen("vitamins")
    object DietPlan : Screen("diet_plan")
    object CaloriesCalculator : Screen("calories_calculator")
    object PlayerProgressAdd : Screen("player_progress_add/{playerId}") {
        fun createRoute(playerId: Int) = "player_progress_add/$playerId"
    }
    object Finance : Screen("finance")
    object ItemsManagement : Screen("items_management")
    object PlayerSalesHistory : Screen("player_sales_history/{playerId}") {
        fun createRoute(playerId: Int) = "player_sales_history/$playerId"
    }
    object Login : Screen("login")
    object TrainingPlanDetails : Screen("training_plan/{planId}") {
        fun createRoute(planId: Int) = "training_plan/$planId"
    }
} 