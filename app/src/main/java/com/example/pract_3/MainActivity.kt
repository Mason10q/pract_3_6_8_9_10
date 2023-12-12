package com.example.pract_3

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.pract_3.ui.MainLayout
import com.example.pract_3.ui.MyList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.util.Date


class MainActivity : AppCompatActivity() {

    private val camera =
        ResultAppNavigator.Camera(::registerForActivityResult, ::registerForActivityResult)

    private val letDirectory by lazy { File(this.filesDir, "LET") }
    private val file by lazy {
        File(letDirectory, "Dates.txt")
    }

    private val viewModel by lazy { ViewModelProvider(this)[MainViewModel::class.java] }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        setContent {
            val navController = rememberNavController()
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            val scope = rememberCoroutineScope()

            App(navController = navController, drawerState = drawerState, scope)
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

    override fun onDestroy() {
        val notificationWorkRequest = OneTimeWorkRequestBuilder<MyWorker>().build()
        WorkManager.getInstance(this).enqueue(notificationWorkRequest)
        super.onDestroy()
    }

    @Composable
    fun BottomBarProvider(navController: NavController) {
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

                Text(text = "BottomAppBar")

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
    fun App(navController: NavHostController, drawerState: DrawerState, scope: CoroutineScope) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                Column {
                    NavigationDrawerItem(label = { Text(text = "Главная") }, selected = false,  onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("mainFragment")
                    })
                    NavigationDrawerItem(label = { Text(text = "Даты") }, selected = false,  onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("listFragment")
                    })
                }
            }) {

            Scaffold(
                modifier = Modifier.fillMaxWidth(),
                topBar = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Green),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = navController.currentBackStackEntryAsState().value?.destination?.route.toString(),
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                },
                bottomBar = {
                    BottomBarProvider(navController)
                },

                ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = "mainFragment",
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable("mainFragment") {
                        MainLayout(camera, viewModel::upload, ::startActivity)
                    }
                    composable("listFragment") {
                        MyList(letDirectory, file)
                    }
                }
            }
        }
    }



}



