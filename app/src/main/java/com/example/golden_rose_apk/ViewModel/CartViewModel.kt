package com.example.golden_rose_apk.ViewModel


import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.golden_rose_apk.model.ProductFirestore
import com.example.golden_rose_apk.model.Order
import com.example.golden_rose_apk.model.OrderItem
import com.example.golden_rose_apk.repository.LocalOrderRepository
import com.example.golden_rose_apk.repository.LocalUserRepository
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.content.Context
import com.google.gson.Gson


data class CartItem(
    val product: ProductFirestore,
    val quantity: Int
)

class CartViewModel(application: Application) : AndroidViewModel(application) {
    private val orderRepository = LocalOrderRepository(application)
    private val userRepository = LocalUserRepository(application)

    private val prefs = application.getSharedPreferences("cart_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems

    private val _checkoutState = MutableStateFlow<CheckoutState>(CheckoutState.Idle)
    val checkoutState: StateFlow<CheckoutState> = _checkoutState

    init {
        loadCart()   // 👈 al crear el viewmodel, carga lo guardado
    }

    // Agregar producto desde el catálogo local
    fun addToCart(product: ProductFirestore) {
        viewModelScope.launch {
            val current = _cartItems.value.toMutableList()
            val index = current.indexOfFirst { it.product.id == product.id }

            if (index >= 0) {
                val item = current[index]
                current[index] = item.copy(quantity = item.quantity + 1)
            } else {
                current.add(CartItem(product, 1))
            }

            _cartItems.value = current
            saveCart()
        }
    }

    fun removeFromCart(productId: String) {
        _cartItems.value = _cartItems.value.filterNot {
            it.product.id == productId
        }
        saveCart()
    }

    fun updateQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(productId)
            return
        }

        _cartItems.value = _cartItems.value.map {
            if (it.product.id == productId) it.copy(quantity = quantity)
            else it
        }
        saveCart()
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        saveCart()
    }

    // ================== PERSISTENCIA ==================

    private fun saveCart() {
        viewModelScope.launch {
            try {
                val json = gson.toJson(_cartItems.value)
                prefs.edit().putString("CART_JSON", json).apply()
            } catch (e: Exception) {
                Log.e("CartViewModel", "Error guardando carrito", e)
            }
        }
    }

    private fun loadCart() {
        try {
            val json = prefs.getString("CART_JSON", null) ?: return
            val type = object : TypeToken<List<CartItem>>() {}.type
            val list: List<CartItem>? = gson.fromJson(json, type)
            _cartItems.value = list ?: emptyList()
        } catch (e: Exception) {
            Log.e("CartViewModel", "Error cargando carrito", e)
            _cartItems.value = emptyList()
        }
    }

    fun checkout(total: Double, userId: String?, token: String?) {
        viewModelScope.launch {
            if (_cartItems.value.isEmpty()) return@launch

            val resolvedUserId = userId?.takeIf { it.isNotBlank() }
                ?: userRepository.getCurrentUserId()

            if (resolvedUserId.isNullOrBlank()) {
                _checkoutState.value = CheckoutState.Error("Debes iniciar sesión.")
                return@launch
            }

            _checkoutState.value = CheckoutState.Loading

            try {
                val items = _cartItems.value.map {
                    OrderItem(
                        productName = it.product.name,
                        quantity = it.quantity,
                        price = it.product.price
                    )
                }

                val order = Order(
                    userId = resolvedUserId,
                    items = items,
                    total = total
                )

                val orderResponse = orderRepository.saveOrder(order)

                _checkoutState.value = CheckoutState.Success(
                    orderId = orderResponse.id,
                    paymentId = "LOCAL-${orderResponse.id}",
                    paymentLink = null
                )
            } catch (e: Exception) {
                Log.e("CartViewModel", "Checkout error", e)
                _checkoutState.value =
                    CheckoutState.Error("Error procesando el pago.")
            }
        }
    }

    fun resetCheckoutState() {
        _checkoutState.value = CheckoutState.Idle
    }
}


class CartViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CartViewModel(application) as T
    }
}

sealed class CheckoutState {
    object Idle : CheckoutState()
    object Loading : CheckoutState()

    data class Success(
        val orderId: String?,
        val paymentId: String?,
        val paymentLink: String?
    ) : CheckoutState()

    data class Error(val message: String) : CheckoutState()
}
