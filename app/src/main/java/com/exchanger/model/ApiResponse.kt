package com.exchanger.model


data class ApiResponse(
    val success: Boolean,
    val timestamp: Long,
    val base: String,
    val date: String,
    var rates: HashMap<String, Double> = HashMap()
)