package com.example.pract_3

import android.R.attr.path
import android.R.attr.start
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pract_3.databinding.MainFragmentBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.Date


class MainFragment: Fragment() {

    private val camera =
        ResultAppNavigator.Camera(::registerForActivityResult, ::registerForActivityResult)

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = MainFragmentBinding.inflate(layoutInflater)
        val letDirectory = File(requireContext().filesDir, "LET")
        val file = File(letDirectory, "Dates.txt")
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

        val networkDispatcher = newSingleThreadContext("Network")
        val diskDispatcher = newSingleThreadContext("Disk")

        binding.camera.setOnClickListener{ camera.launch() }
        binding.dateList.setOnClickListener {
                letDirectory.mkdirs()
                findNavController().navigate(R.id.listFragment)
        }

        camera.createLaunchers(requireContext()) { result ->
            if (result && camera.input != null) {
                file.appendText("${Date()} \n")
            }
        }

        binding.upload.setOnClickListener{
            var url = URL("https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.istockphoto.com%2Fvector%2Fno-cameras-allowed-sign-flat-icon-in-red-crossed-out-circle-vector-gm887464786-246302047&psig=AOvVaw2emVzoWIMRWk5b4xsKX7Xp&ust=1697667167752000&source=images&cd=vfe&opi=89978449&ved=0CBEQjRxqFwoTCNjI5pWN_oEDFQAAAAAdAAAAABAE")


            GlobalScope.launch(diskDispatcher){
                url =  URL(binding.photoUrl.text.toString().trim())
            }

            GlobalScope.launch(networkDispatcher) {
                url.openStream().use { input ->
                    FileOutputStream("${storageDir.absolutePath}/pract_6/${Date().time}.jpeg").use { output ->
                        input.copyTo(output)
                    }
                }
            }

            binding.photoUrl.text.clear()
        }

        binding.fileManager.setOnClickListener{
            startActivity(Intent(Intent.ACTION_PICK))
        }

        return binding.root
    }

}