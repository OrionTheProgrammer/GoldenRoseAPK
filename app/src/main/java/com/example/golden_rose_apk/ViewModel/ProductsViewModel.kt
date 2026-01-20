package com.example.golden_rose_apk.ViewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.golden_rose_apk.model.ProductFirestore
import com.example.golden_rose_apk.repository.LocalProductRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProductsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LocalProductRepository(application)

    private val _products = MutableStateFlow<List<ProductFirestore>>(emptyList())
    val products: StateFlow<List<ProductFirestore>> = _products

    private val TAG = "ProductsViewModel"

    init {
        loadProducts()
    }

    fun refresh() {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            val result = runCatching { repository.loadProducts() }
            result.onFailure { throwable ->
                Log.e(TAG, "Error cargando productos desde assets/products.json", throwable)
            }
            _products.value = result.getOrElse { emptyList() }
            Log.d(TAG, "Productos cargados: ${_products.value.size}")
        }
    }
}
