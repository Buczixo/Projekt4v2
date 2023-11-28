package com.example.projekt4v2

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.projekt4v2.ui.theme.Projekt4v2Theme
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        val database = Firebase.database
        val myRef = database.getReference("NewData")

        myRef.setValue("databaseus")

        setContent {
            Projekt4v2Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyApp(database)
                }
            }
        }
    }

}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MyApp(database: FirebaseDatabase) {
    val navController = rememberNavController()
    val applicationContext = LocalContext.current.applicationContext
    NavGraph(navController = navController,
        applicationContext = applicationContext,
        database = database
    )

}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = "UserForm",
    applicationContext: Context,
    database: FirebaseDatabase
) {

    NavHost(navController = navController, startDestination = startDestination) {
        addScreens(navController, database)
    }
}

private fun NavGraphBuilder.addScreens(
    navController: NavHostController,
    database: FirebaseDatabase
) {
    composable("UserForm"){
        UserForm(database = database, navController = navController)
    }
    composable("UserScreen"){
        UserScreen(database = database)
    }
}

private fun submitData(name: String, age: String) {
    val database = Firebase.database
    val myRef = database.getReference("user")

    // Push data to Firebase Realtime Database
    val userId = myRef.push().key
    val user = mapOf("name" to name, "age" to age)
    if (userId != null) {
        myRef.child(userId).setValue(user)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun UserForm(
    database: FirebaseDatabase,
    navController: NavHostController
) {
    var username by remember { mutableStateOf("") }
    var wiek by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp)
    ) {
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 25.dp)
        )

        OutlinedTextField(
            value = wiek,
            onValueChange = { wiek = it },
            label = { Text("Wiek") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        Button(
            onClick = {
                submitData(username, wiek)
                keyboardController?.hide()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {

            Text("Wyślij",
                modifier = Modifier
                    .padding(14.dp))

            Icon(imageVector = Icons.Default.Check, contentDescription = null)
        }

        Button(
            onClick = {navController.navigate("UserScreen")},
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Show users")
        }
    }
}

@Composable
fun UserScreen(database: FirebaseDatabase) {
    var userList by remember { mutableStateOf(emptyList<User>()) }
    LaunchedEffect(key1 = true) {
        // Load users from Firebase Realtime Database
        val myRef = database.getReference("user")
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = snapshot.children.mapNotNull { it.getValue(User::class.java) }
                userList = users
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
    LazyColumn {
        items(userList) { user ->
            UserListItem(user)
            Divider(color = Color.Cyan, thickness = 1.dp)
        }
    }
}

@Composable
fun UserListItem(user: User) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = "Username: ${user.name}")
        Text(text = "Wiek: ${user.age}")
    }
}

data class User(val name: String = "", val age: String = "")