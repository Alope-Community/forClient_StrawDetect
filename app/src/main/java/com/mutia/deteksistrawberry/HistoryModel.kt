package com.mutia.deteksistrawberry

import com.google.firebase.firestore.DocumentId

data class HistoryModel(
    @DocumentId
    val id: String = "",
    val namaPenyakit: String = "",
    val tanggal: String = "",
    val imageUrl: String = "",
    val confidence: Float = 0f,
    val boxLeft: Float = 0f,
    val boxTop: Float = 0f,
    val boxRight: Float = 0f,
    val boxBottom: Float = 0f
)
