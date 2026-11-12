package com.talha.depremtakip.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.talha.depremtakip.ui.detail.DetailScreen
import com.talha.depremtakip.ui.list.ListScreen
import com.talha.depremtakip.ui.settings.SettingsScreen

object Routes {
    const val LIST = "list"
    const val DETAIL = "detail/{id}"
    const val SETTINGS = "settings"

    fun detail(id: String): String = "detail/${Uri.encode(id)}"
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.LIST) {
        composable(Routes.LIST) {
            ListScreen(
                onOpenDetail = { id -> navController.navigate(Routes.detail(id)) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            DetailScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
