package dev.josegaldamez.app_tickethn_grupo3_final_project.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

private class TextInput(val editable: Boolean) {
    var coordinates: LayoutCoordinates? = null
    var focused = false
}

private val LocalTextInputs = staticCompositionLocalOf<MutableSet<TextInput>?> { null }

@Composable
internal fun DismissKeyboardOnOutsideTap(content: @Composable () -> Unit) {
    val inputs = remember { mutableSetOf<TextInput>() }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val containerCoordinates = remember { mutableStateOf<LayoutCoordinates?>(null) }

    CompositionLocalProvider(LocalTextInputs provides inputs) {
        Box(
            Modifier.fillMaxSize()
                .onGloballyPositioned { containerCoordinates.value = it }
                .pointerInput(focusManager, keyboard) {
                    awaitEachGesture {
                        val pass = PointerEventPass.Initial
                        val down = awaitFirstDown(requireUnconsumed = false, pass = pass)
                        val focusedInput = inputs.firstOrNull { it.focused } ?: return@awaitEachGesture
                        val coordinates = containerCoordinates.value?.takeIf { it.isAttached }
                            ?: return@awaitEachGesture
                        val position = coordinates.localToRoot(down.position)
                        val insideInput = inputs.any { input ->
                            (input == focusedInput || input.editable) &&
                                input.coordinates?.takeIf { it.isAttached }
                                    ?.boundsInRoot()?.contains(position) == true
                        }
                        if (insideInput) return@awaitEachGesture

                        // Observe the tap before a child handles it, leaving the event available to that child.
                        do {
                            val event = awaitPointerEvent(pass)
                            val change = event.changes.singleOrNull()?.takeIf { it.id == down.id }
                                ?: return@awaitEachGesture
                            if ((change.position - down.position).getDistance() > viewConfiguration.touchSlop) {
                                return@awaitEachGesture
                            }
                            if (change.changedToUpIgnoreConsumed() &&
                                change.uptimeMillis - down.uptimeMillis < viewConfiguration.longPressTimeoutMillis
                            ) {
                                val currentInput = inputs.firstOrNull { it.focused }
                                if (currentInput === focusedInput) focusManager.clearFocus()
                                if (currentInput === focusedInput || currentInput?.editable != true) keyboard?.hide()
                            }
                        } while (change.pressed)
                    }
                }
        ) {
            content()
        }
    }
}

internal fun Modifier.dismissKeyboardTarget(editable: Boolean = true): Modifier = composed {
    val inputs = LocalTextInputs.current ?: return@composed Modifier
    val input = remember(editable) { TextInput(editable) }
    DisposableEffect(inputs, input) {
        inputs.add(input)
        onDispose { inputs.remove(input) }
    }
    Modifier.onGloballyPositioned { input.coordinates = it }
        .onFocusChanged { input.focused = it.isFocused }
}
