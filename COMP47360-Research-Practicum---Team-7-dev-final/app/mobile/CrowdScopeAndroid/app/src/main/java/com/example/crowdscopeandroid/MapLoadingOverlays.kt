// This file handles the initialization of and logic relating to loading animations.

package com.example.crowdscopeandroid

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun MapLoadingOverlays(
    isFirstLoadEverComplete: Boolean,
    isDataUpdating: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val crowdScopeThemeColor = Color(0xFF27504A)

    AnimatedVisibility(
        visible = !isFirstLoadEverComplete,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .zIndex(4f),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(R.drawable.crowd_scope)
                    .build(),
                contentDescription = "Loading Map Data",
                modifier = Modifier.size(600.dp),
                contentScale = ContentScale.Fit
            )
        }
    }

    AnimatedVisibility(
        visible = isDataUpdating && isFirstLoadEverComplete,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
            .zIndex(7f)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                color = crowdScopeThemeColor
            )
        }
    }
}
