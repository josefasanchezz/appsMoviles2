package com.example.activaciones

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.activaciones.ui.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppTheme { AppNav() } }
    }
}

@Composable
fun AppNav() {
    val nav = rememberNavController()
    val idArg = listOf(navArgument("id") { type = NavType.LongType })

    NavHost(nav, startDestination = "login") {
        composable("login") {
            LoginScreen { nav.navigate("list") { popUpTo("login") { inclusive = true } } }
        }
        composable("list") {
            ListScreen(
                onOpen = { nav.navigate("detail/$it") },
                onNew = { nav.navigate("form") },
                onCatalog = { nav.navigate("catalog") },
                onLogout = { nav.navigate("login") { popUpTo("list") { inclusive = true } } }
            )
        }
        composable("form") {
            FormScreen(onBack = { nav.popBackStack() }, onSaved = { id ->
                nav.navigate("detail/$id") { popUpTo("form") { inclusive = true } }
            })
        }
        composable("detail/{id}", idArg) { e ->
            val id = e.arguments!!.getLong("id")
            DetailScreen(id, { nav.popBackStack() }, { nav.navigate("activation/$id") }, { nav.navigate("result/$id") })
        }
        composable("activation/{id}", idArg) { e ->
            ActivationScreen(e.arguments!!.getLong("id")) { nav.popBackStack() }
        }
        composable("result/{id}", idArg) { e ->
            ResultScreen(e.arguments!!.getLong("id")) { nav.popBackStack() }
        }
        composable("catalog") { CatalogScreen { nav.popBackStack() } }
    }
}
