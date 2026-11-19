package dev.josegaldamez.app_tickethn_grupo3_final_project.ui.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.zxing.BinaryBitmap
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.ReaderException
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import dev.josegaldamez.app_tickethn_grupo3_final_project.R
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.AuthApiException
import dev.josegaldamez.app_tickethn_grupo3_final_project.data.ScannedTicket
import dev.josegaldamez.app_tickethn_grupo3_final_project.ui.access.AuthPrimaryButton
import java.io.IOException
import java.util.concurrent.Executors
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch

@Composable
internal fun TicketScanner(
    onLookupTicket: suspend (String) -> ScannedTicket,
    onCheckIn: suspend (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var qrContent by rememberSaveable { mutableStateOf<String?>(null) }
    var lookupRetry by remember(qrContent) { mutableStateOf(0) }
    var ticket by remember(qrContent, lookupRetry) { mutableStateOf<ScannedTicket?>(null) }
    var lookupError by remember(qrContent, lookupRetry) { mutableStateOf<Int?>(null) }
    var checkInMessage by rememberSaveable { mutableStateOf<Int?>(null) }
    var checkingIn by remember { mutableStateOf(false) }
    var cameraActive by remember { mutableStateOf(false) }
    var cameraError by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(qrContent, lookupRetry) {
        val payload = qrContent ?: return@LaunchedEffect
        try {
            ticket = onLookupTicket(payload)
        } catch (error: AuthApiException) {
            lookupError = ticketScannerError(error, R.string.validator_lookup_error)
        } catch (_: IOException) {
            lookupError = R.string.validator_lookup_error
        }
    }

    val requestPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        cameraActive = granted
        cameraError = if (granted) null else R.string.validator_camera_permission
    }

    if (cameraActive) {
        Text(stringResource(R.string.validator_scan_hint), style = MaterialTheme.typography.bodyMedium)
        QrCameraPreview(
            onResult = { content ->
                qrContent = content
                cameraActive = false
            },
            onFailure = {
                cameraError = it
                cameraActive = false
            }
        )
    } else {
        cameraError?.let { ProfileFeedback(it) }
        OutlinedButton(
            onClick = {
                cameraError = null
                qrContent = null
                checkInMessage = null
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    cameraActive = true
                } else {
                    requestPermission.launch(Manifest.permission.CAMERA)
                }
            },
            enabled = !checkingIn,
            modifier = Modifier.fillMaxWidth()
        ) {
            val label = if (qrContent == null) R.string.validator_open_camera else R.string.validator_scan_again
            Text(stringResource(label))
        }
    }

    val lookup = ticket
    val errorMessage = lookupError
    when {
        lookup != null -> ScannedTicketPanel(lookup)
        errorMessage != null -> {
            ProfileFeedback(errorMessage)
            if (errorMessage == R.string.validator_lookup_error) {
                TextButton(onClick = { lookupRetry++ }) { Text(stringResource(R.string.onboarding_retry)) }
            }
        }
        qrContent != null -> Text(
            stringResource(R.string.validator_lookup_loading),
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
        )
    }
    val canCheckIn = !cameraActive && !checkingIn && lookup?.canCheckIn == true &&
        checkInMessage != R.string.validator_check_in_success &&
        checkInMessage != R.string.validator_ticket_used &&
        checkInMessage != R.string.validator_ticket_not_eligible
    AuthPrimaryButton(
        label = stringResource(if (checkingIn) R.string.validator_checking_in else R.string.validator_validate_ticket),
        onClick = {
            val payload = qrContent
            if (canCheckIn && !checkingIn && !payload.isNullOrBlank()) {
                checkingIn = true
                checkInMessage = null
                scope.launch {
                    try {
                        onCheckIn(payload)
                        checkInMessage = R.string.validator_check_in_success
                    } catch (error: AuthApiException) {
                        checkInMessage = ticketScannerError(error, R.string.validator_check_in_error)
                    } catch (_: IOException) {
                        checkInMessage = R.string.validator_check_in_error
                    } finally {
                        checkingIn = false
                    }
                }
            }
        },
        color = MaterialTheme.colorScheme.primary,
        enabled = canCheckIn
    )
    val admissionMessage = when {
        checkInMessage != null -> checkInMessage
        lookup?.usedAt != null -> R.string.validator_ticket_used
        lookup?.canCheckIn == false -> R.string.validator_ticket_not_eligible
        else -> null
    }
    admissionMessage?.let { message ->
        if (message == R.string.validator_check_in_success) {
            Text(
                stringResource(message),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
            )
        } else {
            ProfileFeedback(message)
        }
    }
}

@Composable
private fun ScannedTicketPanel(ticket: ScannedTicket) {
    ProfilePanel {
        Column(
            modifier = Modifier.padding(16.dp).semantics { liveRegion = LiveRegionMode.Polite },
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Column {
                Text(
                    stringResource(R.string.validator_buyer),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    ticket.buyerName,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.semantics { heading() }
                )
            }
            ScannedTicketField(R.string.validator_event, ticket.eventTitle)
            ScannedTicketField(R.string.home_ticket_admission, ticket.ticketTypeTitle)
            val purchasedAt = parseOrganizerDate(ticket.purchasedAt)?.let(::formatOrganizerDate) ?: ticket.purchasedAt
            ScannedTicketField(R.string.validator_purchase_date, purchasedAt)
            ScannedTicketField(R.string.home_ticket_access_code, ticket.code)
        }
    }
}

@Composable
private fun ScannedTicketField(label: Int, value: String) {
    Column {
        Text(
            stringResource(label),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium
        )
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun ticketScannerError(error: AuthApiException, fallback: Int): Int = when (error.code) {
    "INVALID_QR" -> R.string.validator_invalid_qr
    "NOT_FOUND" -> R.string.validator_ticket_not_found
    "TICKET_ALREADY_USED" -> R.string.validator_ticket_used
    "TICKET_NOT_ELIGIBLE" -> R.string.validator_ticket_not_eligible
    else -> when (error.status) {
        401 -> R.string.validator_session_error
        403 -> R.string.validator_permission_error
        else -> fallback
    }
}

@Composable
private fun QrCameraPreview(onResult: (String) -> Unit, onFailure: (Int) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val preview = remember { Preview.Builder().build() }

    AndroidView(
        factory = { viewContext ->
            PreviewView(viewContext).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                scaleType = PreviewView.ScaleType.FIT_CENTER
                preview.setSurfaceProvider(surfaceProvider)
            }
        },
        modifier = Modifier.fillMaxWidth().height(260.dp).clip(RoundedCornerShape(16.dp))
    )

    LaunchedEffect(context, lifecycleOwner, preview) {
        val executor = Executors.newSingleThreadExecutor()
        val analysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
        var provider: ProcessCameraProvider? = null
        try {
            val cameraProvider = ProcessCameraProvider.awaitInstance(context.applicationContext)
            provider = cameraProvider
            val camera = if (cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
                CameraSelector.DEFAULT_BACK_CAMERA
            } else {
                CameraSelector.DEFAULT_FRONT_CAMERA
            }

            val reader = QRCodeReader()
            var scanned = false
            analysis.setAnalyzer(executor) { image ->
                val content = try {
                    if (scanned) null else image.readQr(reader)
                } finally {
                    image.close()
                }
                if (content != null) {
                    scanned = true
                    launch { onResult(content) }
                }
            }
            cameraProvider.bindToLifecycle(lifecycleOwner, camera, preview, analysis)
            awaitCancellation()
        } catch (error: CancellationException) {
            throw error
        } catch (_: SecurityException) {
            onFailure(R.string.validator_camera_permission)
        } catch (_: Exception) {
            onFailure(R.string.validator_camera_error)
        } finally {
            analysis.clearAnalyzer()
            provider?.unbind(preview, analysis)
            executor.shutdown()
        }
    }
}

private fun ImageProxy.readQr(reader: QRCodeReader): String? {
    val luminance = planes[0]
    val buffer = luminance.buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    // The Y plane can have padding at the end of each row.
    val source = PlanarYUVLuminanceSource(bytes, luminance.rowStride, height, 0, 0, width, height, false)
    return try {
        reader.decode(BinaryBitmap(HybridBinarizer(source))).text
    } catch (_: ReaderException) {
        null
    }
}
