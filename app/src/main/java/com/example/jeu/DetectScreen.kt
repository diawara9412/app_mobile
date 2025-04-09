package com.example.jeu

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.example.jeu.translations.ObjectTranslations
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DetectScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var labelResult by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var recentDetections by remember { mutableStateOf<List<DetectionResult>>(emptyList()) }
    var showTips by remember { mutableStateOf(false) }
    var ttsInitialized by remember { mutableStateOf(false) }

    // Initialize Text-to-Speech
    var textToSpeech by remember { mutableStateOf<TextToSpeech?>(null) }

    // Initialiser TextToSpeech
    LaunchedEffect(Unit) {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsInitialized = true
                textToSpeech?.language = Locale.FRENCH
            } else {
                Toast.makeText(context, "Erreur d'initialisation de la synthèse vocale", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Clean up TTS when the composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        }
    }

    val primaryColor = Color(0xFFFFA500)

    LaunchedEffect(Unit) {
        val imageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "ObjectDetection"
        )

        if (imageDir.exists() && imageDir.isDirectory) {
            val files = imageDir.listFiles()?.filter {
                it.isFile && (it.extension == "jpg" || it.extension == "png")
            }?.sortedByDescending { it.lastModified() }?.take(5) ?: emptyList()

            recentDetections = files.map { file ->
                val name = file.nameWithoutExtension.substringBeforeLast("_")
                val timestamp = file.lastModified()
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val date = dateFormat.format(Date(timestamp))

                DetectionResult(
                    name = name,
                    imageUri = Uri.fromFile(file),
                    date = date
                )
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val safeUri = imageUri
        if (success && safeUri != null) {
            try {
                isLoading = true
                labelResult = "Analyse en cours..."

                val image = InputImage.fromFilePath(context, safeUri)
                val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
                labeler.process(image)
                    .addOnSuccessListener { labels ->
                        if (labels.isNotEmpty()) {
                            val englishLabel = labels[0].text
                            // Traduire le label en français en utilisant la classe ObjectTranslations
                            val frenchLabel = ObjectTranslations.translate(englishLabel)

                            CoroutineScope(Dispatchers.Main).launch {
                                delay(1500)
                                labelResult = "Objet détecté : $frenchLabel"
                                saveImageToStorage(context, safeUri, frenchLabel, textToSpeech, ttsInitialized)

                                // Lire à haute voix l'objet détecté
                                if (ttsInitialized && textToSpeech != null) {
                                    textToSpeech?.speak(
                                        "Objet détecté : $frenchLabel",
                                        TextToSpeech.QUEUE_FLUSH,
                                        null,
                                        "detection_id"
                                    )
                                }

                                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                val date = dateFormat.format(Date())
                                val newDetection = DetectionResult(
                                    name = frenchLabel,
                                    imageUri = safeUri,
                                    date = date
                                )
                                recentDetections = listOf(newDetection) + recentDetections.take(4)

                                isLoading = false
                            }
                        } else {
                            labelResult = "Aucun objet détecté."

                            // Lire le résultat
                            if (ttsInitialized && textToSpeech != null) {
                                textToSpeech?.speak(
                                    "Aucun objet détecté",
                                    TextToSpeech.QUEUE_FLUSH,
                                    null,
                                    "detection_id"
                                )
                            }

                            isLoading = false
                        }
                    }
                    .addOnFailureListener {
                        labelResult = "Erreur lors de la détection."
                        isLoading = false
                    }
            } catch (e: Exception) {
                labelResult = "Erreur de lecture de l'image."
                isLoading = false
            }
        } else {
            labelResult = "Échec de la prise de photo ou URI invalide."
            isLoading = false
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val photoFile = File.createTempFile("IMG_", ".jpg", context.cacheDir)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", photoFile)
            imageUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Permission caméra refusée", Toast.LENGTH_SHORT).show()
        }
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
            Text(
                text = "Détection d'Objets",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        if (isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = primaryColor)
                            }
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Camera,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.LightGray
                            )
                            Text(
                                text = "Aucune image",
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(
                visible = labelResult.isNotEmpty(),
                enter = fadeIn(animationSpec = tween(500)) + expandVertically(),
                exit = fadeOut(animationSpec = tween(500)) + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (labelResult.startsWith("Objet détecté"))
                            Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (labelResult.startsWith("Objet détecté")) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFFC62828),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = labelResult,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (labelResult.startsWith("Objet détecté"))
                                Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (recentDetections.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(24.dp)
                            .background(primaryColor)
                            .align(Alignment.CenterStart)
                    )

                    Text(
                        text = "Détections Récentes",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(recentDetections) { detection ->
                        RecentDetectionCard(detection, primaryColor) {
                            // Lire le nom de l'objet quand la carte est cliquée
                            if (ttsInitialized && textToSpeech != null) {
                                textToSpeech?.speak(
                                    "Objet détecté : ${detection.name}",
                                    TextToSpeech.QUEUE_FLUSH,
                                    null,
                                    "detection_id"
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTips = !showTips },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = "Conseils pour de meilleures détections",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Icon(
                            imageVector = if (showTips) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }

                    AnimatedVisibility(
                        visible = showTips,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            TipItem("Assurez-vous que l'objet est bien éclairé")
                            TipItem("Centrez l'objet dans le cadre")
                            TipItem("Évitez les arrière-plans trop chargés")
                            TipItem("Prenez la photo à une distance appropriée")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val infiniteTransition = rememberInfiniteTransition()
            val scale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000),
                    repeatMode = RepeatMode.Reverse
                )
            )

            Button(
                onClick = {
                    when (PackageManager.PERMISSION_GRANTED) {
                        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                            val photoFile = File.createTempFile("IMG_", ".jpg", context.cacheDir)
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", photoFile)
                            imageUri = uri
                            cameraLauncher.launch(uri)
                        }
                        else -> {
                            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .scale(scale),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Prendre une photo",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun TipItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFA500))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 14.sp, color = Color.DarkGray)
    }
}

@Composable
fun RecentDetectionCard(detection: DetectionResult, primaryColor: Color, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(detection.imageUri),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                ),
                                startY = 50f,
                                endY = 150f
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = detection.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = detection.date,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
        }
    }
}

data class DetectionResult(
    val name: String,
    val imageUri: Uri,
    val date: String
)

private fun saveImageToStorage(
    context: Context,
    uri: Uri,
    label: String,
    textToSpeech: TextToSpeech?,
    ttsInitialized: Boolean
) {
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "${label}_${System.currentTimeMillis()}.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ObjectDetection")
        }
    }

    val resolver = context.contentResolver
    val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    imageUri?.let {
        try {
            val inputStream = resolver.openInputStream(uri)
            val outputStream = resolver.openOutputStream(it)

            inputStream?.use { input ->
                outputStream?.use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            resolver.delete(it, null, null)
        }
    }
}
