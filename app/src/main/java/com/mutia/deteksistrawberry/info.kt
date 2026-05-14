package com.mutia.deteksistrawberry

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class info : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_info, container, false)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerInfo)

        // 🔥 DATA PENYAKIT
        val data = listOf(

            Penyakit(
                "Leaf Spot",
                "Penyakit bercak daun yang disebabkan oleh jamur Mycosphaerella fragariae.",

                "• Muncul bercak kecil berwarna ungu pada daun.",

                "• Disebabkan oleh jamur Mycosphaerella fragariae.",

                "• Gunakan fungisida dan buang daun yang terinfeksi.",

                R.drawable.gambar_spot
            ),

            Penyakit(
                "Leaf Blight",
                "Penyakit hawar daun yang menyebabkan daun mengering dan mati.",

                "• Daun berubah coklat dan mengering.",

                "• Disebabkan oleh infeksi jamur pada daun.",

                "• Pangkas daun yang terinfeksi dan gunakan fungisida.",

                R.drawable.gambar_blight
            ),

            Penyakit(
                "Leaf Scorch",
                "Penyakit yang menyebabkan daun terlihat seperti terbakar.",

                "• Tepi daun menghitam seperti terbakar.",

                "• Disebabkan oleh kondisi lingkungan dan infeksi penyakit.",

                "• Jaga kelembapan tanaman dan lakukan perawatan rutin.",

                R.drawable.gambar_scorch
            ),

            Penyakit(
                "Healthy",
                "Tanaman dalam kondisi sehat tanpa gejala penyakit.",

                "• Daun berwarna hijau segar tanpa bercak.",

                "• Tidak terdapat infeksi penyakit pada tanaman.",

                "• Lakukan perawatan dan penyiraman secara rutin.",

                R.drawable.gambar_sehat
            )
        )

        // 🔥 SET RECYCLER
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = PenyakitAdapter(data)

        return view
    }
}