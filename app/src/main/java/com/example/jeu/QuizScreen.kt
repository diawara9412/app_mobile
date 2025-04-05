package com.example.jeu

import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun QuizScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val imageDir = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
        "ObjectDetection"
    )

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var isAnswerCorrect by remember { mutableStateOf<Boolean?>(null) }
    var score by remember { mutableStateOf(0) }
    var showResult by remember { mutableStateOf(false) }
    var showFeedback by remember { mutableStateOf(false) }

    val primaryColor = Color(0xFFFFA500) // Orange color

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "Quiz",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            if (errorMessage != null) {
                ErrorMessage(errorMessage!!)
            } else if (isLoading) {
                LoadingIndicator(primaryColor)
            } else if (questions.isEmpty()) {
                EmptyQuizState()
            } else if (showResult) {
                QuizResults(score, questions.size, primaryColor) {
                    // Reset quiz
                    currentQuestionIndex = 0
                    selectedAnswer = null
                    isAnswerCorrect = null
                    score = 0
                    showResult = false
                    showFeedback = false
                }
            } else {
                // Progress indicator
                LinearProgressIndicator(
                    progress = (currentQuestionIndex + 1) / questions.size.toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = primaryColor,
                    trackColor = Color.LightGray.copy(alpha = 0.3f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Question ${currentQuestionIndex + 1}/${questions.size}",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )

                    Text(
                        text = "Score: $score",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Question card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Question text
                        Text(
                            text = questions[currentQuestionIndex].questionText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Question image
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .shadow(4.dp)
                        ) {
                            questions[currentQuestionIndex].imageUri?.let {
                                Image(
                                    painter = rememberAsyncImagePainter(it),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Answer options
                questions[currentQuestionIndex].options.forEach { option ->
                    AnswerOption(
                        text = option,
                        isSelected = selectedAnswer == option,
                        isCorrect = option == questions[currentQuestionIndex].correctAnswer,
                        showResult = showFeedback,
                        primaryColor = primaryColor,
                        onClick = {
                            if (!showFeedback) {
                                selectedAnswer = option
                                isAnswerCorrect = option == questions[currentQuestionIndex].correctAnswer
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (showFeedback) {
                        Button(
                            onClick = {
                                if (isAnswerCorrect == true) {
                                    score++
                                }

                                if (currentQuestionIndex < questions.lastIndex) {
                                    currentQuestionIndex++
                                    selectedAnswer = null
                                    isAnswerCorrect = null
                                    showFeedback = false
                                } else {
                                    showResult = true
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                        ) {
                            Text(
                                text = if (currentQuestionIndex < questions.lastIndex) "Question suivante" else "Voir le résultat",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                showFeedback = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            enabled = selectedAnswer != null
                        ) {
                            Text(
                                text = "Vérifier",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Feedback message
                AnimatedVisibility(
                    visible = showFeedback,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isAnswerCorrect == true) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isAnswerCorrect == true) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = null,
                                tint = if (isAnswerCorrect == true) Color(0xFF2E7D32) else Color(0xFFC62828),
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = if (isAnswerCorrect == true)
                                    "Bonne réponse !"
                                else
                                    "Mauvaise réponse. La bonne réponse est : ${questions[currentQuestionIndex].correctAnswer}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isAnswerCorrect == true) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                        }
                    }
                }
            }

            // Add space at the bottom for the navigation bar
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun ErrorMessage(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = Color(0xFFC62828),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                color = Color(0xFFC62828),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun LoadingIndicator(primaryColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = primaryColor,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Chargement du quiz...",
            fontSize = 16.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun EmptyQuizState() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.QuestionMark,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Impossible de créer un quiz",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Prenez des photos d'objets avec la caméra pour générer un quiz",
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun AnswerOption(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    showResult: Boolean,
    primaryColor: Color,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        !showResult -> if (isSelected) primaryColor.copy(alpha = 0.2f) else Color.White
        isCorrect -> Color(0xFFE8F5E9)
        isSelected -> Color(0xFFFFEBEE)
        else -> Color.White
    }

    val borderColor = when {
        !showResult && isSelected -> primaryColor
        showResult && isCorrect -> Color(0xFF2E7D32)
        showResult && isSelected && !isCorrect -> Color(0xFFC62828)
        else -> Color.LightGray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !showResult) { onClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showResult) {
                if (isCorrect) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(24.dp)
                    )
                } else if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = Color(0xFFC62828),
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color.LightGray, CircleShape)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .border(1.dp, if (isSelected) primaryColor else Color.LightGray, CircleShape)
                        .background(if (isSelected) primaryColor else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = if (isSelected || (showResult && isCorrect)) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun QuizResults(score: Int, totalQuestions: Int, primaryColor: Color, onRestart: () -> Unit) {
    val percentage = (score.toFloat() / totalQuestions) * 100

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Trophy image
            Image(
                painter = painterResource(id = R.drawable.trophy),
                contentDescription = "Trophée",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Result text
            Text(
                text = when {
                    percentage >= 80 -> "Excellent !"
                    percentage >= 60 -> "Très bien !"
                    percentage >= 40 -> "Bon travail !"
                    else -> "Continuez à pratiquer !"
                },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Score
            Text(
                text = "Votre score",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Text(
                text = "$score / $totalQuestions",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )

            // Percentage
            Box(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${percentage.toInt()}%",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        percentage >= 80 -> Color(0xFF2E7D32)
                        percentage >= 60 -> Color(0xFF1976D2)
                        percentage >= 40 -> Color(0xFFFFA000)
                        else -> Color(0xFFC62828)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Restart button
            Button(
                onClick = onRestart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Recommencer",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

data class Question(
    val questionText: String,
    val imageUri: Uri?,
    val options: List<String>,
    val correctAnswer: String
)

