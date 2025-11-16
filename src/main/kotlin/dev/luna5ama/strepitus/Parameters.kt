package dev.luna5ama.strepitus

import androidx.compose.runtime.*
import dev.luna5ama.glwrapper.enums.ImageFormat
import io.github.composefluent.*
import io.github.composefluent.component.*
import java.math.BigDecimal

enum class Format(val value: ImageFormat.Sized) {
    R8_UN(ImageFormat.R8_UN),
    R8G8_UN(ImageFormat.R8G8_UN),
    R8G8B8_UN(ImageFormat.R8G8B8_UN),
    R8G8B8A8_UN(ImageFormat.R8G8B8A8_UN),
    R8_SN(ImageFormat.R8_SN),
    R8G8_SN(ImageFormat.R8G8_SN),
    R8G8B8_SN(ImageFormat.R8G8B8_SN),
    R8G8B8A8_SN(ImageFormat.R8G8B8A8_SN),
    R16_UN(ImageFormat.R16_UN),
    R16G16_UN(ImageFormat.R16G16_UN),
    R16G16B16_UN(ImageFormat.R16G16B16_UN),
    R16G16B16A16_UN(ImageFormat.R16G16B16A16_UN),
    R10G10B10A2_UN(ImageFormat.R10G10B10A2_UN),
}

@Composable
fun MainEditor(
    parameters: MainParameters,
    onChange: (MainParameters) -> Unit
) {
    var expended by remember { mutableStateOf(true) }
    Expander(
        expended,
        onExpandedChanged = { expended = it },
        heading = {
            Text("Main Parameters", style = FluentTheme.typography.subtitle)
        }
    ) {
        CardExpanderItem(heading = { Text("Width") }) {
            IntegerInput(
                value = parameters.width,
                onValueChange = { onChange(parameters.copy(width = it)) }
            )
        }
        CardExpanderItem(heading = { Text("Height") }) {
            IntegerInput(
                value = parameters.height,
                onValueChange = { onChange(parameters.copy(height = it)) }
            )
        }
        CardExpanderItem(heading = { Text("Slices") }) {
            IntegerInput(
                value = parameters.slices,
                onValueChange = { onChange(parameters.copy(slices = it)) }
            )
        }
    }
}

@Composable
fun OutputProcessingEditor(
    parameters: OutputProcessingParameters,
    onChange: (OutputProcessingParameters) -> Unit
) {
    var expended by remember { mutableStateOf(true) }
    Expander(
        expended,
        onExpandedChanged = { expended = it },
        heading = {
            Text("Output Processing", style = FluentTheme.typography.subtitle)
        }
    ) {
        CardExpanderItem(heading = { Text("Format") }) {
            var expandedFormat by remember { mutableStateOf(false) }
            DropDownButton(
                onClick = { expandedFormat = true },
            ) {
                Text(parameters.format.name)
                DropdownMenu(
                    expanded = expandedFormat,
                    onDismissRequest = { expandedFormat = false },
                ) {
                    Format.entries.forEach {
                        DropdownMenuItem(
                            onClick = {
                                onChange(parameters.copy(format = it))
                                expandedFormat = false
                            },
                        ) {
                            Text(it.name)
                        }
                    }
                }
            }
        }
        CardExpanderItem(
            heading = { Text("Normalize") }
        ) {
            ToggleSwitch(
                checked = parameters.normalize,
                onCheckStateChange = { onChange(parameters.copy(normalize = it)) }
            )
        }
        CardExpanderItem(heading = { Text("Min Value") }) {
            DecimalInput(
                value = parameters.minVal,
                onValueChange = { onChange(parameters.copy(minVal = it)) },
                enabled = parameters.normalize,
            )
        }
        CardExpanderItem(heading = { Text("Max Value") }) {
            DecimalInput(
                value = parameters.maxVal,
                onValueChange = { onChange(parameters.copy(maxVal = it)) },
                enabled = parameters.normalize,
            )
        }
        CardExpanderItem(heading = { Text("Flip") }) {
            ToggleSwitch(
                checked = parameters.flip,
                onCheckStateChange = { onChange(parameters.copy(flip = it)) }
            )
        }
        CardExpanderItem(heading = { Text("Dither") }) {
            ToggleSwitch(
                checked = parameters.dither,
                onCheckStateChange = { onChange(parameters.copy(dither = it)) }
            )
        }
    }
}

@Composable
fun ViewerEditor(
    parameters: ViewerParameters,
    onChange: (ViewerParameters) -> Unit
) {
    var expended by remember { mutableStateOf(true) }
    Expander(
        expended,
        onExpandedChanged = { expended = it },
        heading = {
            Text("Viewer", style = FluentTheme.typography.subtitle)
        }
    ) {
        CardExpanderItem(heading = { Text("Center X") }) {
            DecimalInput(
                value = parameters.centerX,
                onValueChange = { onChange(parameters.copy(centerX = it)) }
            )
        }
        CardExpanderItem(heading = { Text("Center Y") }) {
            DecimalInput(
                value = parameters.centerY,
                onValueChange = { onChange(parameters.copy(centerY = it)) }
            )
        }
        CardExpanderItem(heading = { Text("Zoom") }) {
            DecimalInput(
                value = parameters.zoom,
                onValueChange = { onChange(parameters.copy(zoom = it)) }
            )
        }
    }
}

data class MainParameters(
    val width: Int = 512,
    val height: Int = 512,
    val slices: Int = 1
)

data class OutputProcessingParameters(
    val format: Format = Format.R8_UN,
    val normalize: Boolean = true,
    val minVal: BigDecimal = 0.0.toBigDecimal(),
    val maxVal: BigDecimal = 1.0.toBigDecimal(),
    val flip: Boolean = false,
    val dither: Boolean = true,
)

data class ViewerParameters(
    val centerX : BigDecimal = 0.0.toBigDecimal(),
    val centerY : BigDecimal = 0.0.toBigDecimal(),
    val zoom : BigDecimal = 0.0.toBigDecimal(),
)