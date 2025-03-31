package com.example.jeu

import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
                    .height(150.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) { page ->
                Image(
                    painter = rememberAsyncImagePainter(Uri.fromFile(imageFiles[page])),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
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
                    .height(150.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Carrousel des catégories sous forme de liste horizontale
        Text(
            text = "Catégories",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val categoryList = listOf(
            CategoryItem(R.drawable.car1, "Voiture"),
            CategoryItem(R.drawable.chaise, "Chaise"),
            CategoryItem(R.drawable.ic_puma, "Puma"),
            CategoryItem(R.drawable.ic_skechers, "Skechers"),
            CategoryItem(R.drawable.ic_reebok, "Reebok")
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categoryList.size) { index ->
                CategoryCard(categoryList[index])
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Titre du bloc des images capturées
        Text(
            text = "Images Capturées",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (imageFiles.isEmpty()) {
            // Affichage d'un message si aucune image n'est capturée
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Aucune image capturée pour le moment.")
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = painterResource(id = R.drawable.ic_no_images),
                    contentDescription = "Aucune image",
                    modifier = Modifier.size(100.dp)
                )
            }
        } else {
            // Affichage des images capturées
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(imageFiles) { file ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
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
                                text = file.nameWithoutExtension.substringBeforeLast("_"),
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
fun CategoryCard(category: CategoryItem) {
    Card(
        modifier = Modifier
            .width(100.dp)
            .padding(4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = category.imageResId),
                contentDescription = category.name,
                modifier = Modifier.size(60.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = category.name, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

data class CategoryItem(val imageResId: Int, val name: String)