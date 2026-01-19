package com.example.golden_rose_apk.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.golden_rose_apk.model.Order
import com.example.golden_rose_apk.model.OrderItem
import com.example.golden_rose_apk.repository.LocalOrderRepository
import com.example.golden_rose_apk.repository.LocalUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.collections.emptyList

class OrdersViewModel(application: Application) : AndroidViewModel(application) {

    private val orderRepository = LocalOrderRepository(application)
    private val userRepository = LocalUserRepository(application)

    // ====== STATE: LISTA DE MIS ÓRDENES ======
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders

    // ====== CREAR ORDEN A PARTIR DEL CARRITO ======
    fun createOrderFromCart(
        cartItems: List<CartItem>,
        onResult: (Order?) -> Unit
    ) {
        val userId = userRepository.getCurrentUserId().orEmpty()
        if (userId.isBlank()) {
            onResult(null)
            return
        }

        // 1) Pasar carrito -> lista de OrderItem
        val items = cartItems.map { cartItem ->
            OrderItem(
                productName = cartItem.product.name.ifBlank { "Sin nombre" },
                quantity = cartItem.quantity,
                price = cartItem.product.price
            )
        }

        val total = items.sumOf { it.price * it.quantity }

        // 2) Crear Order sin id
        val order = Order(
            userId = userId,
            items = items,
            total = total
        )

        val savedOrder = orderRepository.saveOrder(order)
        if (userId.isNotBlank()) {
            _orders.value = orderRepository.getOrdersForUser(userId)
        }
        onResult(savedOrder)
    }

    // ====== ESCUCHAR MIS ÓRDENES (HISTORIAL) ======
    fun listenMyOrders() {
        val userId = userRepository.getCurrentUserId() ?: return
        _orders.value = orderRepository.getOrdersForUser(userId)
    }
}
