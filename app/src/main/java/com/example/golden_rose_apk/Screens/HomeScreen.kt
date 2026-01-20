package com.example.golden_rose_apk.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.golden_rose_apk.model.BottomNavItem
import com.example.golden_rose_apk.model.Category
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.example.golden_rose_apk.ViewModel.CartViewModel
import com.example.golden_rose_apk.ViewModel.ProductsViewModel
import com.example.golden_rose_apk.model.ProductFirestore
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    isGuest: Boolean,
    productsViewModel: ProductsViewModel,
    cartViewModel: CartViewModel
) {

    val products by productsViewModel.products.collectAsState()
    var searchText by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Categorías principales
    val categories = listOf(
        Category("Melee", Color(0xFF5649A5)),
        Category("Vandal", Color(0xFF4CAF50)),
        Category("Operator", Color(0xFF2196F3)),
        Category("Phantom", Color(0xFFFF9800))
    )


    // Barra de navegación inferior
    val navItems = listOf(
        BottomNavItem("Inicio", Icons.Filled.Home, "home"),
        BottomNavItem("Categorías", Icons.Filled.Category, "categories"),
        BottomNavItem("Blogs", Icons.AutoMirrored.Filled.Article, "blogs"),
        BottomNavItem("Perfil", Icons.Filled.Person, "perfil")
    )


    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(colorScheme.primary)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Barra de búsqueda
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        label = {
                            Text(
                                "¿Qué buscas?",
                                color = colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(45.dp),
                        shape = RoundedCornerShape(25.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorScheme.onPrimary,
                            unfocusedBorderColor = colorScheme.onPrimary.copy(alpha = 0.7f),
                            focusedLabelColor = colorScheme.onPrimary.copy(alpha = 0.9f),
                            unfocusedLabelColor = colorScheme.onPrimary.copy(alpha = 0.6f),
                            focusedTextColor = colorScheme.onPrimary,
                            unfocusedTextColor = colorScheme.onPrimary,
                            cursorColor = colorScheme.onPrimary,
                            focusedContainerColor = colorScheme.onPrimary.copy(alpha = 0.12f),
                            unfocusedContainerColor = colorScheme.onPrimary.copy(alpha = 0.08f)
                        ),
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Menu,
                                contentDescription = "Menú",
                                tint = colorScheme.onPrimary
                            )
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = "Buscar",
                                tint = colorScheme.onPrimary.copy(alpha = 0.8f),
                                modifier = Modifier.clickable {
                                    navController.navigate("search")
                                }
                            )
                        },
                        singleLine = true,
                        placeholder = {
                            Text(
                                "Buscar productos...",
                                color = colorScheme.onPrimary.copy(alpha = 0.5f)
                            )
                        }
                    )

                    // Botón de notificaciones
                    Box(
                        modifier = Modifier.size(42.dp)
                    ) {
                        IconButton(
                            onClick = { },
                            modifier = Modifier.size(42.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color.Transparent
                            )
                        ) {
                            Icon(
                                Icons.Filled.Notifications,
                                contentDescription = "Notificaciones",
                                tint = colorScheme.onPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Botón de favoritos (solo si no es invitado)
                    if (!isGuest) {
                        IconButton(
                            onClick = { navController.navigate("favorites") },
                            modifier = Modifier.size(42.dp),
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color.Transparent
                            )
                        ) {
                            Icon(
                                Icons.Filled.Favorite,
                                contentDescription = "Favoritos",
                                tint = colorScheme.onPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Botón de carrito
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clickable { navController.navigate("cart") }
                    ) {
                        Icon(
                            Icons.Filled.ShoppingCart,
                            contentDescription = "Carrito",
                            tint = colorScheme.onPrimary,
                            modifier = Modifier
                                .size(22.dp)
                                .align(Alignment.Center)
                        )
                    }
                }
            }
        },
        bottomBar = {
            HomeBottomNavigationBar(
                navController = navController,
                navItems = navItems
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Column {
                Text(
                    text = if (isGuest) "Bienvenido Invitado a Golden Rose" else "Bienvenido a Golden Rose",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )
            }

            // Categorías principales
            Text(
                text = "Categorías Destacadas",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { category ->
                    CategoryCard(category = category, navController = navController)
                }
            }

            // Lo más vendido
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lo más vendido",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onBackground
                )
                Text(
                    text = "Ver todo →",
                    fontSize = 14.sp,
                    color = colorScheme.primary,
                    modifier = Modifier.clickable { navController.navigate("categories") }
                )
            }

            if (products.isEmpty()) {
                Text(
                    "No hay productos disponibles",
                    color = colorScheme.onBackground
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.height(420.dp)
                ) {
                    items(products) { product ->
                        ProductCard(
                            product = product,
                            navController = navController,
                            cartViewModel = cartViewModel,
                            onAddToCart = {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        "${product.name} agregado al carrito"
                                    )
                                }
                            }
                        )
                    }
                }
            }

            // Grid de productos más vendidos (skins)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.height(400.dp)
            ) {
                items(products) { product ->
                    ProductCardFromSkin(
                        skin = product,
                        navController = navController,
                        cartViewModel = cartViewModel,
                        onAddToCart = {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "✅ ${product.name} agregada al carrito",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        },
                        onClick = {
                            navController.navigate("productDetail/${product.id}")
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// Componente de tarjeta de categoría
@Composable
fun CategoryCard(
    category: Category,
    navController: NavController
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .size(120.dp)
            .clickable { navController.navigate("categories") },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(category.color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Diamond,
                        contentDescription = category.name,
                        tint = category.color,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = category.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface
                )
            }
        }
    }
}

// Componente de tarjeta de producto
@Composable
fun ProductCard(
    product: ProductFirestore,
    navController: NavController,
    cartViewModel: CartViewModel,
    onAddToCart: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("productDetail/${product.id}") },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            AsyncImage(
                model = product.image,
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colorScheme.surfaceVariant),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = product.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "$${product.price}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    cartViewModel.addToCart(product)
                    onAddToCart()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Agregar")
            }
        }
    }
}

// Barra de navegación inferior ESPECÍFICA para HomeScreen
@Composable
fun HomeBottomNavigationBar(
    navController: NavController,
    navItems: List<BottomNavItem>
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val colorScheme = MaterialTheme.colorScheme

    NavigationBar(
        containerColor = colorScheme.surface
    ) {
        navItems.forEach { item ->
            val isSelected = currentRoute == item.route ||
                    (item.route == "home" && currentRoute?.startsWith("home/") == true)

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (item.route == "home") {
                        navController.navigate("home/false") {
                            launchSingleTop = true
                            popUpTo("home") { inclusive = true }
                        }
                    } else {
                        navController.navigate(item.route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        color = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant
                    )
                }
            )
        }
    }
}

// Componente reutilizable para badge de notificaciones
@Composable
fun NotificationBadge(count: Int, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier.size(40.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(40.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Transparent
            )
        ) {
            Icon(
                Icons.Outlined.Notifications,
                contentDescription = "Notificaciones",
                tint = colorScheme.onPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        if (count > 0) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(Color.Red, CircleShape)
                    .align(Alignment.TopEnd)
            ) {
                Text(
                    text = if (count > 9) "9+" else count.toString(),
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun ProductCardFromSkin(
    skin: ProductFirestore,
    navController: NavController,
    cartViewModel: CartViewModel,
    onAddToCart: () -> Unit,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            AsyncImage(
                model = skin.image,
                contentDescription = skin.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colorScheme.surfaceVariant),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = skin.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "$${skin.price}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    cartViewModel.addToCart(skin)
                    onAddToCart()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary
                )
            ) {
                Text("Agregar al carrito", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun CartBadge(count: Int, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme

    Box(modifier = Modifier.size(40.dp)) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(40.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Transparent
            )
        ) {
            Icon(
                Icons.Outlined.ShoppingCart,
                contentDescription = "Carrito",
                tint = colorScheme.onPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        if (count > 0) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(Color(0xFFFF9800), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (count > 9) "9+" else count.toString(),
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
