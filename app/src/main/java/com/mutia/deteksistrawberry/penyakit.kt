package com.mutia.deteksistrawberry

data class Penyakit(
    val nama: String,
    val deskripsi: String,
    val gejala: String,
    val penyebab: String,
    val penanganan: String,
    val gambar: Int,
    var isExpanded: Boolean = false
)