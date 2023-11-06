package com.example.pract_3

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.system.ErrnoException
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ReportFragment.Companion.reportFragment
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pract_3.ui.Pink40
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL
import java.util.Date


class MainActivity : AppCompatActivity() {

    private val camera =
        ResultAppNavigator.Camera(::registerForActivityResult, ::registerForActivityResult)

    private val letDirectory by lazy { File(this.filesDir, "LET") }
    private val file by lazy {
        File(letDirectory, "Dates.txt")
    }
    private val storageDir =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)


    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!letDirectory.exists()) {
            letDirectory.mkdirs()
        }

        if (!file.exists()) {
            try {
                file.createNewFile()
            } catch (e: IOException) {
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            }
        }

        setContent {
            val navController = rememberNavController()

            Scaffold(
                topBar = {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Green)){
                        Text(text = navController.currentBackStackEntryAsState().value?.destination?.route.toString())
                    }
                },
                bottomBar = { BottomBarProvider(navController) }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = "mainFragment",
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable("mainFragment") {
                        MainLayout()
                    }
                    composable("listFragment") {
                        MyList()
                    }
                }
            }
        }



        camera.createLaunchers(this) { result ->
            if (result && camera.input != null) {
                try {
                    file.appendText("${Date()} \n")
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    @Composable
    fun BottomBarProvider(navController: NavController){
        BottomAppBar() {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                IconButton(onClick = { navController.navigate("mainFragment") }) {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = "Главная"
                    )
                }
                Spacer(Modifier.weight(1f, true))

                IconButton(onClick = { navController.navigate("listFragment") }) {
                    Icon(
                        imageVector = Icons.Filled.List,
                        contentDescription = "Список дат"
                    )
                }
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainLayout() {
        val inputvalue = remember { mutableStateOf(TextFieldValue()) }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {


            Card {
                Button(
                    onClick = { camera.launch() },
                    modifier = Modifier
                        .size(100.dp)
                        .padding(8.dp)
                ) {
                    Text(text = "Камера")
                }

            }

            Box(Modifier.background(Pink40)) {
                TextField(
                    value = inputvalue.value,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(8.dp),
                    onValueChange = { inputvalue.value = it },
                    placeholder = { Text(text = "Url") }
                )
            }


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                Button(
                    onClick = {
                        upload(inputvalue.value.text)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .weight(1f)
                ) {
                    Text(text = "Загрузить")
                }

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_PICK)
                        val path = Environment.getExternalStorageDirectory().absolutePath + "/Pictures/pract_6/"
                        val uri = Uri.parse(path)
                        intent.setDataAndType(uri, "*/*")
                        startActivity(intent)
                      },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .weight(1f)
                ) {
                    Text(text = "Открыть папку")
                }

            }
        }
    }

    @Composable
    fun MyList() {

        val dateArray = ArrayList<String>()
        letDirectory.mkdirs()

        try {
            val inputStream = FileInputStream(file)
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                dateArray.add(line ?: "")
            }
        } catch (err: FileNotFoundException) {
            dateArray.add("Список пуст")
            Log.d("main", "no file")
        } catch (err: ErrnoException) {
            Log.d("main", err.message.toString())
        }

        LazyColumn() {
            items(dateArray.size) { i ->
                Text(text = dateArray[i])
            }
        }
    }


    @OptIn(DelicateCoroutinesApi::class)
    private fun upload(strUrl: String) {
        val networkDispatcher = newSingleThreadContext("Network")
        val diskDispatcher = newSingleThreadContext("Disk")

        var url = URL("https://")


        GlobalScope.launch(diskDispatcher) {
            url = URL(strUrl.trim())
        }

        GlobalScope.launch(networkDispatcher) {
            withContext(Dispatchers.IO) {
                url.openStream()
            }.use { input ->
                FileOutputStream("${storageDir.absolutePath}/pract_6/${Date().time}.jpg").use { output ->
                    input.copyTo(output)
                }
            }
        }
    }
}



