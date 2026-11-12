package dev.josegaldamez.app_tickethn_grupo3_final_project.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.josegaldamez.app_tickethn_grupo3_final_project.R
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.ProfileDetails
import dev.josegaldamez.app_tickethn_grupo3_final_project.ui.access.TicketHNWordmark
import dev.josegaldamez.app_tickethn_grupo3_final_project.ui.access.ScreenHeaderDivider
import java.text.Normalizer
import java.io.IOException
import kotlinx.coroutines.launch

private enum class HomeTab(val label: Int, val icon: Int) {
    Home(R.string.home_tab, R.drawable.ic_home_home),
    Explore(R.string.home_explore, R.drawable.ic_home_search),
    Tickets(R.string.home_tickets, R.drawable.ic_home_ticket),
    Profile(R.string.home_profile, R.drawable.ic_access_person)
}

@Composable
internal fun HomeScreen(
    email: String,
    onSignOut: () -> Unit,
    signingOut: Boolean,
    signOutFailed: Boolean,
    onReadProfile: suspend () -> ProfileDetails,
    onSaveProfile: suspend (ProfileDetails) -> Unit
) {
    var tab by rememberSaveable { mutableStateOf(HomeTab.Home) }
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf<HomeCategory?>(null) }
    var city by rememberSaveable { mutableStateOf<HomeCity?>(null) }
    var selectedDate by rememberSaveable { mutableStateOf<Long?>(null) }
    var maxPrice by rememberSaveable { mutableStateOf<Int?>(null) }
    var showingFilters by rememberSaveable { mutableStateOf(false) }
    var selectedEvent by rememberSaveable { mutableStateOf<HomeEvent?>(null) }
    var selectedTicket by rememberSaveable { mutableStateOf<DemoTicket?>(null) }
    val focus = LocalFocusManager.current
    val colors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    var profileStatus by remember(email) { mutableStateOf<Result<ProfileDetails>?>(null) }
    var profileReload by remember(email) { mutableStateOf(0) }
    var savingProfile by remember(email) { mutableStateOf(false) }
    var profileSaveFailed by remember(email) { mutableStateOf(false) }

    LaunchedEffect(email, profileReload) {
        profileStatus = null
        profileStatus = try {
            Result.success(onReadProfile())
        } catch (error: IOException) {
            Result.failure(error)
        }
    }

    fun updateProfile(details: ProfileDetails) {
        if (savingProfile || signingOut || profileStatus?.isSuccess != true) return
        savingProfile = true
        profileSaveFailed = false
        scope.launch {
            try {
                onSaveProfile(details)
                profileStatus = Result.success(details)
            } catch (_: IOException) {
                profileSaveFailed = true
            } finally {
                savingProfile = false
            }
        }
    }

    fun applyFilters(filters: ExploreFilters) {
        category = filters.category
        city = filters.city
        selectedDate = filters.date
        maxPrice = filters.maxPrice
    }

    fun selectTab(next: HomeTab) {
        focus.clearFocus()
        query = ""
        applyFilters(ExploreFilters())
        showingFilters = false
        tab = next
    }

    val filters = ExploreFilters(category, city, selectedDate, maxPrice)
    val normalizedQuery = searchable(query.trim())
    val searchedEvents = HomeEvent.entries.filter { event ->
        val title = stringResource(event.title)
        val cityName = stringResource(event.city.label)
        val kind = stringResource(event.category.label)
        searchable("$title $cityName $kind").contains(normalizedQuery)
    }

    BackHandler(enabled = selectedTicket == null && tab != HomeTab.Home) { selectTab(HomeTab.Home) }

    TicketPresentation(selectedTicket, onClose = { selectedTicket = null }) {
        Scaffold(
            containerColor = colors.background,
            bottomBar = {
                Surface(color = colors.surface, shadowElevation = 1.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().navigationBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 6.dp).selectableGroup(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        HomeTab.entries.forEach { item ->
                            val selected = item == tab
                            val tint = if (selected) colors.primary else colors.onSurfaceVariant
                            Column(
                                modifier = Modifier.weight(1f).heightIn(min = 50.dp).clip(CircleShape)
                                    .background(if (selected) colors.primaryContainer else Color.Transparent)
                                    .selectable(selected, role = Role.Tab, onClick = { if (!selected) selectTab(item) })
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)
                            ) {
                                Icon(painterResource(item.icon), contentDescription = null, tint = tint)
                                Text(stringResource(item.label), style = MaterialTheme.typography.labelMedium, color = tint)
                            }
                        }
                    }
                }
            }
        ) { insets ->
            Box(
                modifier = Modifier.fillMaxSize().padding(insets),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier.widthIn(max = 660.dp).fillMaxSize()
                        .verticalScroll(rememberScrollState()).padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when (tab) {
                        HomeTab.Home, HomeTab.Explore -> {
                            if (tab == HomeTab.Home) {
                                HomeHeader(
                                    email, profileStatus?.getOrNull()?.displayName,
                                    onProfile = { selectTab(HomeTab.Profile) }
                                )
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TicketHNWordmark()
                                    Box(
                                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        ExploreFiltersButton(filters.count) {
                                            focus.clearFocus()
                                            showingFilters = true
                                        }
                                    }
                                }
                                ScreenHeaderDivider()
                                PageHeading(stringResource(R.string.home_explore))
                            }
                            HomeSearch(query, onQueryChange = {
                                query = it
                                if (tab == HomeTab.Home && it.isNotBlank()) tab = HomeTab.Explore
                            })

                            if (tab == HomeTab.Explore) {
                                val results = searchedEvents.filter { event ->
                                    filters.matches(event) && (filters.date == null || filters.date == event.dateMillis)
                                }
                                Text(
                                    pluralStringResource(R.plurals.home_event_count, results.size, results.size),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.onSurfaceVariant
                                )
                                if (results.isEmpty()) {
                                    Text(stringResource(R.string.home_no_events), color = colors.onSurfaceVariant)
                                    if (filters.count > 0) {
                                        TextButton(onClick = { applyFilters(ExploreFilters()) }) {
                                            Text(stringResource(R.string.home_clear_filter))
                                        }
                                    }
                                } else {
                                    results.forEach { event ->
                                        ExploreEventCard(event, onClick = { selectedEvent = event })
                                    }
                                }
                            } else {
                                HomeSection(stringResource(R.string.home_featured), onMore = { selectTab(HomeTab.Explore) })
                                FeaturedEvents(onEvent = { selectedEvent = it })
                                HomeSection(stringResource(R.string.home_upcoming), onMore = { selectTab(HomeTab.Explore) })
                                listOf(HomeEvent.Festival, HomeEvent.Football, HomeEvent.AcousticNight, HomeEvent.ValleyStories)
                                    .sortedBy { it.dateMillis }
                                    .chunked(2)
                                    .forEach { events -> EventRow(events, onEvent = { selectedEvent = it }) }
                                HomeSection(stringResource(R.string.home_your_next_ticket))
                                NextTicket(onClick = { selectedTicket = DemoTicket.Concert })
                            }
                        }
                        HomeTab.Tickets -> TicketsScreen(onTicket = { selectedTicket = it })
                        HomeTab.Profile -> ProfileScreen(
                            email = email,
                            details = profileStatus?.getOrNull(),
                            readFailed = profileStatus?.isFailure == true,
                            saveFailed = profileSaveFailed,
                            saving = savingProfile || signingOut,
                            onRetry = { profileReload++ },
                            onUpdate = ::updateProfile,
                            onSignOut = onSignOut,
                            signOutFailed = signOutFailed
                        )
                    }
                }
            }
        }
    }

    if (tab == HomeTab.Explore && showingFilters) {
        ExploreFiltersSheet(
            applied = filters,
            searchedEvents = searchedEvents,
            onDismiss = { showingFilters = false },
            onApply = {
                applyFilters(it)
                showingFilters = false
            }
        )
    }

    selectedEvent?.let { event ->
        AlertDialog(
            onDismissRequest = { selectedEvent = null },
            title = { Text(stringResource(event.title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.home_demo_event), color = colors.onSurfaceVariant)
                    EventMetadata(R.drawable.ic_home_calendar, event.dateLabel)
                    EventMetadata(R.drawable.ic_home_pin, stringResource(event.city.label))
                    Text(event.priceLabel())
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedEvent = null }) { Text(stringResource(R.string.access_close)) }
            }
        )
    }
}

@Composable
private fun TicketPresentation(ticket: DemoTicket?, onClose: () -> Unit, content: @Composable () -> Unit) {
    val stateHolder = rememberSaveableStateHolder()
    AnimatedContent(
        targetState = ticket,
        modifier = Modifier.fillMaxSize(),
        transitionSpec = {
            if (targetState != null) {
                (slideInVertically(animationSpec = tween(340)) { height -> height } togetherWith
                    ExitTransition.KeepUntilTransitionsFinished).apply { targetContentZIndex = 1f }
            } else {
                EnterTransition.None togetherWith slideOutVertically(animationSpec = tween(260)) { height -> height }
            }
        },
        label = "Ticket presentation"
    ) { visibleTicket ->
        if (visibleTicket == null) {
            stateHolder.SaveableStateProvider("ticket_origin", content)
        } else {
            TicketDetailScreen(visibleTicket, onClose)
        }
    }
}

@Composable
private fun HomeHeader(email: String, displayName: String?, onProfile: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val name = (displayName ?: profileNameFromEmail(email))?.substringBefore(' ')
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TicketHNWordmark()
            IconButton(onClick = onProfile, modifier = Modifier.size(48.dp)) {
                ProfilePortrait(Modifier.size(46.dp), stringResource(R.string.home_profile))
            }
        }
        ScreenHeaderDivider(Modifier.padding(top = 8.dp, bottom = 16.dp))
        val fallbackGreeting = stringResource(R.string.home_greeting_fallback)
        val greeting = if (name == null) {
            buildAnnotatedString { append(fallbackGreeting) }
        } else {
            val fullGreeting = stringResource(R.string.home_greeting, name)
            buildAnnotatedString {
                append(fullGreeting)
                val start = fullGreeting.lastIndexOf(name)
                if (start >= 0) addStyle(SpanStyle(color = colors.primary), start, start + name.length)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                greeting,
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 30.sp, lineHeight = 34.sp),
                modifier = Modifier.semantics { heading() }
            )
            Text(
                stringResource(R.string.home_tagline),
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun ProfilePortrait(modifier: Modifier, description: String? = null) {
    Image(
        painterResource(R.drawable.home_avatar),
        contentDescription = description,
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(CircleShape)
    )
}

@Composable
private fun HomeSearch(query: String, onQueryChange: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(stringResource(R.string.home_search), style = MaterialTheme.typography.bodyMedium) },
        leadingIcon = { Icon(painterResource(R.drawable.ic_home_search), contentDescription = null) },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        painterResource(R.drawable.ic_home_clear),
                        contentDescription = stringResource(R.string.home_clear_search)
                    )
                }
            }
        } else {
            null
        },
        textStyle = MaterialTheme.typography.bodyMedium,
        singleLine = true,
        shape = CircleShape,
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = colors.surfaceVariant,
            unfocusedContainerColor = colors.surfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}

@Composable
private fun PageHeading(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 26.sp, lineHeight = 30.sp),
        modifier = Modifier.semantics { heading() }
    )
}

@Composable
private fun HomeSection(title: String, onMore: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 22.sp, lineHeight = 26.sp),
            modifier = Modifier.weight(1f).semantics { heading() }
        )
        if (onMore != null) {
            TextButton(onClick = onMore, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(R.string.home_see_more))
                Icon(painterResource(R.drawable.ic_home_next), contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

private fun searchable(text: String): String =
    Normalizer.normalize(text.lowercase(), Normalizer.Form.NFD).filterNot {
        Character.getType(it) == Character.NON_SPACING_MARK.toInt()
    }
