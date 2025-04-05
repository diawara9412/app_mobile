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

    Box(modifier = Modifier.fillMaxSize()) {
        // Fixed carousel at the top with enhanced design
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
                    .height(220.dp)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .zIndex(1f)
            ) {
                // Shadow effect for the carousel
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .shadow(
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
                            .clip(RoundedCornerShape(20.dp))
                    ) {
                        // Carousel with page transformation
                        HorizontalPager(
                            count = carouselImages.size,
                            state = pagerState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 0.dp),
                        ) { page ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        // Calculate the absolute offset for the current page from the
                                        // scroll position. We use the absolute value which allows us to mirror
                                        // any effects for both directions
                                        val pageOffset = calculateCurrentOffsetForPage(page).absoluteValue

                                        // We animate the scaleX + scaleY, between 85% and 100%
                                        lerp(
                                            start = 0.85f,
                                            stop = 1f,
                                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                                        ).also { scale ->
                                            scaleX = scale
                                            scaleY = scale
                                        }

                                        // We animate the alpha, between 50% and 100%
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
                                    contentScale = ContentScale.Crop
                                )
                            }
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

                        // Decorative elements
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

                        // Page indicator with animation
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

        // Scrollable content below the fixed carousel
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 220.dp) // Match the height of the carousel
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Categories section with enhanced design
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Orange accent bar with rounded corners
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .height(30.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(primaryColor)
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
                CategoryItem(R.drawable.car1, "Voiture"),
                CategoryItem(R.drawable.chaise, "Chaise"),
                CategoryItem(R.drawable.ic_puma, "Puma"),
                CategoryItem(R.drawable.ic_skechers, "Skechers"),
                CategoryItem(R.drawable.ic_reebok, "Reebok")
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(categoryList) { category ->
                    EnhancedCategoryCard(category, primaryColor)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Images Capturées section with enhanced design
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Orange accent bar with rounded corners
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

            if (imageFiles.isEmpty()) {
                // Enhanced empty state
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8F8))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Animated pulsing effect
                        val infiniteTransition = rememberInfiniteTransition()
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 0.9f,
                            targetValue = 1.1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1500),
                                repeatMode = RepeatMode.Reverse
                            )
                        )

                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
                                .clip(CircleShape)
                                .background(primaryColor.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = primaryColor
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Aucune image capturée",
                            color = Color.DarkGray,
                            fontSize = 18.sp,
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
                // Enhanced grid of captured images
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .height((imageFiles.size * 100).coerceAtMost(400).dp)
                ) {
                    items(imageFiles) { file ->
                        EnhancedCapturedImageCard(file, primaryColor)
                    }
                }
            }

            // Add space at the bottom for the navigation bar
            Spacer(modifier = Modifier.height(80.dp))
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

