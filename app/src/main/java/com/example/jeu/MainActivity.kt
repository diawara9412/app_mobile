package com.example.jeu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.jeu.ui.theme.JeuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JeuTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(
        topBar = { AppBar(navController) },
        bottomBar = { BottomBar(navController) }
    ) { innerPadding ->
        NavigationGraph(navController = navController, modifier = Modifier.padding(innerPadding))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val title = when (currentRoute) {
        "accueil" -> "Accueil"
        "detect" -> "Détection"
        "quiz" -> "Quiz"
        else -> "Jeu Éducatif"
    }

    TopAppBar(
        title = { Text(title) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
fun BottomBar(navController: NavHostController) {
    val items = listOf("accueil", "detect", "quiz")
    NavigationBar(
        containerColor = Color(0xFFFFA500), // Changer la couleur de fond en orange
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { screen ->
            val selected = currentRoute == screen
            val offsetY by animateDpAsState(if (selected) (-8).dp else 0.dp)

            val icon = when (screen) {
                "accueil" -> Icons.Filled.Home
                "detect" -> Icons.Filled.PhotoCamera
                "quiz" -> Icons.Filled.Quiz
                else -> Icons.Filled.Home
            }

            NavigationBarItem(
                selected = selected,
                onClick = { navController.navigate(screen) },
                icon = {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.offset(y = offsetY)
                    ) {
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White) // Changer la couleur de l'indicateur en blanc
                            )
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = screen,
                            tint = if (selected) Color(0xFFFFA500) else MaterialTheme.colorScheme.onSurfaceVariant // Changer la couleur de l'icône sélectionnée en orange
                        )
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFFFA500), // Changer la couleur de l'icône sélectionnée en orange
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = Color.Transparent // Rendre l'indicateur transparent
                )
            )
        }
    }
}

@Composable
fun NavigationGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = "accueil", modifier = modifier) {
        composable("accueil") { AccueilScreen() }
        composable("detect") { DetectScreen() }
        composable("quiz") { QuizScreen() }
    }
}