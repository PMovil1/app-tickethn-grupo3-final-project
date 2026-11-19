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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import dev.josegaldamez.app_tickethn_grupo3_final_project.ui.access.AuthPrimaryButton
import java.util.concurrent.Executors
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch

@Composable
internal fun TicketScanner() {
    val context = LocalContext.current
    var qrContent by rememberSaveable { mutableStateOf<String?>(null) }
    var cameraActive by remember { mutableStateOf(false) }
    var cameraError by remember { mutableStateOf<Int?>(null) }

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
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    cameraActive = true
                } else {
                    requestPermission.launch(Manifest.permission.CAMERA)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            val label = if (qrContent == null) R.string.validator_open_camera else R.string.validator_scan_again
            Text(stringResource(label))
        }
    }

    OutlinedTextField(
        value = qrContent.orEmpty(),
        onValueChange = {},
        readOnly = true,
        label = { Text(stringResource(R.string.validator_qr_content)) },
        minLines = 2,
        maxLines = 5,
        modifier = Modifier.fillMaxWidth()
    )
    AuthPrimaryButton(
        label = stringResource(R.string.validator_validate_ticket),
        onClick = {},
        color = MaterialTheme.colorScheme.primary,
        enabled = false
    )
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
