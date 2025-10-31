package com.denoise.denoiseapp.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.denoise.denoiseapp.core.ui.theme.DenoiseTheme
import com.denoise.denoiseapp.presentation.report.DetailViewModel
import com.denoise.denoiseapp.presentation.report.FormViewModel
import com.denoise.denoiseapp.presentation.report.ListViewModel
import com.denoise.denoiseapp.ui.dashboard.DashboardScreen
import com.denoise.denoiseapp.ui.report.detail.ReportDetailScreen
import com.denoise.denoiseapp.ui.report.form.ReportFormScreen
import com.denoise.denoiseapp.ui.report.list.ReportListScreen
import com.denoise.denoiseapp.ui.settings.SettingsScreen

object Routes {
    const val DASHBOARD = "dashboard"
    const val LIST = "list"
    const val FORM = "form"
    const val DETAIL = "detail/{id}"
    const val SETTINGS = "settings"
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavGraph(modifier: Modifier = Modifier) {
    val nav = rememberNavController()

    // Oscuro por defecto (puedes cambiarlo desde Settings)
    var darkTheme by rememberSaveable { mutableStateOf(true) }

    DenoiseTheme(darkTheme = darkTheme) {

        // Ruta actual y visibilidad de bottom bar
        val currentRoute = nav.currentBackStackEntryAsState().value
            ?.destination?.route.normalize()
        val showBottomBar = currentRoute in setOf(Routes.LIST, Routes.DASHBOARD, Routes.SETTINGS)

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    val backRoute = nav.currentBackStackEntryAsState().value
                        ?.destination?.route.normalize()

                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        val items = listOf(
                            Triple(Routes.LIST, "Reportes", Icons.Outlined.ListAlt),
                            Triple(Routes.DASHBOARD, "Dashboard", Icons.Outlined.Assessment),
                            Triple(Routes.SETTINGS, "Ajustes", Icons.Outlined.Settings)
                        )
                        items.forEach { (route, _, icon) ->
                            val selected = when (route) {
                                Routes.DASHBOARD -> backRoute == Routes.DASHBOARD
                                Routes.SETTINGS  -> backRoute == Routes.SETTINGS
                                Routes.LIST      -> backRoute == Routes.LIST
                                else -> false
                            }
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (!selected) {
                                        nav.navigate(route) {
                                            popUpTo(nav.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null
                                    )
                                },
                                label = null, // minimalista
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onSurface,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                    indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(Modifier.padding(innerPadding)) {
                NavHost(
                    navController = nav,
                    startDestination = Routes.DASHBOARD,
                    modifier = modifier,
                    enterTransition = {
                        val from = initialState.destination.route.normalize()
                        val to = targetState.destination.route.normalize()
                        if (routeIndex(to) > routeIndex(from)) {
                            slideInHorizontally(animationSpec = tween(250)) { it }
                        } else {
                            slideInHorizontally(animationSpec = tween(250)) { -it }
                        }
                    },
                    exitTransition = {
                        val from = initialState.destination.route.normalize()
                        val to = targetState.destination.route.normalize()
                        if (routeIndex(to) > routeIndex(from)) {
                            slideOutHorizontally(animationSpec = tween(250)) { -it }
                        } else {
                            slideOutHorizontally(animationSpec = tween(250)) { it }
                        }
                    },
                    popEnterTransition = {
                        val from = initialState.destination.route.normalize()
                        val to = targetState.destination.route.normalize()
                        if (routeIndex(to) < routeIndex(from)) {
                            slideInHorizontally(animationSpec = tween(250)) { -it }
                        } else {
                            slideInHorizontally(animationSpec = tween(250)) { it }
                        }
                    },
                    popExitTransition = {
                        val from = initialState.destination.route.normalize()
                        val to = targetState.destination.route.normalize()
                        if (routeIndex(to) < routeIndex(from)) {
                            slideOutHorizontally(animationSpec = tween(250)) { it }
                        } else {
                            slideOutHorizontally(animationSpec = tween(250)) { -it }
                        }
                    }
                ) {
                    // ---- Dashboard ----
                    composable(Routes.DASHBOARD) {
                        DashboardScreen(
                            onIrALista = {
                                nav.navigate(Routes.LIST) {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onOpenSettings = {
                                nav.navigate(Routes.SETTINGS) {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onOpenDashboard = { /* no-op */ }
                        )
                    }

                    // ---- Lista ----
                    composable(Routes.LIST) {
                        val vm: ListViewModel = viewModel()
                        ReportListScreen(
                            vm = vm,
                            onAdd = { nav.navigate(Routes.FORM) },
                            onOpen = { id -> nav.navigate("detail/$id") },
                            onEdit = { id -> nav.navigate("form?id=$id") },
                            onOpenSettings = { nav.navigate(Routes.SETTINGS) }
                        )
                    }

                    // ---- Form (oculta bottom bar) ----
                    composable(
                        route = "${Routes.FORM}?id={id}",
                        arguments = listOf(
                            navArgument("id") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            }
                        )
                    ) { backStackEntry ->
                        val vm: FormViewModel = viewModel()
                        val id = backStackEntry.arguments?.getString("id")
                        if (id != null) vm.cargarParaEditar(id)

                        ReportFormScreen(
                            vm = vm,
                            onSaved = {
                                // volver a LIST sin duplicar
                                nav.popBackStack(Routes.LIST, inclusive = false)
                            }
                        )
                    }

                    // ---- Detail (oculta bottom bar) ----
                    composable(
                        Routes.DETAIL,
                        arguments = listOf(navArgument("id") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val vm: DetailViewModel = viewModel()
                        val id = backStackEntry.arguments?.getString("id")!!
                        vm.cargar(id)
                        ReportDetailScreen(
                            vm = vm,
                            onEdit = { nav.navigate("form?id=$id") },
                            onBack = { nav.popBackStack() }
                        )
                    }

                    // ---- Settings ----
                    composable(Routes.SETTINGS) {
                        SettingsScreen(
                            isDarkTheme = darkTheme,
                            onToggleTheme = { dark -> darkTheme = dark }
                        )
                    }
                }
            }
        }
    }
}

/* ---------------- Helpers ---------------- */

// Normaliza rutas (agrupa detail/form como LIST)
private fun String?.normalize(): String {
    val raw = this ?: return Routes.DASHBOARD
    val base = raw.substringBefore("?")
    return when {
        base.startsWith("detail") || base.startsWith("form") -> Routes.LIST
        else -> base
    }
}

// Índice posicional para dirección: List=0, Dashboard=1, Settings=2
private fun routeIndex(route: String): Int = when {
    route == Routes.LIST      -> 0
    route == Routes.DASHBOARD -> 1
    route == Routes.SETTINGS  -> 2
    route.startsWith("detail") || route.startsWith("form") -> 0
    else -> 1
}
