package dev.josegaldamez.app_tickethn_grupo3_final_project.ui.home

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.josegaldamez.app_tickethn_grupo3_final_project.R
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.AuthApiException
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.CoverTooLargeException
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.EventDraft
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.EventCoverCache
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.ORGANIZER_PAGE_SIZE
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.OrganizerEvent
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.OrganizerEvents
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.readEventCover
import dev.josegaldamez.app_tickethn_grupo3_final_project.ui.access.AuthPrimaryButton
import dev.josegaldamez.app_tickethn_grupo3_final_project.ui.access.ScreenHeaderDivider
import dev.josegaldamez.app_tickethn_grupo3_final_project.ui.dismissKeyboardTarget
import java.io.IOException
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val hondurasTime = TimeZone.getTimeZone("America/Tegucigalpa")

@Composable
internal fun OrganizerDestinationScreen(
    destination: ProfileDestination,
    draftCreated: Boolean,
    eventPublished: Boolean,
    eventId: String?,
    onBack: () -> Unit,
    onNew: () -> Unit,
    onManage: (String) -> Unit,
    onCreated: (String, Boolean) -> Unit,
    onPublished: () -> Unit,
    organizerEvents: OrganizerEvents,
    modifier: Modifier = Modifier
) {
    val creating = destination == ProfileDestination.CreateEvent
    var saving by remember { mutableStateOf(false) }
    BackHandler { if (!saving) onBack() }

    Column(
        modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, enabled = !saving) {
                Icon(
                    painterResource(R.drawable.ic_verification_back),
                    contentDescription = stringResource(R.string.profile_back)
                )
            }
            Text(
                stringResource(if (eventId != null) R.string.organizer_manage else destination.title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.semantics { heading() }
            )
        }
        ScreenHeaderDivider()

        when {
            eventId != null -> key(eventId) {
                ManageOrganizerEvent(eventId, organizerEvents, saving, { saving = it }, onPublished)
            }
            creating -> EventDetailsForm(
                saving = saving,
                onSaving = { saving = it },
                onSave = { draft, cover, next ->
                    val event = organizerEvents.create(draft, cover)
                    onCreated(event.id, next)
                }
            )
            else -> Box(Modifier.fillMaxSize()) {
                OrganizerEventList(draftCreated, eventPublished, onManage, organizerEvents::list)
                ExtendedFloatingActionButton(
                    onClick = onNew,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(end = 4.dp, bottom = 4.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = { Icon(painterResource(R.drawable.ic_organizer_add), contentDescription = null) },
                    text = { Text(stringResource(R.string.organizer_create)) }
                )
            }
        }
    }
}

@Composable
private fun OrganizerEventList(
    draftCreated: Boolean,
    eventPublished: Boolean,
    onManage: (String) -> Unit,
    onLoad: suspend (Int) -> List<OrganizerEvent>
) {
    var events by remember { mutableStateOf(emptyList<OrganizerEvent>()) }
    var offset by remember { mutableStateOf(0) }
    var loadRequest by remember { mutableStateOf(0) }
    var hasMore by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(loadRequest) {
        loading = true
        error = null
        try {
            val page = onLoad(offset)
            offset += page.size
            events = (events + page).distinctBy { it.id }
            hasMore = page.size == ORGANIZER_PAGE_SIZE
        } catch (failure: AuthApiException) {
            error = organizerError(failure, R.string.organizer_load_error)
        } catch (_: IOException) {
            error = R.string.organizer_load_error
        } finally {
            loading = false
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 76.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (draftCreated || eventPublished) {
            item {
                Text(
                    stringResource(if (eventPublished) R.string.organizer_event_published else R.string.organizer_draft_saved),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                )
            }
        }
        if (!loading && error == null && events.isEmpty()) {
            item {
                ProfilePanel {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(stringResource(R.string.organizer_empty_title), style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(R.string.organizer_empty_hint),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        items(events, key = { it.id }) { event -> OrganizerEventCard(event) { onManage(event.id) } }
        if (loading) {
            item {
                if (events.isEmpty()) {
                    EventLoading(EventLoadingLayout.OrganizerList)
                } else {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                }
            }
        }
        error?.let { message ->
            item {
                ProfileFeedback(message)
                TextButton(onClick = { loadRequest++ }) { Text(stringResource(R.string.onboarding_retry)) }
            }
        }
        if (hasMore && !loading && error == null) {
            item {
                OutlinedButton(
                    onClick = { loadRequest++ },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                ) { Text(stringResource(R.string.organizer_more)) }
            }
        }
    }
}

@Composable
private fun OrganizerEventCard(event: OrganizerEvent, onManage: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val status = when (event.status) {
        "draft" -> R.string.organizer_status_draft
        "published" -> R.string.organizer_status_published
        "cancelled" -> R.string.organizer_status_cancelled
        else -> R.string.organizer_status_unknown
    }
    ProfilePanel {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            PublicEventCover(
                event.coverUrl,
                Modifier.widthIn(max = 168.dp).weight(0.42f, fill = false).fillMaxWidth()
                    .aspectRatio(4f / 3f).clip(RoundedCornerShape(11.dp))
            )
            Column(Modifier.weight(0.58f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    event.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                val date = remember(event.startsAt) {
                    parseOrganizerDate(event.startsAt)?.let(::formatOrganizerDate)
                }
                date?.let { EventMetadata(R.drawable.ic_home_calendar, it) }
                event.address?.takeIf { it.isNotBlank() }?.let {
                    EventMetadata(R.drawable.ic_home_pin, it, maxLines = 2)
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    itemVerticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (event.status == "published") colors.primaryContainer else colors.surfaceVariant,
                        contentColor = if (event.status == "published") colors.onPrimaryContainer else colors.onSurfaceVariant
                    ) {
                        Text(
                            stringResource(status),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    TextButton(
                        onClick = onManage,
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(stringResource(R.string.organizer_manage_action))
                    }
                }
            }
        }
    }
}

@Composable
internal fun EventDetailsForm(
    event: OrganizerEvent? = null,
    saving: Boolean,
    onSaving: (Boolean) -> Unit,
    scroll: Boolean = true,
    onSave: suspend (EventDraft, ByteArray?, Boolean) -> Unit
) {
    val context = LocalContext.current
    val focus = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    var title by rememberSaveable { mutableStateOf(event?.title.orEmpty()) }
    var description by rememberSaveable { mutableStateOf(event?.description.orEmpty()) }
    var address by rememberSaveable { mutableStateOf(event?.address.orEmpty()) }
    var startsAt by rememberSaveable { mutableStateOf(parseOrganizerDate(event?.startsAt)) }
    var latitude by rememberSaveable { mutableStateOf(event?.latitude?.toString().orEmpty()) }
    var longitude by rememberSaveable { mutableStateOf(event?.longitude?.toString().orEmpty()) }
    var cover by rememberSaveable { mutableStateOf<Uri?>(null) }
    var error by remember { mutableStateOf<Int?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            cover = uri
            error = null
        }
    }
    val lat = latitude.trim().replace(',', '.').toDoubleOrNull()
    val lon = longitude.trim().replace(',', '.').toDoubleOrNull()
    val locationValid = (latitude.isBlank() && longitude.isBlank()) ||
        (lat != null && lat in -90.0..90.0 && lon != null && lon in -180.0..180.0)
    val locationLocked = event?.localities.orEmpty().any { it.soldCount > 0 }
    val canSave = !saving && title.isNotBlank() && locationValid

    fun save(next: Boolean) {
        if (!canSave) return
        focus.clearFocus()
        onSaving(true)
        error = null
        val initialTime = parseOrganizerDate(event?.startsAt)
        val date = if (event != null && initialTime == startsAt) event.startsAt else startsAt?.let {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ROOT).apply {
                timeZone = hondurasTime
            }.format(Date(it))
        }
        val savedAddress = if (locationLocked) event?.address.orEmpty() else address.trim()
        val draft = EventDraft(title.trim(), description.trim(), savedAddress, date, lat, lon)
        scope.launch {
            try {
                val image = cover?.let { readEventCover(context.contentResolver, it) }
                onSave(draft, image, next)
            } catch (_: CoverTooLargeException) {
                error = R.string.organizer_cover_large
            } catch (_: SecurityException) {
                error = R.string.organizer_cover_read_error
            } catch (failure: AuthApiException) {
                error = organizerError(failure, if (event == null) R.string.organizer_create_error else R.string.organizer_details_error)
            } catch (_: IOException) {
                error = if (event == null) R.string.organizer_create_error else R.string.organizer_details_error
            } finally {
                onSaving(false)
            }
        }
    }

    Column(
        if (scroll) Modifier.verticalScroll(rememberScrollState()) else Modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        EventCoverPicker(
            cover = cover,
            savedCoverUrl = event?.coverUrl,
            enabled = !saving,
            onPick = {
                focus.clearFocus()
                picker.launch("image/*")
            },
            onRemove = {
                cover = null
                error = null
            }
        )
        if (event == null) Text(
            stringResource(R.string.organizer_create_hint),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
        DraftField(title, { title = it }, R.string.organizer_title, saving)
        DraftField(description, { description = it }, R.string.organizer_description, saving, multiline = true)
        DraftField(address, { address = it }, R.string.organizer_address, saving || locationLocked)
        if (event != null) {
            DraftField(latitude, { latitude = it }, R.string.organizer_latitude, saving || locationLocked, keyboardType = KeyboardType.Decimal)
            DraftField(longitude, { longitude = it }, R.string.organizer_longitude, saving || locationLocked, keyboardType = KeyboardType.Decimal)
            if (!locationValid) ProfileFeedback(R.string.organizer_coordinates_error)
            if (locationLocked) Text(stringResource(R.string.organizer_location_locked), style = MaterialTheme.typography.bodySmall)
        }
        val selectedDate = startsAt
        OutlinedButton(
            onClick = {
                focus.clearFocus()
                val date = Calendar.getInstance(hondurasTime)
                if (selectedDate != null) date.timeInMillis = selectedDate
                DatePickerDialog(context, { _, year, month, day ->
                    TimePickerDialog(context, { _, hour, minute ->
                        date.clear()
                        date.set(year, month, day, hour, minute)
                        startsAt = date.timeInMillis
                    }, date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE),
                        android.text.format.DateFormat.is24HourFormat(context)).show()
                }, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH)).show()
            },
            enabled = !saving && !locationLocked,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
        ) {
            Text(selectedDate?.let(::formatOrganizerDate) ?: stringResource(R.string.organizer_select_date))
        }
        if (selectedDate != null && !locationLocked) {
            TextButton(onClick = { startsAt = null }, enabled = !saving) {
                Text(stringResource(R.string.organizer_remove_date))
            }
        }
        error?.let { ProfileFeedback(it) }
        AuthPrimaryButton(
            label = stringResource(when {
                saving -> R.string.organizer_saving
                event == null -> R.string.organizer_next
                else -> R.string.organizer_save_details
            }),
            onClick = { save(event == null) },
            color = MaterialTheme.colorScheme.primary,
            enabled = canSave
        )
        if (event == null) {
            OutlinedButton(
                onClick = { save(false) },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            ) { Text(stringResource(R.string.organizer_save_draft)) }
        }
    }
}

@Composable
private fun EventCoverPicker(
    cover: Uri?,
    savedCoverUrl: String?,
    enabled: Boolean,
    onPick: () -> Unit,
    onRemove: () -> Unit
) {
    val context = LocalContext.current
    val resolver = context.contentResolver
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(16.dp)
    val selectLabel = stringResource(R.string.organizer_select_cover)
    val changeLabel = stringResource(R.string.organizer_replace_cover)
    val hasCover = cover != null || savedCoverUrl != null
    var preview by remember(cover, savedCoverUrl) {
        mutableStateOf(if (cover == null) savedCoverUrl?.let(EventCoverCache::get) else null)
    }
    var loading by remember(cover, savedCoverUrl) { mutableStateOf(hasCover && preview == null) }
    LaunchedEffect(resolver, cover, savedCoverUrl) {
        if (loading) {
            preview = if (cover != null) {
                loadCoverPreview { decodeCoverPreview(resolver, cover) }
            } else {
                savedCoverUrl?.let { EventCoverCache.load(context.applicationContext, it) }
            }
            loading = false
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val image = preview
        val previewSize = if (image == null) {
            Modifier.heightIn(min = 144.dp).padding(12.dp)
        } else {
            Modifier.aspectRatio(image.width.toFloat() / image.height)
        }
        Box(
            modifier = Modifier.fillMaxWidth()
                .clip(shape)
                .background(colors.surface)
                .drawWithContent {
                    drawContent()
                    val line = 1.dp.toPx()
                    drawRoundRect(
                        color = colors.outline,
                        topLeft = Offset(line / 2, line / 2),
                        size = Size(size.width - line, size.height - line),
                        cornerRadius = CornerRadius(16.dp.toPx() - line / 2),
                        style = Stroke(
                            width = line,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()))
                        )
                    )
                }
                .clickable(
                    enabled = enabled,
                    role = Role.Button,
                    onClickLabel = if (hasCover) changeLabel else selectLabel,
                    onClick = onPick
                )
                .then(previewSize),
            contentAlignment = Alignment.Center
        ) {
            when {
                image != null -> Image(
                    bitmap = image.asImageBitmap(),
                    contentDescription = stringResource(R.string.organizer_cover_preview),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                loading -> CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                else -> Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painterResource(R.drawable.ic_organizer_add),
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        if (hasCover) changeLabel else selectLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = colors.primary
                    )
                }
            }
        }
        if (hasCover) {
            if (!loading && preview == null) ProfileFeedback(R.string.organizer_cover_preview_error)
        }
        if (cover != null) {
            TextButton(onClick = onRemove, enabled = enabled) {
                Text(stringResource(R.string.organizer_remove_cover))
            }
        }
    }
}

private fun decodeCoverPreview(resolver: ContentResolver, cover: Uri): Bitmap? {
    val maxSide = 1024
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        return ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, cover)) { decoder, info, _ ->
            val largest = maxOf(info.size.width, info.size.height)
            if (largest > maxSide) {
                val scale = maxSide.toDouble() / largest
                decoder.setTargetSize(
                    (info.size.width * scale).toInt().coerceAtLeast(1),
                    (info.size.height * scale).toInt().coerceAtLeast(1)
                )
            }
        }
    }

    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(cover)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    val largest = maxOf(bounds.outWidth, bounds.outHeight)
    if (largest <= 0) return null
    var sample = 1
    while (largest / sample > maxSide) sample *= 2
    val options = BitmapFactory.Options().apply { inSampleSize = sample }
    return resolver.openInputStream(cover)?.use { BitmapFactory.decodeStream(it, null, options) }
}

@Composable
internal fun DraftField(
    value: String,
    onValueChange: (String) -> Unit,
    label: Int,
    saving: Boolean,
    multiline: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val colors = MaterialTheme.colorScheme
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(label)) },
        enabled = !saving,
        singleLine = !multiline,
        minLines = if (multiline) 3 else 1,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, keyboardType = keyboardType),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colors.surface,
            unfocusedContainerColor = colors.surface,
            disabledContainerColor = colors.surface,
            errorContainerColor = colors.surface,
            unfocusedBorderColor = colors.outlineVariant
        ),
        modifier = Modifier.fillMaxWidth().dismissKeyboardTarget()
    )
}

internal fun formatOrganizerDate(millis: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.forLanguageTag("es-HN"))
        .apply { timeZone = hondurasTime }
        .format(Date(millis))

internal fun organizerError(error: AuthApiException, fallback: Int): Int = when (error.status) {
    401 -> R.string.organizer_session_error
    403 -> R.string.organizer_permission_error
    404 -> R.string.organizer_event_missing
    409 -> R.string.organizer_conflict
    413 -> R.string.organizer_cover_large
    415 -> R.string.organizer_cover_format
    else -> fallback
}

internal fun parseOrganizerDate(raw: String?): Long? = try {
    raw?.replace(Regex("\\.\\d+(?=Z|[+-])"), "")?.let {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ROOT).parse(it)?.time
    }
} catch (_: ParseException) {
    null
}

private suspend fun loadCoverPreview(decode: () -> Bitmap?): Bitmap? = withContext(Dispatchers.IO) {
    try {
        decode()
    } catch (_: IOException) {
        null
    } catch (_: SecurityException) {
        null
    }
}
