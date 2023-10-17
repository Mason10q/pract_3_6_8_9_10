package com.example.pract_3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.example.pract_3.databinding.FragmentListBinding
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import android.R
import android.system.ErrnoException
import android.util.Log
import java.io.FileNotFoundException

class ListFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentListBinding.inflate(layoutInflater)

        val letDirectory = File(requireContext().filesDir, "LET")
        val file = File(letDirectory, "Dates.txt")

        val dateArray = ArrayList<String>()

        try {
            val inputStream = FileInputStream(file)
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                dateArray.add(line ?: "")
            }
        } catch (err: FileNotFoundException){
            dateArray.add("Список пуст")
            Log.d("main", "no file")
        } catch (err: ErrnoException){
            Log.d("main", err.message.toString())
        }

        val adapter = ArrayAdapter(requireContext(), R.layout.simple_list_item_1, dateArray)

        binding.list.adapter = adapter

        return binding.root
    }

}