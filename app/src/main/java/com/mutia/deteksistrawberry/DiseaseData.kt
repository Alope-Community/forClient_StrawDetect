package com.mutia.deteksistrawberry

object DiseaseData {
    fun getInfo(name: String): Triple<String, String, String> {
        return when (name) {
            "Leaf Spot" -> Triple(
                "Bercak ungu atau coklat bulat dengan titik tengah putih pada permukaan daun.",
                "Jamur Mycosphaerella fragariae yang terbawa air atau angin.",
                "Gunakan varietas tahan penyakit, bersihkan sisa tanaman, dan gunakan fungisida tembaga."
            )
            "Leaf Scorch" -> Triple(
                "Bercak ungu/merah tidak beraturan yang kemudian menyatu dan membuat daun tampak terbakar.",
                "Jamur Diplocarpon earliana yang berkembang pesat saat cuaca hangat dan lembab.",
                "Hancurkan daun yang terinfeksi, hindari penyiraman overhead, dan aplikasikan fungisida."
            )
            "Leaf Blight" -> Triple(
                "Bercak berbentuk V berwarna coklat di sepanjang tulang daun atau tepi daun.",
                "Jamur Phomopsis obscurans yang menyukai kelembapan tinggi.",
                "Pangkas daun yang terinfeksi dan pastikan sirkulasi udara yang baik antar tanaman."
            )
            "Healthy" -> Triple(
                "Daun berwarna hijau segar, tidak ada bercak, dan tekstur normal.",
                "Lingkungan tumbuh yang ideal dengan nutrisi cukup.",
                "Pertahankan perawatan rutin, pemupukan seimbang, dan cek berkala."
            )
            else -> Triple("-", "-", "-")
        }
    }
}
