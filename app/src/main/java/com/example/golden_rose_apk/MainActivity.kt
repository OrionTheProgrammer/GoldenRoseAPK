package com.example.golden_rose_apk

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.golden_rose_apk.Screens.blogs.BlogScreen
import com.example.golden_rose_apk.Screens.categories.CategoriesScreen
import com.example.golden_rose_apk.Screens.HomeScreen
import com.example.golden_rose_apk.Screens.LoginScreen
import com.example.golden_rose_apk.Screens.perfil.PerfilScreen
import com.example.golden_rose_apk.Screens.RegisterScreen
import com.example.golden_rose_apk.Screens.WelcomeScreen
import com.example.golden_rose_apk.Screens.blogs.BlogDetailScreen
import com.example.golden_rose_apk.Screens.cart.CartScreen
import com.example.golden_rose_apk.Screens.cart.CheckoutScreen
import com.example.golden_rose_apk.Screens.cart.PaymentSuccessScreen
import com.example.golden_rose_apk.Screens.cart.ReceiptScreen
import com.example.golden_rose_apk.Screens.favorites.FavoritesScreen
import com.example.golden_rose_apk.Screens.orders.MyOrdersScreen
import com.example.golden_rose_apk.Screens.orders.OrderDetailScreen
import com.example.golden_rose_apk.Screens.orders.OrderHistoryScreen
import com.example.golden_rose_apk.Screens.perfil.EditarPefilScreen
import com.example.golden_rose_apk.Screens.products.ProductDetailScreen
import com.example.golden_rose_apk.Screens.search.SearchScreen
import com.example.golden_rose_apk.ViewModel.AuthViewModel
import com.example.golden_rose_apk.ViewModel.BlogViewModel
import com.example.golden_rose_apk.ViewModel.CartViewModel
import com.example.golden_rose_apk.ViewModel.OrdersViewModel
import com.example.golden_rose_apk.ViewModel.ProductsViewModel
import com.example.golden_rose_apk.ViewModel.SettingsViewModel
import com.example.golden_rose_apk.ViewModel.SettingsViewModelFactory
import com.example.golden_rose_apk.ViewModel.UserViewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Habilitar edge-to-edge (opcional pero recomendado para diseño moderno)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                101
            )
        }

        setContent {
            val authViewModel: AuthViewModel = viewModel()
            val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModelFactory(application))
            val currentTheme by settingsViewModel.appTheme.collectAsState()
            val isDark = currentTheme == "dark"
            val isLoggedIn by authViewModel.isLoggedIn.collectAsState()


            val userViewModel: UserViewModel = viewModel()
            val isDarkTheme = currentTheme == "dark"

            // Si ya hay usuario, entra directo a "home"
            val startDestination = if (isLoggedIn) "home" else "welcome"
            MaterialTheme(
                colorScheme = if (isDark) darkColorScheme() else lightColorScheme()
            ) {
                val cartViewModel: CartViewModel = viewModel()
                val navController = rememberNavController()
                val productsViewModel: ProductsViewModel = viewModel()
                NavHost(
                    navController = navController,
                    startDestination = startDestination

                ) {
                    composable("welcome") { WelcomeScreen(navController) }
                    composable("login") { LoginScreen(navController) }
                    composable("register") { RegisterScreen(navController) }

                    // Pantalla principal con parámetro opcional
                    composable("home") {
                        HomeScreen(
                            navController = navController,
                            isGuest = false,
                            productsViewModel = productsViewModel,
                            cartViewModel = cartViewModel
                        )
                    }

                    // Pantalla principal como Invitado
                    composable("home/{isGuest}") { backStackEntry ->
                        val isGuest = backStackEntry.arguments
                            ?.getString("isGuest")
                            ?.toBoolean() ?: false

                        HomeScreen(
                            navController = navController,
                            isGuest = isGuest,
                            productsViewModel = productsViewModel,
                            cartViewModel = cartViewModel
                        )
                    }

                    // TopBar
                    // Pantallas de la bottom navigation
                    composable("categories") {
                        CategoriesScreen(navController)
                    }
                    composable("blogs") { BlogScreen(navController) }
                    composable(
                        "blogDetail/{blogId}"
                    ) { backStackEntry ->
                        val blogId =
                            backStackEntry.arguments?.getString("blogId")?.toIntOrNull() ?: 0
                        val blogViewModel: BlogViewModel = viewModel()
                        BlogDetailScreen(navController, blogId, blogViewModel)
                    }

                    composable("perfil") {
                        PerfilScreen(
                            navController = navController,
                            settingsViewModel = settingsViewModel
                        )
                    }

                    composable("editarPerfil") {
                        EditarPefilScreen(
                            navController = navController,
                            userViewModel = userViewModel,
                            isDarkTheme = isDarkTheme,
                            onThemeChange = { enabled ->
                                // ✅ aquí llamamos a SettingsViewModel para cambiar el tema
                                settingsViewModel.setTheme(
                                    if (enabled) "dark" else "light"
                                )
                            }
                        )
                    }


                    composable("my_orders") {
                        val ordersViewModel: OrdersViewModel = viewModel()
                        MyOrdersScreen(
                            navController = navController,
                            ordersViewModel = ordersViewModel
                        )
                    }


                    // Pantalla de detalle de producto
                    composable("productDetail/{productId}") { backStackEntry ->
                        val productId = backStackEntry.arguments
                            ?.getString("productId")
                            ?: ""

                        ProductDetailScreen(
                            navController = navController,
                            productId = productId,
                            productsViewModel = productsViewModel,
                            cartViewModel = cartViewModel
                        )
                    }

                    composable("search") {
                        SearchScreen(navController = navController)
                    }

                    // ========== PANTALLAS DE FAVORITOS ==========
                    composable("favorites") { FavoritesScreen(navController) }

                    // ========== PANTALLAS DEL CARRITO Y CHECKOUT ==========
                    composable("cart") {
                        CartScreen(
                            navController = navController,
                            cartViewModel = cartViewModel
                        )
                    }


                    composable("checkout") {
                        CheckoutScreen(
                            navController = navController,
                            cartViewModel = cartViewModel,
                            authViewModel = authViewModel
                        )
                    }


                    composable("payment_success/{orderId}") { backStackEntry ->
                        val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                        PaymentSuccessScreen(
                            navController = navController,
                            orderId = orderId
                        )
                    }

                    composable(
                        route = "receipt/{total}",
                    ) { backStackEntry ->
                        val total = backStackEntry.arguments
                            ?.getString("total")
                            ?.toDoubleOrNull() ?: 0.0

                        ReceiptScreen(
                            navController = navController,
                            totalAmount = total,
                            cartViewModel = cartViewModel
                        )
                    }

                    composable("orderHistory") {
                        OrderHistoryScreen(
                            navController = navController
                        )
                    }

                    composable("orderDetail/{orderId}") { backStackEntry ->
                        val orderId = backStackEntry.arguments?.getString("orderId") ?: return@composable
                        OrderDetailScreen(
                            navController = navController,
                            orderId = orderId
                        )
                    }





                }
            }
        }
    }
}
