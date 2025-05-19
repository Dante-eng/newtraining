package com.example.newtraining.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.newtraining.ui.screens.*
import com.example.newtraining.ui.navigation.Screen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(navController)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.PlayerList.route) {
            PlayersListScreen(navController)
        }
        composable(Screen.AddPlayer.route) {
            AddPlayerScreen(navController)
        }
        composable(
            route = Screen.PlayerDetails.route,
            arguments = listOf(
                navArgument("playerId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val playerId = backStackEntry.arguments?.getInt("playerId") ?: return@composable
            PlayerDetailsScreen(
                navController = navController,
                playerId = playerId
            )
        }
        composable(Screen.Plans.route) {
            PlansScreen(navController)
        }
        composable(Screen.Workouts.route) {
            WorkoutsScreen(navController)
        }
        composable(
            route = Screen.AddWorkout.route,
            arguments = listOf(
                navArgument("groupId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            AddWorkoutScreen(
                navController = navController,
                muscleGroupId = backStackEntry.arguments?.getInt("groupId") ?: 0
            )
        }
        composable(Screen.Supplements.route) {
            SupplementsScreen(navController)
        }
        composable(Screen.SupplementsList.route) {
            SupplementsListScreen(navController)
        }
        composable(Screen.HormonesList.route) {
            HormonesListScreen(navController)
        }
        composable(Screen.Antibiotics.route) {
            AntibioticsScreen(navController)
        }
        composable(Screen.Vitamins.route) {
            VitaminsScreen(navController)
        }
        composable(Screen.DietPlan.route) {
            DietPlanScreen(navController)
        }
        composable(Screen.CaloriesCalculator.route) {
            CaloriesCalculatorScreen(navController)
        }
        composable(
            route = Screen.TrainingPlanDetails.route,
            arguments = listOf(
                navArgument("planId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getInt("planId") ?: return@composable
            TrainingPlanDetailsScreen(
                navController = navController,
                planId = planId
            )
        }
        composable(
            route = Screen.PlayerProgressAdd.route,
            arguments = listOf(
                navArgument("playerId") { type = NavType.IntType },
                navArgument("planId") { type = NavType.IntType; defaultValue = -1 }
            )
        ) { backStackEntry ->
            val playerId = backStackEntry.arguments?.getInt("playerId") ?: return@composable
            val planId = backStackEntry.arguments?.getInt("planId")?.takeIf { it != -1 }
            PlayerProgressAddScreen(
                navController = navController,
                playerId = playerId,
                planId = planId
            )
        }
        composable(Screen.Finance.route) {
            FinanceScreen(navController)
        }
        composable(Screen.ItemsManagement.route) {
            ItemsManagementScreen(navController)
        }
        composable(
            route = Screen.PlayerSalesHistory.route,
            arguments = listOf(
                navArgument("playerId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val playerId = backStackEntry.arguments?.getInt("playerId") ?: return@composable
            PlayerSalesHistoryScreen(navController, playerId)
        }
        composable("my_purchases") {
            MyPurchasesScreen(navController, adminPlayerId = 1)
        }
    }
} 