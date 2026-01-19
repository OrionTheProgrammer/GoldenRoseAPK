package com.example.golden_rose_apk.Screens.cart

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.golden_rose_apk.ViewModel.AuthViewModel
import com.example.golden_rose_apk.ViewModel.CartViewModel
import com.example.golden_rose_apk.ViewModel.OrdersViewModel
import com.example.golden_rose_apk.data.ReceiptItem
import com.example.golden_rose_apk.model.Order
import com.example.golden_rose_apk.repository.LocalReceiptRepository
import com.example.golden_rose_apk.repository.LocalUserRepository
import com.example.golden_rose_apk.utils.formatPrice
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    navController: NavController,
    cartViewModel: CartViewModel,
    authViewModel: AuthViewModel,
    ordersViewModel: OrdersViewModel = viewModel()
) {
    val cartItems by cartViewModel.cartItems.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userRepository = remember { LocalUserRepository(context) }
    val receiptRepository = remember { LocalReceiptRepository(context) }
    var paymentMethod by remember { mutableStateOf("Tarjeta") }

    // Cálculos reales a partir del carrito
    val subtotal = cartItems.sumOf { (it.product.price ?: 0.0) * it.quantity }
    val commission = if (subtotal > 0) subtotal * 0.05 else 0.0
    val shipping = if (subtotal > 0) 5.0 else 0.0   // aquí ya bajaste el envío
    val total = subtotal + commission + shipping

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirmar Compra") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Resumen del Pedido", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    items(cartItems) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${item.product.name} (x${item.quantity})")
                            Text(
                                "$${((item.product.price ?: 0.0) * item.quantity).formatPrice()}",
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SummaryRow("Subtotal:", subtotal)
                    SummaryRow("Envío:", shipping)
                    SummaryRow("Comisión:", commission)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Método de pago:")
                        Text(paymentMethod, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total:", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "$${total.formatPrice()}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Método de pago", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Tarjeta", "Transferencia", "Efectivo").forEach { method ->
                    FilterChip(
                        selected = paymentMethod == method,
                        onClick = { paymentMethod = method },
                        label = { Text(method) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🔹 Botón Pagar con creación de orden, limpieza de carrito y navegación a boleta
            Button(
                onClick = {
                    scope.launch {
                        if (cartItems.isEmpty()) return@launch
                        val currentUser = userRepository.getCurrentUser()
                        if (currentUser == null) {
                            Toast.makeText(
                                context,
                                "Debes iniciar sesión para comprar",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@launch
                        }

                        ordersViewModel.createOrderFromCart(cartItems) { order: Order? ->
                            if (order != null) {
                                scope.launch {
                                    val items = cartItems.map {
                                        ReceiptItem(
                                            name = it.product.name,
                                            quantity = it.quantity,
                                            unitPrice = it.product.price
                                        )
                                    }
                                    val receiptId = receiptRepository.createReceipt(
                                        buyerName = currentUser.username,
                                        buyerEmail = currentUser.email,
                                        paymentMethod = paymentMethod,
                                        items = items,
                                        subtotal = subtotal,
                                        shipping = shipping,
                                        commission = commission,
                                        total = total,
                                        receiptId = order.id
                                    )

                                    // 2) Navegar a la boleta pasando el id
                                    navController.navigate("receipt/$receiptId") {
                                        // sacamos la pantalla de carrito de la pila
                                        popUpTo("cart") { inclusive = true }
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Error al crear la orden",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = cartItems.isNotEmpty()
            ) {
                Text("Pagar")
            }
        }
    }
}
