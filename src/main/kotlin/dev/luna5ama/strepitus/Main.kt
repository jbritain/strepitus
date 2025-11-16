package dev.luna5ama.strepitus

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.awt.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import io.github.composefluent.*
import io.github.composefluent.component.*
import io.github.composefluent.component.rememberScrollbarAdapter
import javax.swing.BoxLayout
import javax.swing.JPanel

fun main() {
    application {
        Window(
            onCloseRequest = ::exitApplication,
            icon = null,
            title = "Strepitus",
            state = rememberWindowState(width = 1920.dp, height = 1080.dp)
        ) {
            var mainParameters by remember { mutableStateOf(MainParameters()) }
            var outputProcessingParameters by remember { mutableStateOf(OutputProcessingParameters()) }
            var viewerParameters by remember { mutableStateOf(ViewerParameters()) }

            FluentTheme {
                Row {
                    val scrollState = rememberScrollState()
                    ScrollbarContainer(
                        adapter = rememberScrollbarAdapter(scrollState),
                    ) {
                        Column(
                            modifier = Modifier
                                .width(320.dp)
                                .fillMaxHeight()
                                .padding(8.dp)
                                .verticalScroll(scrollState)
                        ) {
//                            MainEditor(
//                                mainParameters,
//                                { mainParameters = it }
//                            )
                            ParameterEditor(
                                mainParameters,
                                { mainParameters = it }
                            )
                            ParameterEditor(
                                outputProcessingParameters,
                                { outputProcessingParameters = it }
                            )
                            ParameterEditor(
                                viewerParameters,
                                { viewerParameters = it }
                            )
                        }
                    }
                    NoiseGeneratorPanel(
                        mainParameters,
                        outputProcessingParameters,
                        viewerParameters,
                        onScroll = {
                            viewerParameters =
                                viewerParameters.copy(zoom = viewerParameters.zoom - it.toBigDecimal() * 0.1.toBigDecimal())
                        },
                    )
                }
            }
        }
    }

}

@Composable
fun NoiseGeneratorPanel(
    mainParameters: MainParameters,
    outputProcessingParameters: OutputProcessingParameters,
    viewerParameters: ViewerParameters,
    onScroll: (wheelRotation: Int) -> Unit,
) {
    val noiseGenerator by remember {
        mutableStateOf(
            LWJGLCanvas {
                NoiseGeneratorRenderer(
                    mainParameters,
                    outputProcessingParameters,
                    viewerParameters,
                )
            }
        )
    }
    if (noiseGenerator.mouseWheelListeners.isEmpty()) {
        noiseGenerator.addMouseWheelListener { onScroll(it.wheelRotation) }
    } else {
        noiseGenerator.mouseWheelListeners[0] = { onScroll(it.wheelRotation) }
    }
    SwingPanel(
        modifier = Modifier.fillMaxSize(),
        factory = {
            JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                add(noiseGenerator)
            }
        }
    )

    LaunchedEffect(Unit) {
        noiseGenerator.redraw()
    }

    noiseGenerator.update {
        it.mainParameters = mainParameters
        it.outputProcessingParameters = outputProcessingParameters
        it.viewerParameters = viewerParameters
    }

    DisposableEffect(Unit) {
        object : DisposableEffectResult {
            override fun dispose() {
                noiseGenerator.destroy()
            }
        }
    }
}