package dev.josegaldamez.app_tickethn_grupo3_final_project.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.josegaldamez.app_tickethn_grupo3_final_project.R
import dev.josegaldamez.app_tickethn_grupo3_final_project.ui.access.AccessButton
import dev.josegaldamez.app_tickethn_grupo3_final_project.ui.access.TicketHNWordmark
import dev.josegaldamez.app_tickethn_grupo3_final_project.ui.access.ScreenHeaderDivider
import kotlinx.coroutines.launch

private enum class Slide(
    @param:StringRes val title: Int,
    @param:StringRes val description: Int,
    @param:StringRes val emphasis: Int? = null
) {
    Discover(
        R.string.onboarding_discover_title,
        R.string.onboarding_discover_description,
        R.string.onboarding_discover_emphasis
    ),
    Purchase(
        R.string.onboarding_purchase_title,
        R.string.onboarding_purchase_description,
        R.string.onboarding_purchase_emphasis
    ),
    Tickets(
        R.string.onboarding_tickets_title,
        R.string.onboarding_tickets_description,
        R.string.onboarding_tickets_emphasis
    ),
    Welcome(R.string.onboarding_welcome_title, R.string.onboarding_welcome_description)
}

@Composable
internal fun OnboardingScreen(
    onRegister: () -> Unit,
    onLogin: () -> Unit,
    saving: Boolean,
    saveFailed: Boolean,
    startAtWelcome: Boolean = false
) {
    val lastPage = Slide.entries.lastIndex
    val pager = rememberPagerState(initialPage = if (startAtWelcome) lastPage else 0, pageCount = { Slide.entries.size })
    val scope = rememberCoroutineScope()
    val background = if (isSystemInDarkTheme()) {
        Modifier
    } else {
        Modifier.background(
            Brush.verticalGradient(
                0f to Color(0xFFE8F0FC),
                0.28f to Color(0xFFFAFBFE),
                1f to Color(0xFFF4F7FD)
            )
        )
    }

    BackHandler(enabled = pager.currentPage > 0 && !saving) {
        scope.launch { pager.animateScrollToPage(pager.currentPage - 1) }
    }

    Column(
        modifier = Modifier.fillMaxSize().then(background).safeDrawingPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TicketHNWordmark(Modifier.padding(top = 20.dp, bottom = 12.dp))
        ScreenHeaderDivider(Modifier.widthIn(max = 480.dp).fillMaxWidth().padding(horizontal = 24.dp))
        HorizontalPager(
            state = pager,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            userScrollEnabled = !saving
        ) { index ->
            val slide = Slide.entries[index]
            if (slide == Slide.Welcome) {
                SlideLayout {
                    WelcomeIllustration()
                    SlideCopy(slide)
                    WelcomeActions(onRegister, onLogin, saving, saveFailed)
                }
            } else {
                StorySlide(slide)
            }
        }

        Box(
            modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth().heightIn(min = 52.dp)
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            ProgressIndicator(pager.currentPage, Slide.entries.size)
            if (pager.currentPage != lastPage) {
                TextButton(
                    onClick = { scope.launch { pager.animateScrollToPage(lastPage) } },
                    modifier = Modifier.align(Alignment.CenterEnd).heightIn(min = 48.dp)
                ) {
                    Text(stringResource(R.string.onboarding_skip), color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
    }
}

@Composable
private fun StorySlide(slide: Slide) {
    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        val availableHeight = maxHeight
        val pageWidth = maxWidth.coerceAtMost(480.dp)
        val sidePadding = (pageWidth * 0.08f).coerceIn(24.dp, 38.dp)
        val typeScale = (pageWidth / 380.dp).coerceIn(0.86f, 1.12f)
        val discover = slide == Slide.Discover

        Column(
            modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth()
                .verticalScroll(rememberScrollState()).heightIn(min = availableHeight),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(Modifier.padding(start = sidePadding, end = sidePadding, top = 44.dp)) {
                SlideCopy(slide, storyTypeScale = typeScale)
                if (discover) {
                    Spacer(Modifier.height(28.dp))
                    EventCategories()
                    Spacer(Modifier.height(8.dp))
                } else {
                    Spacer(Modifier.height(20.dp))
                }
            }
            if (discover) {
                Image(
                    painter = painterResource(R.drawable.onboarding_discover),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1.05f),
                    contentScale = ContentScale.FillBounds
                )
            } else {
                val purchase = slide == Slide.Purchase
                val card = painterResource(if (purchase) R.drawable.onboarding_purchase else R.drawable.onboarding_tickets)
                val aspectRatio = card.intrinsicSize.width / card.intrinsicSize.height
                val cardWidth = pageWidth * 0.93f
                val cardHeight = minOf(cardWidth / aspectRatio, (availableHeight * 0.71f).coerceAtLeast(240.dp))
                Image(
                    painter = card,
                    contentDescription = stringResource(
                        if (purchase) R.string.onboarding_purchase_example else R.string.onboarding_ticket_example
                    ),
                    modifier = Modifier.fillMaxWidth(0.93f).height(cardHeight)
                )
            }
        }
    }
}

@Composable
private fun SlideLayout(content: @Composable () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        val availableHeight = maxHeight
        Column(
            modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth().verticalScroll(rememberScrollState())
                .heightIn(min = availableHeight).padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content()
        }
    }
}

@Composable
private fun SlideCopy(slide: Slide, storyTypeScale: Float = 1f) {
    val colors = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val title = stringResource(slide.title)
    val emphasis = slide.emphasis?.let { stringResource(it) }
    val accent = when {
        (slide == Slide.Discover || slide == Slide.Tickets) && !dark -> Color(0xFF153BEE)
        slide != Slide.Purchase -> colors.primary
        dark -> Color(0xFF6EE7B7)
        else -> Color(0xFF008B62)
    }
    val alignment = if (slide == Slide.Welcome) TextAlign.Center else TextAlign.Start
    val typography = MaterialTheme.typography
    val story = slide != Slide.Welcome
    val titleStyle = if (story) {
        typography.headlineLarge.copy(fontSize = 36.sp * storyTypeScale, lineHeight = 39.sp * storyTypeScale)
    } else {
        typography.headlineLarge
    }
    val descriptionStyle = if (story) {
        typography.bodyLarge.copy(fontSize = 19.sp * storyTypeScale, lineHeight = 25.sp * storyTypeScale)
    } else {
        typography.bodyLarge
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (story) 16.dp else 10.dp)
    ) {
        Text(
            text = buildAnnotatedString {
                append(title)
                if (emphasis != null) {
                    append(" ")
                    withStyle(SpanStyle(color = accent)) { append(emphasis) }
                }
            },
            modifier = Modifier.fillMaxWidth().semantics { heading() },
            style = titleStyle,
            textAlign = alignment
        )
        Text(
            text = stringResource(slide.description),
            modifier = Modifier.fillMaxWidth(),
            style = descriptionStyle,
            color = if (story) colors.onBackground else colors.onSurfaceVariant,
            textAlign = alignment
        )
    }
}

@Composable
private fun WelcomeIllustration() {
    val dark = isSystemInDarkTheme()
    Box(
        modifier = Modifier.fillMaxWidth().aspectRatio(1.7f),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painterResource(R.drawable.onboarding_welcome),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            alpha = if (dark) 0.46f else 1f,
            contentScale = ContentScale.Fit
        )
        Image(
            painterResource(R.drawable.tickethn_logo),
            contentDescription = null,
            modifier = Modifier.size(114.dp)
        )
    }
}

@Composable
private fun WelcomeActions(onRegister: () -> Unit, onLogin: () -> Unit, saving: Boolean, saveFailed: Boolean) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AccessButton(
            label = stringResource(R.string.access_register),
            onClick = onRegister,
            modifier = Modifier.fillMaxWidth(),
            enabled = !saving
        )
        OutlinedButton(
            onClick = onLogin,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, if (saving) colors.outline else colors.primary),
            enabled = !saving
        ) {
            Text(stringResource(R.string.access_login))
        }
        if (saveFailed) {
            Text(
                text = stringResource(R.string.onboarding_save_error),
                color = colors.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
            )
        }
    }
}

@Composable
private fun ProgressIndicator(currentPage: Int, pageCount: Int) {
    val colors = MaterialTheme.colorScheme
    val position = stringResource(R.string.onboarding_page, currentPage + 1, pageCount)
    Row(
        modifier = Modifier.semantics { contentDescription = position },
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val selected = index == currentPage
            val color by animateColorAsState(
                if (selected) colors.primary else colors.outline.copy(alpha = 0.55f),
                label = "Pager indicator"
            )
            Box(Modifier.size(9.dp).background(color, CircleShape))
        }
    }
}
