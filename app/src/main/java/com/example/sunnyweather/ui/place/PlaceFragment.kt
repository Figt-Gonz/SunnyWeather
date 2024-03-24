package com.example.sunnyweather.ui.place

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sunnyweather.databinding.FragmentPlaceBinding

class PlaceFragment : Fragment() {
    private lateinit var binding: FragmentPlaceBinding
    private val viewModel by lazy { ViewModelProvider(this)[PlaceViewModel::class.java] }
    private lateinit var adapter: PlaceAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val layoutManager = LinearLayoutManager(activity)
        binding.apply {
            recyclerView.layoutManager = layoutManager
            adapter = PlaceAdapter(this@PlaceFragment, viewModel.placeList)
            recyclerView.adapter = adapter
            searchPlaceEdit.addTextChangedListener { editable ->
                val content = editable.toString()
                if (content.isNotEmpty()) {
                    viewModel.searchPlaces(content)
                } else {
                    recyclerView.visibility = View.GONE
                    bgImageView.visibility = View.VISIBLE
                    viewModel.placeList.clear()
                    adapter.notifyDataSetChanged()
                }
            }
            viewModel.placeLiveData.observe(viewLifecycleOwner) { result ->
                val places = result.getOrNull()
                if (places != null) {
                    recyclerView.visibility = View.VISIBLE
                    bgImageView.visibility = View.GONE
                    viewModel.placeList.clear()
                    viewModel.placeList.addAll(places)
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(activity, "未能查询到任何地点", Toast.LENGTH_SHORT).show()
                    result.exceptionOrNull()?.printStackTrace()
                }
            }

        }
    }
}