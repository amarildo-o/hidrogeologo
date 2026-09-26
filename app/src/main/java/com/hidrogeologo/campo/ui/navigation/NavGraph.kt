package com.hidrogeologo.campo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hidrogeologo.campo.ui.detail.RecordDetailScreen
import com.hidrogeologo.campo.ui.form.RecordFormScreen
import com.hidrogeologo.campo.ui.list.RecordListScreen

@Composable
fun HidroCampoNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.RecordList.route) {
        composable(Screen.RecordList.route) {
            RecordListScreen(
                onAddRecord = { navController.navigate(Screen.RecordForm.createRoute()) },
                onOpenRecord = { id -> navController.navigate(Screen.RecordDetail.createRoute(id)) }
            )
        }

        composable(
            route = Screen.RecordForm.route,
            arguments = listOf(navArgument(Screen.RecordForm.ARG_RECORD_ID) { type = NavType.LongType })
        ) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getLong(Screen.RecordForm.ARG_RECORD_ID)
                ?: Screen.RecordForm.NEW_RECORD_ID
            RecordFormScreen(
                recordId = recordId,
                onBack = { navController.popBackStack() },
                onSaved = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.RecordDetail.route,
            arguments = listOf(navArgument(Screen.RecordDetail.ARG_RECORD_ID) { type = NavType.LongType })
        ) { backStackEntry ->
            val recordId = backStackEntry.arguments?.getLong(Screen.RecordDetail.ARG_RECORD_ID) ?: return@composable
            RecordDetailScreen(
                recordId = recordId,
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(Screen.RecordForm.createRoute(id)) },
                onDeleted = { navController.popBackStack() }
            )
        }
    }
}
