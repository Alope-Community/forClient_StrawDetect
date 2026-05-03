package com.mutia.deteksistrawberry

import com.google.firebase.firestore.DocumentId

data class HistoryModel(
    @DocumentId
    val id: String = "",
    val namaPenyakit: String = "",
    val tanggal: String = "",
    val imageUrl: String = "",
    val confidence: Float = 0f
)