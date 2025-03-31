package com.example.jeu

import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalPagerApi::class)
@Composable
fun AccueilScreen() {
    val context = LocalContext.current
    val imageDir = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
        "ObjectDetection"
    )
    val imageFiles = imageDir.listFiles()?.filter {
        it.isFile && (it.extension == "jpg" || it.extension == "png")
    } ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // Carrousel d'images
        if (imageFiles.isNotEmpty()) {
            val pagerState = rememberPagerState()

            LaunchedEffect(pagerState) {
                while (true) {
                    delay(3000) // Défilement toutes les 3 secondes
                    val newPage = (pagerState.currentPage + 1) % imageFiles.size
                    pagerState.animateScrollToPage(newPage)
                }
            }

            HorizontalPager(
                count = imageFiles.size,
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp) // Réduire la hauteur à 150dp
            ) { page ->
                Image(
                    painter = rememberAsyncImagePainter(Uri.fromFile(imageFiles[page])),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        } else {
            // Afficher une image de substitution si le répertoire est vide
            Image(
                painter = painterResource(id = R.drawable.ic_no_images),
                contentDescription = "Aucune image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp) // Réduire la hauteur à 150dp
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bloc de catégories (carrousel horizontal)
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = "Catégories",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val categoryImages = listOf(
                R.drawable.ic_star,
                R.drawable.ic_star,
                R.drawable.ic_star,
                R.drawable.ic_star // Ajout d'une catégorie supplémentaire
            )
            val categoryNames = listOf("Nature", "Animaux", "Objets", "Nourriture")

            val categoryPagerState = rememberPagerState()
            HorizontalPager(
                count = categoryImages.size,
                state = categoryPagerState,
                modifier = Modifier.height(100.dp) // Ajuster la hauteur du carrousel
            ) { page ->
                CategoryCard(
                    imageResId = categoryImages[page],
                    categoryName = categoryNames[page]
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Titre du bloc des cartes d'images
        Text(
            text = "Images Capturées",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Images capturées en cartes
        if (imageFiles.isEmpty()) {
            // Afficher un message si le répertoire est vide
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Aucune image capturée pour le moment.")
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = painterResource(id = R.drawable.ic_no_images), // Remplacez par une image de substitution
                    contentDescription = "Aucune image",
                    modifier = Modifier.size(100.dp)
                )
            }
        } else {
            // Afficher les images si le répertoire n'est pas vide
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(imageFiles) { file ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                        Column {
                            Image(
                                painter = rememberAsyncImagePainter(Uri.fromFile(file)),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = file.nameWithoutExtension.substringBeforeLast("_"), // Nom de l'image
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryCard(imageResId: Int, categoryName: String) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val cardWidth = screenWidth / 4 // Calcul dynamique de la largeur

    Column(
        modifier = Modifier
            .width(cardWidth), // Suppression du padding horizontal
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = categoryName,
            modifier = Modifier.size(60.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = categoryName, fontSize = 12.sp)
    }
}