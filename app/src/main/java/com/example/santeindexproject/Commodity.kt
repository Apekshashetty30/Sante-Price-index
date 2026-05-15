package com.example.santeindexproject

data class Commodity(
    val name: String,
    val mandiPrice: Double, // Price per kg at Mandi
    val unit: String = "kg",
    val trend: Trend = Trend.STABLE
)

enum class Trend {
    UP, DOWN, STABLE
}
