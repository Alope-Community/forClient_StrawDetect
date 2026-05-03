package com.mutia.deteksistrawberry

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

// 🔥 TAMBAHKAN INI
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

import android.content.Intent
import android.graphics.BitmapFactory
import android.widget.ProgressBar
import android.widget.Toast

class History : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private lateinit var progressBar: ProgressBar
    private val list = mutableListOf<HistoryModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        progressBar = view.findViewById(R.id.progressBar)

        recyclerView = view.findViewById(R.id.recyclerHistory)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = HistoryAdapter(list, { model ->
            // Hapus dari Firebase dan Lokal
            val repository = FirebaseRepository()
            repository.deleteHistoryWithLocalImage(
                userId = "user_dummy",
                documentId = model.id,
                imagePath = model.imageUrl
            ) { success ->
                if (success) {
                    val index = list.indexOf(model)
                    if (index != -1) {
                        list.removeAt(index)
                        adapter.notifyItemRemoved(index)
                    }
                    Toast.makeText(requireContext(), "History dihapus", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Gagal menghapus history", Toast.LENGTH_SHORT).show()
                }
            }
        }) { model ->
            val info = DiseaseData.getInfo(model.namaPenyakit)
            val result = AnalysisResult(
                diseaseName = model.namaPenyakit,
                accuracy = model.confidence,
                date = model.tanggal,
                symptoms = info.first,
                cause = info.second,
                treatment = info.third,
                imageBitmap = model.imageUrl.let { path ->
                    BitmapFactory.decodeFile(path)
                },
                boundingBox = null
            )

            AnalysisDataHolder.analysisResult = result
            AnalysisDataHolder.isHistory = true

            val intent = Intent(requireContext(), DetailAnalisisActivity::class.java)
            startActivity(intent)
        }

        recyclerView.adapter = adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = FirebaseRepository()

        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        
        

        repository.getHistory("user_dummy") { data ->

            progressBar.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE

            list.clear()
            list.addAll(data)
            adapter.notifyDataSetChanged()
        }
    }
}
