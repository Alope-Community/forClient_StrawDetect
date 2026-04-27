package com.mutia.deteksistrawberry

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

// 🔥 TAMBAHKAN INI
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager

class History : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private val list = mutableListOf<HistoryModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        recyclerView = view.findViewById(R.id.recyclerHistory)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        list.add(HistoryModel("Leaf Spot", "28 Maret 2026", "", 95f))
        list.add(HistoryModel("Healthy", "27 Maret 2026", "", 99f))

        adapter = HistoryAdapter(list)
        recyclerView.adapter = adapter

        return view
    }
}