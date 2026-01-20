package com.example.golden_rose_apk.repository

import android.content.Context
import com.example.golden_rose_apk.model.ProductFirestore

class ProductRepository(private val context: Context) {

    private val localRepository = LocalProductRepository(context)

    suspend fun getProducts(): List<ProductFirestore> {
        return localRepository.loadProducts()
    }
}
