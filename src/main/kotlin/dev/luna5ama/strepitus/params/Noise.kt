package dev.luna5ama.strepitus.params

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import io.github.composefluent.*
import io.github.composefluent.component.*
import io.github.composefluent.icons.*
import io.github.composefluent.icons.regular.*
import kotlinx.serialization.Transient
import java.math.BigDecimal

enum class CompositeMode {
    Add,
    Subtract,
    Multiply
}

data class NoiseLayerParameters<T : NoiseSpecificParameters>(
    val enabled: Boolean,
    val compositeMode: CompositeMode,
    val specificParameters: T,
    @Transient
    val expanded: Boolean = true,
)

enum class DistanceFunction {
    Euclidean,
    Manhattan,
    Chebyshev
}

@Immutable
sealed interface NoiseSpecificParameters {
    data class Value(
        val value: BigDecimal = 0.0.toBigDecimal(),
    ) : NoiseSpecificParameters

    data class Perlin(
        val rotated: Boolean = false,
    ) : NoiseSpecificParameters

    data class Simplex(
        val rotated: Boolean = false,
    ) : NoiseSpecificParameters

    data class Worley(
        val distanceFunction: DistanceFunction = DistanceFunction.Euclidean,
    )
}

@Composable
fun NoiseLayerEditor(
    layers: SnapshotStateList<NoiseLayerParameters<*>>,
) {
    var deletingIndex by remember { mutableIntStateOf(-1) }
    ContentDialog(
        title = "Delete Layer",
        visible = deletingIndex in layers.indices,
        size = DialogSize.Companion.Standard,
        primaryButtonText = "Delete",
        onButtonClick = {
            if (it == ContentDialogButton.Primary) {
                if (deletingIndex in layers.indices) {
                    layers.removeAt(deletingIndex)
                }
            } else {
                deletingIndex = -1
            }
        },
        secondaryButtonText = "Cancel",
        content = {
            Text("Are you sure you want to delete this layer?")
        }
    )
//        Button(
//            onClick = {
//                if (deletingIndex in layers.indices) {
//                    layers.removeAt(deletingIndex)
//                }
//                deletingIndex = -1
//            }
//        ) {
//            Text("Delete")
//        }
//        Button(
//            onClick = {
//                deletingIndex = -1
//            }
//        ) {
//            Text("Cancel")
//        }
    layers.forEachIndexed { i, layer ->
        Expander(
            layer.expanded,
            onExpandedChanged = { layers[i] = layer.copy(expanded = it) },
            icon = {
                Spacer(modifier = Modifier.Companion.width(FluentTheme.typography.subtitle.fontSize.value.dp * 3.0f))
                Icon(
                    imageVector = Icons.Default.ReOrderDotsVertical,
                    contentDescription = "",
                    modifier = Modifier.Companion.size(FluentTheme.typography.subtitle.fontSize.value.dp)
                )
            },
            heading = {
                Text(layer.specificParameters::class.simpleName!!, style = FluentTheme.typography.subtitle)
            },
            trailing = {
                Button(
                    onClick = {
                        deletingIndex = i
                    },
                    iconOnly = true
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Layer"
                    )
                }
            }
        ) {

        }
    }
}