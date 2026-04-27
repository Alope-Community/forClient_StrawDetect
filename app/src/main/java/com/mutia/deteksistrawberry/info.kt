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
                R.drawable.foto_tentang
            ),
            Penyakit(
                "Leaf Blight",
                "Penyakit hawar daun yang menyebabkan daun mengering dan mati.",
                R.drawable.foto_tentang
            ),
            Penyakit(
                "Leaf Scorch",
                "Penyakit yang menyebabkan daun terlihat seperti terbakar.",
                R.drawable.foto_tentang
            ),
            Penyakit(
                "Healthy",
                "Tanaman dalam kondisi sehat tanpa gejala penyakit.",
                R.drawable.foto_tentang
            )
        )

        // 🔥 SET RECYCLER
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = PenyakitAdapter(data)

        return view
    }
}