package dev.josegaldamez.app_tickethn_grupo3_final_project.ui.home

import android.os.SystemClock
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.AuthApiException
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.OrganizerEvents
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.ProfileDetails
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.PublicEvent
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.PublicEventsApi
import dev.josegaldamez.app_tickethn_grupo3_final_project.ui.dismissKeyboardTarget
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

private const val ORGANIZER_ROLE_REFRESH_INTERVAL_MS = 5 * 60 * 1000L

@Composable
internal fun HomeScreen(
    email: String,
    accountName: String?,
    organizerRole: Boolean?,
    onSignOut: () -> Unit,
    signingOut: Boolean,
    signOutFailed: Boolean,
    onReadProfile: suspend () -> ProfileDetails,
    onSaveProfile: suspend (ProfileDetails) -> Unit,
    onRefreshOrganizerRole: suspend () -> Unit,
    organizerEvents: OrganizerEvents
) {
    var tab by rememberSaveable { mutableStateOf(HomeTab.Home) }
    var query by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedDate by rememberSaveable { mutableStateOf<Long?>(null) }
    var maxPrice by rememberSaveable { mutableStateOf<Int?>(null) }
    var showingFilters by rememberSaveable { mutableStateOf(false) }
    var selectedEventId by rememberSaveable { mutableStateOf<String?>(null) }
    var catalog by remember { mutableStateOf<List<PublicEvent>>(emptyList()) }
    var catalogLoading by remember { mutableStateOf(true) }
    var catalogFailed by remember { mutableStateOf(false) }
    var catalogRequest by remember { mutableStateOf(0) }
    LaunchedEffect(catalogRequest) {
        catalogLoading = true
        catalogFailed = false
        try {
            catalog = PublicEventsApi.list()
        } catch (_: IOException) {
            catalogFailed = true
        } finally {
            catalogLoading = false
        }
    }
    var selectedTicket by rememberSaveable { mutableStateOf<DemoTicket?>(null) }
    val focus = LocalFocusManager.current
    val colors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    var profileStatus by remember(email) { mutableStateOf<Result<ProfileDetails>?>(null) }
    var profileReload by remember(email) { mutableStateOf(0) }
    var savingProfile by remember(email) { mutableStateOf(false) }
    var profileSaveFailed by remember(email) { mutableStateOf(false) }
    var editingProfile by rememberSaveable(email) { mutableStateOf(false) }
    var profileDestination by rememberSaveable(email) { mutableStateOf<ProfileDestination?>(null) }
    var draftCreated by rememberSaveable(email) { mutableStateOf(false) }
    var eventEditorId by rememberSaveable(email) { mutableStateOf<String?>(null) }
    var eventPublished by rememberSaveable(email) { mutableStateOf(false) }
    var creatingFromList by rememberSaveable(email) { mutableStateOf(false) }
    val isOrganizer = organizerRole == true
    val openProfileDestination = profileDestination?.takeIf { !it.requiresOrganizer || isOrganizer }
    val showingProfilePage = editingProfile || openProfileDestination != null

    val refreshOrganizerRole by rememberUpdatedState(onRefreshOrganizerRole)
    LaunchedEffect(organizerRole) {
        if (organizerRole == false && profileDestination?.requiresOrganizer == true) {
            profileDestination = null
        }
    }

    LaunchedEffect(email) {
        var lastCheckedAt: Long? = null
        snapshotFlow { tab == HomeTab.Profile }.collect { inProfile ->
            val previous = lastCheckedAt
            if (previous == null ||
                (inProfile && SystemClock.elapsedRealtime() - previous >= ORGANIZER_ROLE_REFRESH_INTERVAL_MS)
            ) {
                lastCheckedAt = SystemClock.elapsedRealtime()
                try {
                    refreshOrganizerRole()
                } catch (_: IOException) {
                    // Keep the last known role when the server is unreachable.
                } catch (_: AuthApiException) {
                    // Only a successful role response can replace the cached value.
                }
            }
        }
    }

    LaunchedEffect(email, profileReload) {
        profileStatus = null
        profileStatus = try {
            Result.success(onReadProfile())
        } catch (error: IOException) {
            Result.failure(error)
        }
    }

    fun updateProfile(details: ProfileDetails, closeEditor: Boolean) {
        if (savingProfile || signingOut || profileStatus?.isSuccess != true) return
        savingProfile = true
        profileSaveFailed = false
        scope.launch {
            try {
                onSaveProfile(details)
                profileStatus = Result.success(details)
                if (closeEditor) editingProfile = false
            } catch (_: IOException) {
                profileSaveFailed = true
            } finally {
                savingProfile = false
            }
        }
    }

    fun applyFilters(filters: ExploreFilters) {
        address = filters.address
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

    val filters = ExploreFilters(address, selectedDate, maxPrice)
    val upcomingRows = remember(catalog) { catalog.chunked(2) }
    val searchedEvents = remember(catalog, query) {
        if (query.isBlank()) catalog else {
            val normalizedQuery = searchable(query.trim())
            catalog.filter { event ->
                searchable("${event.title} ${event.address.orEmpty()} ${event.description.orEmpty()}").contains(normalizedQuery)
            }
        }
    }

    BackHandler(enabled = !showingProfilePage && selectedTicket == null && tab != HomeTab.Home) { selectTab(HomeTab.Home) }
    BackHandler(enabled = editingProfile) {
        if (!savingProfile) editingProfile = false
    }
    BackHandler(enabled = openProfileDestination != null && !openProfileDestination.requiresOrganizer && selectedTicket == null) {
        profileDestination = null
    }

    TicketPresentation(selectedTicket, onClose = { selectedTicket = null }) {
        Scaffold(
            containerColor = colors.background,
            bottomBar = {
                if (!showingProfilePage) Surface(color = colors.surface, shadowElevation = 1.dp) {
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
            if (editingProfile) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(insets).consumeWindowInsets(insets).imePadding(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    EditProfileScreen(
                        details = profileStatus?.getOrNull(),
                        accountName = accountName,
                        email = email,
                        readFailed = profileStatus?.isFailure == true,
                        saveFailed = profileSaveFailed,
                        saving = savingProfile,
                        onBack = { editingProfile = false },
                        onRetry = { profileReload++ },
                        onSave = { updateProfile(it, closeEditor = true) },
                        onRequestOrganizer = {
                            profileStatus?.getOrNull()?.let { current ->
                                updateProfile(current.copy(organizerRequested = true), closeEditor = false)
                            }
                        },
                        modifier = Modifier.widthIn(max = 660.dp).fillMaxSize()
                    )
                }
                return@Scaffold
            }
            if (openProfileDestination?.requiresOrganizer == true) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(insets).consumeWindowInsets(insets).imePadding(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    OrganizerDestinationScreen(
                        destination = openProfileDestination,
                        draftCreated = draftCreated,
                        eventPublished = eventPublished,
                        eventId = eventEditorId,
                        onBack = {
                            profileDestination = if (eventEditorId != null ||
                                (openProfileDestination == ProfileDestination.CreateEvent && creatingFromList)
                            ) {
                                ProfileDestination.OrganizerEvents
                            } else {
                                null
                            }
                            draftCreated = false
                            eventPublished = false
                            eventEditorId = null
                            creatingFromList = false
                        },
                        onNew = {
                            draftCreated = false
                            eventPublished = false
                            creatingFromList = true
                            profileDestination = ProfileDestination.CreateEvent
                        },
                        onManage = { id ->
                            eventEditorId = id
                            draftCreated = false
                            eventPublished = false
                            profileDestination = ProfileDestination.OrganizerEvents
                        },
                        onCreated = { id, next ->
                            eventEditorId = id.takeIf { next }
                            draftCreated = !next
                            creatingFromList = false
                            profileDestination = ProfileDestination.OrganizerEvents
                        },
                        onPublished = {
                            eventEditorId = null
                            eventPublished = true
                            catalogRequest++
                        },
                        organizerEvents = organizerEvents,
                        modifier = Modifier.widthIn(max = 660.dp).fillMaxSize()
                    )
                }
                return@Scaffold
            }
            if (openProfileDestination != null) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(insets),
                    contentAlignment = Alignment.TopCenter
                ) {
                    ProfileDestinationScreen(
                        destination = openProfileDestination,
                        email = email,
                        onBack = { profileDestination = null },
                        modifier = Modifier.widthIn(max = 660.dp).fillMaxSize()
                    )
                }
                return@Scaffold
            }
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize().padding(insets),
                contentAlignment = Alignment.TopCenter
            ) {
                if (tab == HomeTab.Profile) {
                    ProfileScreen(
                        email = email,
                        accountName = accountName,
                        details = profileStatus?.getOrNull(),
                        readFailed = profileStatus?.isFailure == true,
                        saving = savingProfile || signingOut,
                        isOrganizer = isOrganizer,
                        onRetry = { profileReload++ },
                        onEdit = {
                            profileSaveFailed = false
                            editingProfile = true
                        },
                        onOpen = {
                            draftCreated = false
                            eventPublished = false
                            eventEditorId = null
                            creatingFromList = false
                            profileDestination = it
                        },
                        onSignOut = onSignOut,
                        signOutFailed = signOutFailed,
                        modifier = Modifier.widthIn(max = 660.dp).fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .heightIn(min = maxHeight)
                            .padding(horizontal = 14.dp, vertical = 16.dp)
                    )
                    return@BoxWithConstraints
                }
                val currentTab = tab
                val results = if (currentTab == HomeTab.Explore) {
                    searchedEvents.filter { event ->
                        filters.matches(event) && (filters.date == null || filters.date == event.calendarDay)
                    }
                } else {
                    emptyList()
                }
                key(currentTab == HomeTab.Tickets) {
                    LazyColumn(
                        modifier = Modifier.widthIn(max = 660.dp).fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (currentTab == HomeTab.Tickets) {
                            item { TicketsScreen(onTicket = { selectedTicket = it }) }
                        } else {
                            item(key = "header") {
                                if (currentTab == HomeTab.Home) {
                                    HomeHeader(
                                        profileStatus?.getOrNull()?.displayName ?: accountName,
                                        onProfile = { selectTab(HomeTab.Profile) }
                                    )
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
                                }
                            }
                            item(key = "search") {
                                HomeSearch(query, onQueryChange = {
                                    query = it
                                    if (currentTab == HomeTab.Home && it.isNotBlank()) tab = HomeTab.Explore
                                })
                            }
                            if (catalogLoading || catalogFailed || catalog.isEmpty()) {
                                item(key = "catalog_status") {
                                    when {
                                        catalogFailed -> {
                                            ProfileFeedback(R.string.home_catalog_error)
                                            TextButton(onClick = { catalogRequest++ }) {
                                                Text(stringResource(R.string.onboarding_retry))
                                            }
                                        }
                                        catalogLoading && catalog.isEmpty() -> EventLoading(
                                            if (currentTab == HomeTab.Home) EventLoadingLayout.Home
                                            else EventLoadingLayout.Explore
                                        )
                                        catalogLoading -> {
                                            LinearProgressIndicator(Modifier.fillMaxWidth())
                                        }
                                        else -> {
                                            Text(stringResource(R.string.home_catalog_empty))
                                            TextButton(onClick = { catalogRequest++ }) {
                                                Text(stringResource(R.string.home_catalog_refresh))
                                            }
                                        }
                                    }
                                }
                            }
                            if (catalog.isNotEmpty() && currentTab == HomeTab.Home) {
                                item(key = "featured_heading") {
                                    HomeSection(stringResource(R.string.home_featured), onMore = { selectTab(HomeTab.Explore) })
                                }
                                item(key = "featured") {
                                    FeaturedEvents(catalog.take(3), autoAdvance = selectedEventId == null) {
                                        selectedEventId = it.id
                                    }
                                }
                                item(key = "upcoming_heading") {
                                    HomeSection(stringResource(R.string.home_upcoming), onMore = { selectTab(HomeTab.Explore) })
                                }
                                items(upcomingRows, key = { it.first().id }) { row ->
                                    HomeEventRow(row) { selectedEventId = it.id }
                                }
                            } else if (catalog.isNotEmpty() && currentTab == HomeTab.Explore) {

                                item(key = "event_count") {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            pluralStringResource(R.plurals.home_event_count, results.size, results.size),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = colors.onSurfaceVariant,
                                            modifier = Modifier.weight(1f)
                                        )
                                        TextButton(onClick = { catalogRequest++ }, enabled = !catalogLoading && !catalogFailed) {
                                            Text(stringResource(R.string.home_catalog_refresh))
                                        }
                                    }
                                }
                                if (results.isEmpty()) {
                                    item(key = "no_events") {
                                        Text(stringResource(R.string.home_no_events), color = colors.onSurfaceVariant)
                                        if (filters.count > 0) {
                                            TextButton(onClick = { applyFilters(ExploreFilters()) }) {
                                                Text(stringResource(R.string.home_clear_filter))
                                            }
                                        }
                                    }
                                } else {
                                    items(results, key = { it.id }) { event ->
                                        ExploreEventCard(event) { selectedEventId = event.id }
                                    }
                                }
                            }
                        }
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

    catalog.firstOrNull { it.id == selectedEventId }?.let { event ->
        PublicEventDetail(event, onClose = { selectedEventId = null })
    }
}

@Composable
private fun PublicEventDetail(event: PublicEvent, onClose: () -> Unit) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text(event.title) },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!event.coverUrl.isNullOrBlank()) {
                    PublicEventCover(event.coverUrl, Modifier.fillMaxWidth().aspectRatio(2.4f).clip(MaterialTheme.shapes.medium))
                }
                parseOrganizerDate(event.startsAt)?.let {
                    EventMetadata(R.drawable.ic_home_calendar, formatOrganizerDate(it))
                }
                event.address?.takeIf { it.isNotBlank() }?.let { EventMetadata(R.drawable.ic_home_pin, it) }
                event.description?.takeIf { it.isNotBlank() }?.let { Text(it) }
                val ticketTypes = event.ticketTypes.orEmpty().filter { it.active }
                Text(stringResource(R.string.organizer_localities), style = MaterialTheme.typography.titleMedium)
                if (ticketTypes.isEmpty()) Text(stringResource(R.string.home_event_unavailable))
                ticketTypes.forEach { locality ->
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(locality.title, style = MaterialTheme.typography.titleSmall)
                        locality.description?.takeIf { it.isNotBlank() }?.let {
                            Text(it, style = MaterialTheme.typography.bodySmall)
                        }
                        Text(
                            stringResource(R.string.home_locality_availability, formatEventPrice(locality.price), locality.available),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onClose) { Text(stringResource(R.string.access_close)) }
        }
    )
}

@Composable
private fun TicketPresentation(ticket: DemoTicket?, onClose: () -> Unit, content: @Composable () -> Unit) {
    val stateHolder = rememberSaveableStateHolder()
    ProvideHomeCovers {
        TicketTransition(ticket, onClose) {
            stateHolder.SaveableStateProvider("ticket_origin", content)
        }
    }
}

@Composable
private fun TicketTransition(ticket: DemoTicket?, onClose: () -> Unit, content: @Composable () -> Unit) {
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
            content()
        } else {
            TicketDetailScreen(visibleTicket, onClose)
        }
    }
}

@Composable
private fun HomeHeader(name: String?, onProfile: () -> Unit) {
    val colors = MaterialTheme.colorScheme
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
        modifier = Modifier.fillMaxWidth().dismissKeyboardTarget(),
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
