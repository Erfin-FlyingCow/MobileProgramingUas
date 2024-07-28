package com.example.projectuas

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileReader

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ListAdapter
    private lateinit var listlokasi: MutableList<ListHead>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = view.findViewById(R.id.Tempatview)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        listlokasi = readAllJsonFiles(requireContext()).toMutableList()
        adapter = ListAdapter(listlokasi, { selectedItem ->
            // Handle item click
            val bundle = Bundle().apply {
                putString("nama_lokasi", selectedItem.nama_lokasi)
                putString("deskripsi_lokasi", selectedItem.deskripsi_lokasi)
                putString("koordinat_lokasi", selectedItem.koordinat_lokasi)
            }
            val formLokasiFragment = FormLokasi().apply {
                arguments = bundle
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, formLokasiFragment)
                .addToBackStack(null)
                .commit()
        }, { position ->
            showDeleteConfirmationDialog(position)
        })
        recyclerView.adapter = adapter

        return view
    }

    private fun showDeleteConfirmationDialog(position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Lokasi")
            .setMessage("Apakah Anda yakin ingin menghapus lokasi ini?")
            .setPositiveButton("Hapus") { _, _ ->
                val removedItem = listlokasi[position]
                adapter.removeItem(position)
                updateJsonFilesAfterRemoval(removedItem)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateJsonFilesAfterRemoval(removedItem: ListHead) {
        val directory = requireContext().filesDir
        val files = directory.listFiles { file -> file.extension == "json" } ?: return

        for (file in files) {
            val fileName = file.name
            val fileData = readDataFromInternalStorage(requireContext(), fileName).toMutableList()

            if (fileData.contains(removedItem)) {
                fileData.remove(removedItem)
                saveDataToInternalStorage(requireContext(), fileName, fileData)
                break // Assuming the item is only in one file
            }
        }
    }

    private fun readAllJsonFiles(context: Context): List<ListHead> {
        val directory = context.filesDir
        val files = directory.listFiles { file -> file.extension == "json" } ?: return emptyList()
        val gson = Gson()
        val dataType = object : TypeToken<List<ListHead>>() {}.type
        val allData = mutableListOf<ListHead>()
        for (file in files) {
            val fileReader = FileReader(file)
            val data: List<ListHead> = gson.fromJson(fileReader, dataType)
            fileReader.close()
            allData.addAll(data)
        }

        return allData
    }

    private fun readDataFromInternalStorage(context: Context, fileName: String): List<ListHead> {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) {
            return emptyList()
        }

        val fileReader = FileReader(file)
        val gson = Gson()
        val dataType = object : TypeToken<List<ListHead>>() {}.type
        val data: List<ListHead> = gson.fromJson(fileReader, dataType)
        fileReader.close()

        return data
    }

    private fun saveDataToInternalStorage(context: Context, fileName: String, data: List<ListHead>) {
        val file = File(context.filesDir, fileName)
        val gson = Gson()
        file.writeText(gson.toJson(data))
    }
}


