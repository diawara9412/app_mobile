package com.example.jeu

import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun QuizScreen() {
    val context = LocalContext.current
    val imageDir = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
        "ObjectDetection"
    )

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var score by remember { mutableStateOf(0) }
    var showResult by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    val orangeColor = Color(0xFFFFA500)

    val customColors = MaterialTheme.colorScheme.copy(
        primary = orangeColor,
        onPrimary = Color.White,
        surfaceVariant = Color.LightGray,
        onSurfaceVariant = Color.Black
    )

    MaterialTheme(colorScheme = customColors) {
        LaunchedEffect(Unit) {
            Log.d("QuizScreen", "Chargement du quiz...")
            delay(1000)

            if (!imageDir.exists() || !imageDir.isDirectory) {
                errorMessage = "Répertoire d'images non trouvé."
                Log.e("QuizScreen", "Répertoire introuvable : ${imageDir.absolutePath}")
            } else {
                val imageFiles = imageDir.listFiles()?.filter {
                    it.isFile && (it.extension == "jpg" || it.extension == "png")
                } ?: emptyList()

                if (imageFiles.isEmpty()) {
                    errorMessage = "Aucune image trouvée dans le dossier."
                } else {
                    questions = imageFiles.shuffled().take(10).mapNotNull { file ->
                        try {
                            val imageName = file.nameWithoutExtension.substringBeforeLast("_")
                            if (imageName == "pattern") return@mapNotNull null

                            val otherImages = imageFiles.filter { it != file }.shuffled().take(2)
                            val otherNames = mutableSetOf<String>()

                            for (img in otherImages) {
                                val name = img.nameWithoutExtension.substringBeforeLast("_")
                                if (name != "pattern") {
                                    otherNames.add(name)
                                }
                            }

                            if (otherNames.size < 2) return@mapNotNull null

                            val options = (otherNames + imageName).shuffled()
                            Question(
                                questionText = "Quel objet est sur cette image ?",
                                imageUri = Uri.fromFile(file),
                                options = options,
                                correctAnswer = imageName
                            )
                        } catch (e: Exception) {
                            Log.e("QuizScreen", "Erreur lors de la création d'une question : ${e.message}")
                            null
                        }
                    }
                }
            }
            isLoading = false
        }

        if (errorMessage != null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(errorMessage ?: "Erreur inconnue", color = MaterialTheme.colorScheme.error)
            }
        } else if (isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Chargement du quiz...")
            }
        } else if (questions.isNotEmpty()) {
            val currentQuestion = questions[currentQuestionIndex]

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(
                    progress = (currentQuestionIndex + 1) / questions.size.toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Question ${currentQuestionIndex + 1} / ${questions.size}",
                    fontSize = 18.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                currentQuestion.imageUri?.let {
                    Image(
                        painter = rememberAsyncImagePainter(it),
                        contentDescription = null,
                        modifier = Modifier
                            .size(250.dp)
                            .padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = currentQuestion.questionText,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Column {
                    currentQuestion.options.forEach { option ->
                        Button(
                            onClick = { selectedAnswer = option },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedAnswer == option) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(text = option, fontSize = 18.sp, color = Color.Black)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (selectedAnswer == currentQuestion.correctAnswer) {
                            score++
                        }
                        if (currentQuestionIndex < questions.lastIndex) {
                            currentQuestionIndex++
                            selectedAnswer = null
                        } else {
                            showResult = true
                            showDialog = true
                        }
                    },
                    enabled = selectedAnswer != null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(if (currentQuestionIndex < questions.lastIndex) "Question suivante" else "Voir le résultat")
                }

                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text("Résultat du Quiz", color = Color.Black) },
                        text = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.trophy),
                                    contentDescription = "Trophée",
                                    modifier = Modifier.size(150.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Votre score est : $score / ${questions.size}", color = Color.Black)
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = { showDialog = false },
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text("OK")
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }
}

data class Question(
    val questionText: String,
    val imageUri: Uri?,
    val options: List<String>,
    val correctAnswer: String)