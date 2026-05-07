package com.mutia.deteksistrawberry

object DiseaseData {
    fun getInfo(name: String): Triple<String, String, String> {
        val normalizedName = name.replace("_", " ").lowercase()
        
        return when {
            normalizedName.contains("leaf spot") -> Triple(
                "Bercak ungu atau coklat bulat dengan titik tengah putih pada permukaan daun.",
                "Jamur Mycosphaerella fragariae yang terbawa air atau angin.",
                "Gunakan varietas tahan penyakit, bersihkan sisa tanaman, dan gunakan fungisida tembaga."
            )
            normalizedName.contains("leaf scorch") -> Triple(
                "Bercak ungu/merah tidak beraturan yang kemudian menyatu dan membuat daun tampak terbakar.",
                "Jamur Diplocarpon earliana yang berkembang pesat saat cuaca hangat dan lembab.",
                "Hancurkan daun yang terinfeksi, hindari penyiraman overhead, dan aplikasikan fungisida."
            )
            normalizedName.contains("leaf blight") -> Triple(
                "Bercak berbentuk V berwarna coklat di sepanjang tulang daun atau tepi daun.",
                "Jamur Phomopsis obscurans yang menyukai kelembapan tinggi.",
                "Pangkas daun yang terinfeksi dan pastikan sirkulasi udara yang baik antar tanaman."
            )
            normalizedName.contains("healthy") -> Triple(
                "Daun berwarna hijau segar, tidak ada bercak, dan tekstur normal.",
                "Lingkungan tumbuh yang ideal dengan nutrisi cukup.",
                "Pertahankan perawatan rutin, pemupukan seimbang, dan cek berkala."
            )
            normalizedName.contains("penyakit lain") -> Triple(
                "Terdeteksi adanya gejala penyakit namun tidak spesifik dalam kategori utama.",
                "Berbagai faktor patogen atau lingkungan.",
                "Lakukan pengamatan lebih lanjut dan pastikan kebersihan area tanam."
            )
            else -> Triple(
                "Informasi gejala tidak ditemukan.",
                "Penyebab belum dapat diidentifikasi secara pasti.",
                "Konsultasikan dengan ahli tanaman jika kondisi memburuk."
            )
        }
    }
}
