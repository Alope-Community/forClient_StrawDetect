package com.mutia.deteksistrawberry

data class HistoryModel(
    val namaPenyakit: String = "",
    val tanggal: String = "",
    val imageUrl: String = "",
    val confidence: Float = 0f
)