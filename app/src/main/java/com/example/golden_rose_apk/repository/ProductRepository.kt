package com.example.golden_rose_apk.repository

import android.content.Context
import com.example.golden_rose_apk.model.ProductFirestore

class ProductRepository(private val context: Context) {

    private val localRepository = LocalProductRepository(context)

    fun getProducts(
        onSuccess: (List<ProductFirestore>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        runCatching { localRepository.loadProducts() }
            .onSuccess(onSuccess)
            .onFailure { onError(Exception(it)) }
    }
}
