package com.example.jeu

import android.net.Uri
import android.os.Environment
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

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

    Box(modifier = Modifier.fillMaxSize()) {
        // Fixed carousel at the top
        if (carouselImages.isNotEmpty()) {
            val pagerState = rememberPagerState()
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(pagerState) {
                while (true) {
                    delay(3000) // Défilement toutes les 3 secondes
                    val newPage = (pagerState.currentPage + 1) % carouselImages.size
                    pagerState.animateScrollToPage(newPage)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .zIndex(1f)
            ) {
                HorizontalPager(
                    count = carouselImages.size,
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    Image(
                        painter = painterResource(id = carouselImages[page]),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Gradient overlay for better text visibility
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

                // Page indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    HorizontalPagerIndicator(
                        pagerState = pagerState,
                        activeColor = primaryColor,
                        inactiveColor = Color.White.copy(alpha = 0.5f),
                        indicatorWidth = 8.dp,
                        indicatorHeight = 8.dp,
                        spacing = 4.dp
                    )
                }
            }
        }

        // Scrollable content below the fixed carousel
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 200.dp) // Match the height of the carousel
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Categories section with orange accent bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // Orange accent bar
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(24.dp)
                        .background(primaryColor)
                        .align(Alignment.CenterStart)
                )

                Text(
                    text = "Catégories",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            val categoryList = listOf(
                CategoryItem(R.drawable.car1, "Voiture"),
                CategoryItem(R.drawable.chaise, "Chaise"),
                CategoryItem(R.drawable.ic_puma, "Puma"),
                CategoryItem(R.drawable.ic_skechers, "Skechers"),
                CategoryItem(R.drawable.ic_reebok, "Reebok")
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(categoryList) { category ->
                    CategoryCard(category, primaryColor)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Images Capturées section with orange accent bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // Orange accent bar
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(24.dp)
                        .background(primaryColor)
                        .align(Alignment.CenterStart)
                )

                Text(
                    text = "Images Capturées",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }

            if (imageFiles.isEmpty()) {
                // Empty state
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Aucune image capturée",
                            color = Color.Gray,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Utilisez la caméra pour détecter des objets",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            } else {
                // Grid of captured images
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height((imageFiles.size * 100).coerceAtMost(400).dp)
                ) {
                    items(imageFiles) { file ->
                        CapturedImageCard(file, primaryColor)
                    }
                }
            }

            // Add space at the bottom for the navigation bar
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun CategoryCard(category: CategoryItem, primaryColor: Color) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(primaryColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = category.imageResId),
                    contentDescription = category.name,
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = category.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
fun CapturedImageCard(file: File, primaryColor: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Image
            Image(
                painter = rememberAsyncImagePainter(Uri.fromFile(file)),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradient overlay for better text visibility
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = 100f,
                            endY = 300f
                        )
                    )
            )

            // Object name
            Text(
                text = file.nameWithoutExtension.substringBeforeLast("_"),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
            )

            // Indicator dot
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(primaryColor)
            )
        }
    }
}

