package com.example.jeu

import android.net.Uri
import android.os.Environment
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.pager.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.absoluteValue

data class CategoryItem(val imageResId: Int, val name: String)

@OptIn(ExperimentalPagerApi::class)
@Composable
fun AccueilScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val imageDir = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
        "ObjectDetection"
    )
    val imageFiles = imageDir.listFiles()?.filter {
        it.isFile && (it.extension == "jpg" || it.extension == "png")
    } ?: emptyList()

    // Liste d'images à afficher dans le carrousel
    val carouselImages = listOf(
        R.drawable.carou,
        R.drawable.objet,

    )

    val primaryColor = Color(0xFFFFA500) // Orange color

    Box(modifier = Modifier.fillMaxSize()) { // Conteneur principal prenant tout l'espace disponible

        if (carouselImages.isNotEmpty()) { // Si la liste d'images du carrousel n'est pas vide

            val pagerState = rememberPagerState() // État pour suivre la page actuelle du carrousel
            val coroutineScope = rememberCoroutineScope() // Portée pour lancer des coroutines

            LaunchedEffect(pagerState) { // Effet lancé à chaque changement d'état du carrousel
                while (true) {
                    delay(3000) // Attente de 3 secondes entre les défilements automatiques
                    val newPage = (pagerState.currentPage + 1) % carouselImages.size // Page suivante en boucle
                    pagerState.animateScrollToPage(newPage) // Défilement animé vers la nouvelle page
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp) // Hauteur fixe du carrousel
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .zIndex(1f) // Priorité d'affichage
            ){
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .shadow( // Ombre de la carte
                            elevation = 10.dp,
                            shape = RoundedCornerShape(20.dp),
                            spotColor = primaryColor.copy(alpha = 0.3f)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp)) // Coins arrondis
                    ){
                        // Composant de carrousel avec transformation de page
                        HorizontalPager(
                            count = carouselImages.size, // Nombre de pages
                            state = pagerState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 0.dp),
                        ) { page ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        val pageOffset = calculateCurrentOffsetForPage(page).absoluteValue
                                        lerp(
                                            start = 0.85f,
                                            stop = 1f,
                                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                        ).also { scale ->
                                            scaleX = scale
                                            scaleY = scale
                                        }
                                        alpha = lerp(
                                            start = 0.5f,
                                            stop = 1f,
                                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                        )
                                    }
                            ) {
                                Image(
                                    painter = painterResource(id = carouselImages[page]),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop // L’image remplit toute la carte
                                )
                            }
                        }

                        // Superposition dégradée pour améliorer la lisibilité
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.6f)
                                        ),
                                        startY = 300f,
                                        endY = 900f
                                    )
                                )
                        )

                        // Élément décoratif (icône en haut à gauche)
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(16.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(primaryColor.copy(alpha = 0.7f))
                                .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "📷",
                                fontSize = 18.sp,
                                color = Color.White
                            )
                        }

                        // Indicateurs de pages en bas du carrousel
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                        ) {
                            Card(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.Black.copy(alpha = 0.5f)
                                )
                            ) {
                                HorizontalPagerIndicator(
                                    pagerState = pagerState,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    activeColor = primaryColor,
                                    inactiveColor = Color.White.copy(alpha = 0.5f),
                                    indicatorWidth = 8.dp,
                                    indicatorHeight = 8.dp,
                                    spacing = 6.dp
                                )
                            }
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 220.dp) // Décalage pour laisser la place au carrousel
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState) // Scroll vertical
        ) {
            Spacer(modifier = Modifier.height(16.dp)) // Espacement

            // Titre "Catégories"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .height(30.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(primaryColor) // Barre décorative
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Catégories",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.Black
                )
            }

            val categoryList = listOf(
                CategoryItem(R.drawable.car1, "Transport"),
                CategoryItem(R.drawable.chaise, "Mobilier"),
                CategoryItem(R.drawable.animal, "animaux"),
                CategoryItem(R.drawable.vetement, "Vêtement"),
                CategoryItem(R.drawable.nouriture, "Nourriture")
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(categoryList) { category ->
                    EnhancedCategoryCard(category, primaryColor) // Affichage des catégories
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Titre "Images Capturées"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .height(30.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(primaryColor)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Images Capturées",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.Black
                )
            }

            // Vérifie si la liste d'images capturées est vide
            if (imageFiles.isEmpty()) {

                // Affiche une carte stylisée pour indiquer qu'aucune image n'a été capturée
                Card(
                    modifier = Modifier
                        .fillMaxWidth() // La carte prend toute la largeur
                        .height(200.dp) // Hauteur fixe de 200dp
                        .padding(vertical = 8.dp), // Espacement vertical
                    shape = RoundedCornerShape(20.dp), // Coins arrondis
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // Élévation (ombre)
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8)) // Couleur de fond gris clair
                ) {

                    // Contenu verticalement centré dans la carte
                    Column(
                        modifier = Modifier
                            .fillMaxSize() // Remplit toute la taille de la carte
                            .padding(16.dp), // Padding intérieur
                        horizontalAlignment = Alignment.CenterHorizontally, // Alignement horizontal centré
                        verticalArrangement = Arrangement.Center // Alignement vertical centré
                    ) {

                        // Création d'une animation infinie pour faire un effet de pulsation
                        val infiniteTransition = rememberInfiniteTransition()
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 0.9f, // Échelle initiale
                            targetValue = 1.1f, // Échelle maximale
                            animationSpec = infiniteRepeatable( // Répète à l’infini
                                animation = tween(1500), // Durée de 1.5s
                                repeatMode = RepeatMode.Reverse // Fait un va-et-vient
                            )
                        )

                        // Boîte contenant une icône avec effet d'animation de zoom
                        Box(
                            modifier = Modifier
                                .size(80.dp) // Taille du cercle
                                .graphicsLayer {
                                    scaleX = scale // Applique l’échelle animée sur l’axe X
                                    scaleY = scale // Applique l’échelle animée sur l’axe Y
                                }
                                .clip(CircleShape) // Forme circulaire
                                .background(primaryColor.copy(alpha = 0.1f)), // Fond coloré transparent
                            contentAlignment = Alignment.Center // Centre l’icône
                        ) {

                            // Icône représentant une image
                            Icon(
                                imageVector = Icons.Default.Image, // Icône image par défaut
                                contentDescription = null, // Pas de description car purement décoratif
                                modifier = Modifier.size(40.dp), // Taille de l’icône
                                tint = primaryColor // Couleur principale utilisée comme teinte
                            )
                        }

                        // Espacement entre l’icône et le texte
                        Spacer(modifier = Modifier.height(16.dp))

                        // Message principal : aucune image capturée
                        Text(
                            text = "Aucune image capturée", // Texte affiché
                            color = Color.DarkGray, // Couleur du texte
                            fontSize = 18.sp, // Taille du texte
                            fontWeight = FontWeight.Medium // Poids du texte
                        )

                        // Espacement entre les deux textes
                        Spacer(modifier = Modifier.height(8.dp))

                        // Message secondaire pour inviter à utiliser la caméra
                        Text(
                            text = "Utilisez la caméra pour détecter des objets", // Sous-texte d’aide
                            color = Color.Gray, // Couleur plus claire
                            fontSize = 14.sp, // Plus petite taille de texte
                            textAlign = TextAlign.Center, // Centrage du texte
                            modifier = Modifier.padding(horizontal = 32.dp) // Padding horizontal
                        )
                    }
                }
            }
            else {
                // Grille des images capturées
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2), // 2 colonnes
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .height((imageFiles.size * 100).coerceAtMost(400).dp) // Hauteur dynamique
                ) {
                    items(imageFiles) { file ->
                        EnhancedCapturedImageCard(file, primaryColor) // Carte d'image capturée
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp)) // Espace pour la barre de navigation
        }
    }

}

@Composable
fun EnhancedCategoryCard(category: CategoryItem, primaryColor: Color) {
    Card(
        modifier = Modifier
            .width(130.dp)
            .height(150.dp)
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Enhanced circular background with border
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(primaryColor.copy(alpha = 0.1f))
                    .border(2.dp, primaryColor.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = category.imageResId),
                    contentDescription = category.name,
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category name with background
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(primaryColor.copy(alpha = 0.1f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = category.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun EnhancedCapturedImageCard(file: File, primaryColor: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Image with rounded corners
            Image(
                painter = rememberAsyncImagePainter(Uri.fromFile(file)),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop
            )

            // Enhanced gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f)
                            ),
                            startY = 50f,
                            endY = 300f
                        )
                    )
            )

            // Object name with enhanced styling
            Card(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.6f)
                )
            ) {
                Text(
                    text = file.nameWithoutExtension.substringBeforeLast("_"),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            // Enhanced indicator dot
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(24.dp),
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = primaryColor)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(2.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.7f), CircleShape)
                )
            }
        }
    }
}

