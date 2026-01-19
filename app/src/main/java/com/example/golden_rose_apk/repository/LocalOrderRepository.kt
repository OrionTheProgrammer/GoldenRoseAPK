package com.example.golden_rose_apk.repository

import android.content.Context
import com.example.golden_rose_apk.model.Order
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.util.UUID

class LocalOrderRepository(private val context: Context) {
    private val gson = Gson()
    private val ordersFile = File(context.filesDir, "orders.json")

    fun saveOrder(order: Order): Order {
        val orders = loadOrders()
        val saved = order.copy(id = order.id.ifBlank { UUID.randomUUID().toString() })
        orders.add(saved)
        saveOrders(orders)
        return saved
    }

    fun getOrdersForUser(userId: String): List<Order> {
        return loadOrders()
            .filter { it.userId == userId }
            .sortedByDescending { it.createdAt }
    }

    private fun loadOrders(): MutableList<Order> {
        if (!ordersFile.exists()) return mutableListOf()
        val json = ordersFile.readText()
        if (json.isBlank()) return mutableListOf()
        val type = object : TypeToken<List<Order>>() {}.type
        return gson.fromJson<List<Order>>(json, type)?.toMutableList() ?: mutableListOf()
    }

    private fun saveOrders(orders: List<Order>) {
        ordersFile.writeText(gson.toJson(orders))
    }
}
