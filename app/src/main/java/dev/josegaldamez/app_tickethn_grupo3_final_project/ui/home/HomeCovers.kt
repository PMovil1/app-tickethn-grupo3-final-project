package dev.josegaldamez.app_tickethn_grupo3_final_project.ui.home

import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val LocalHomeCovers = compositionLocalOf<Map<Int, ImageBitmap>> { emptyMap() }

@Composable
internal fun ProvideHomeCovers(content: @Composable () -> Unit) {
    val resources = LocalContext.current.resources
    val covers = remember(resources) { mutableStateMapOf<Int, ImageBitmap>() }
    LaunchedEffect(resources) {
        for (image in DemoTicketEvent.entries.map { it.image }.distinct()) {
            val bitmap = withContext(Dispatchers.Default) {
                BitmapFactory.decodeResource(resources, image)?.asImageBitmap()
            }
            if (bitmap != null) covers[image] = bitmap
        }
    }
    CompositionLocalProvider(LocalHomeCovers provides covers, content = content)
}

@Composable
internal fun HomeCover(
    @DrawableRes image: Int,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center
) {
    val bitmap = LocalHomeCovers.current[image]
    if (bitmap == null) {
        Box(modifier.background(MaterialTheme.colorScheme.surfaceVariant))
    } else {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier,
            alignment = alignment
        )
    }
}

