package dev.luna5ama.strepitus

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.unit.*
import dev.luna5ama.strepitus.params.*
import io.github.composefluent.*
import io.github.composefluent.component.*
import io.github.composefluent.component.rememberScrollbarAdapter
import io.github.composefluent.icons.*
import io.github.composefluent.icons.regular.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose
import org.lwjgl.system.MemoryStack
import org.lwjgl.util.nfd.NFDFilterItem
import org.lwjgl.util.nfd.NFDSaveDialogArgs
import org.lwjgl.util.nfd.NativeFileDialog
import java.math.MathContext
import java.math.RoundingMode
import kotlin.io.path.Path
import kotlin.io.path.inputStream
import kotlin.io.path.readText
import kotlin.io.path.writeText

val roundingMode = MathContext(4, RoundingMode.FLOOR)

class AppState(
    private val windowHandle: Long,
    val scope: CoroutineScope,
) {
    var mainParameters by mutableStateOf(MainParameters())
    val noiseLayers = mutableStateListOf(
        NoiseLayerParameters(
            baseSeed = NoiseLayerParameters.generateBaseSeed(0)
        )
    )
    var outputParameters by mutableStateOf(OutputParameters())
    var viewerParameters by mutableStateOf(ViewerParameters())

    var systemParameters by mutableStateOf(SystemParameters())

    fun exitApp() {
        glfwSetWindowShouldClose(windowHandle, true)
    }

    fun load() {
        runCatching {
            loadSystem(SYSTEM_CONFIG_PATH)
        }
        runCatching {
            loadNoise(NOISE_CONFIG_PATH)
        }
    }

    fun save() {
        runCatching {
            saveSystem(SYSTEM_CONFIG_PATH)
        }
        runCatching {
            saveNoise(NOISE_CONFIG_PATH)
        }
    }

    fun loadNoise(path: java.nio.file.Path) {
        path.inputStream().use {
            val config = JSON.decodeFromString<NoiseConfig>(path.readText())
            mainParameters = config.mainParameters
            outputParameters = config.outputParameters
            viewerParameters = config.viewerParameters
            noiseLayers.clear()
            noiseLayers.addAll(config.noiseLayers)
        }
    }

    fun saveNoise(path: java.nio.file.Path) {
        val config = NoiseConfig(
            mainParameters = mainParameters,
            noiseLayers = noiseLayers.toList(),
            outputParameters = outputParameters,
            viewerParameters = viewerParameters
        )
        path.writeText(JSON.encodeToString(config))
    }

    fun saveSystem(path: java.nio.file.Path) {
        path.writeText(JSON.encodeToString(systemParameters))
    }

    fun loadSystem(path: java.nio.file.Path) {
        systemParameters = JSON.decodeFromString(path.readText())
    }

    @Serializable
    data class NoiseConfig(
        val mainParameters: MainParameters,
        val noiseLayers: List<NoiseLayerParameters>,
        val outputParameters: OutputParameters,
        val viewerParameters: ViewerParameters
    )

    companion object {
        val SYSTEM_CONFIG_PATH = Path("system.json")
        val NOISE_CONFIG_PATH = Path("noise.json")

        @OptIn(ExperimentalSerializationApi::class)
        val JSON = Json {
            prettyPrint = true
            encodeDefaults = true
            prettyPrintIndent = "    "
            namingStrategy = JsonNamingStrategy.SnakeCase
        }
    }
}

@Composable
fun MenuFlyoutScope.MenuFlyoutButton(
    onClick: () -> Unit,
    icon: ImageVector? = null,
    text: String,
    trailingText: String? = null,
    enabled: Boolean = true,
) {
    MenuFlyoutItem(
        onClick = onClick,
        icon = icon?.let { { Icon(imageVector = it, contentDescription = null) } },
        text = { Text(text) },
        trailing = trailingText?.let {
            {
                Spacer(Modifier.width(16.dp))
                Text(it, style = FluentTheme.typography.caption)
            }
        },
        enabled = enabled,
    )
}

@Suppress("AssignedValueIsNeverRead")
@Composable
fun App(renderer: NoiseGeneratorRenderer, appState: AppState) {
    var mainParameters by appState::mainParameters
    var outputParameters by appState::outputParameters
    var systemParameters by appState::systemParameters

    val darkMode = when (systemParameters.darkMode) {
        DarkModeOption.Auto -> isSystemInDarkTheme()
        DarkModeOption.Light -> false
        DarkModeOption.Dark -> true
    }

    var errorPrompt by remember { mutableStateOf<String?>(null) }
    errorPrompt?.let { message ->
        ContentDialog(
            title = "Error",
            visible = true,
            primaryButtonText = "OK",
            onButtonClick = {
                errorPrompt = null
            },
            content = { Text(message) }
        )
    }

    var exportingImage by remember { mutableStateOf(false) }
    var exportingFormat by remember { mutableStateOf(OutputFileFormat.PNG) }

    if (exportingImage) {
        exportingImage = false
        val outputFormat = exportingFormat
        MemoryStack.stackPush().use {
            val filters = NFDFilterItem.calloc(1, it)
            filters[0]
                .name(it.UTF8("${outputFormat.fullName} File"))
                .spec(it.UTF8(outputFormat.extensions.joinToString(",")))
            val args = NFDSaveDialogArgs.calloc(it)
                .filterList(filters)
            val savePath = it.mallocPointer(1)
            when (NativeFileDialog.NFD_SaveDialog_With(savePath, args)) {
                NativeFileDialog.NFD_OKAY -> {
                    val path = Path(savePath.getStringUTF8(0))
                    appState.scope.launch {
                        runCatching {
                            renderer.saveImage(path, outputFormat)
                        }.onFailure { ex ->
                            errorPrompt = "Failed to export image:\n${ex.message}"
                        }
                    }
                }

                NativeFileDialog.NFD_CANCEL -> {
                    // User cancelled
                }

                else -> {
                    errorPrompt = NativeFileDialog.NFD_GetError().toString()
                }
            }
        }
    }

    FluentTheme(
        colors = if (darkMode) darkColors() else lightColors(),
    ) {
        Column(
            modifier = Modifier
                .background(color = Color.Transparent)
        ) {
            MenuBar(
                modifier = Modifier
                    .background(color = FluentTheme.colors.background.mica.base)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 0.dp)
            ) {
                MenuBarItem(
                    items = {
                        MenuFlyoutButton(
                            onClick = { /* TODO */ },
                            icon = Icons.Default.Document,
                            text = "New Project",
                            trailingText = "Ctrl+N"
                        )
                        MenuFlyoutButton(
                            onClick = { /* TODO */ },
                            icon = Icons.Default.FolderOpen,
                            text = "Open Project",
                            trailingText = "Ctrl+O"
                        )
                        MenuFlyoutButton(
                            onClick = { /* TODO */ },
                            icon = Icons.Default.Save,
                            text = "Save Project",
                            trailingText = "Ctrl+S"
                        )
                        MenuFlyoutSeparator()
                        MenuFlyoutItem(
                            text = { Text("Export Texture") },
                            icon = { Icon(imageVector = Icons.Default.SaveArrowRight, contentDescription = null) },
                            items = {
                                fun isFormatEnabled(format: OutputFileFormat): Boolean {
                                    if ((mainParameters.slices > 1) && format.only2D) {
                                        return false
                                    }
                                    val outputSpec = outputParameters.format.outputSpec
                                    if (!format.supportedChannelCount.contains(outputSpec.channels)) {
                                        return false
                                    }
                                    if (!format.supportedPixelType.contains(outputSpec.pixelType)) {
                                        return false
                                    }
                                    return true
                                }

                                OutputFileFormat.entries.forEach { format ->
                                    MenuFlyoutButton(
                                        onClick = {
                                            exportingFormat = format
                                            exportingImage = true
                                        },
                                        text = format.name,
                                        enabled = isFormatEnabled(format)
                                    )
                                }
                            }
                        )
                        MenuFlyoutSeparator()
                        MenuFlyoutButton(
                            onClick = { appState.exitApp() },
                            icon = Icons.Default.Dismiss,
                            text = "Exit"
                        )
                    }
                ) {
                    Text("File")
                }
            }
            Row {
                Box(
                    modifier = Modifier
                        .background(color = FluentTheme.colors.background.mica.base)
                ) {
                    SideEditor(renderer, appState)
                }
                NoiseViewer(renderer, appState)
            }
        }
    }

}

@OptIn(ExperimentalFoundationApi::class, ExperimentalFluentApi::class)
@Composable
fun SideEditor(renderer: NoiseGeneratorRenderer, appState: AppState) {
    var mainParameters by appState::mainParameters
    var outputParameters by appState::outputParameters
    var viewerParameters by appState::viewerParameters
    var systemParameters by appState::systemParameters
    val noiseLayers by appState::noiseLayers

    Row {
        var sideNavItem by remember { mutableStateOf(SideNavItem.Main) }
        var sideNavExpanded by remember { mutableStateOf(false) }

        @Composable
        fun AppSideNavItem(item: SideNavItem) {
            SideNavItem(
                selected = sideNavItem == item,
                onClick = { if (it) sideNavItem = item },
                icon = {
                    Icon(imageVector = item.icon, contentDescription = "")
                },
            ) {
                Text(item.name)
            }
        }

        SideNav(
            expanded = sideNavExpanded,
            onExpandStateChange = { sideNavExpanded = it },
            footer = {
                val item = SideNavItem.Setting
                AppSideNavItem(item)
            }
        ) {
            SideNavItem.entries.dropLast(1).forEach { item ->
                AppSideNavItem(item)
            }
        }

        val scrollState = rememberScrollState()
        ScrollbarContainer(
            modifier = Modifier
                .background(color = FluentTheme.colors.background.layer.default),
            adapter = rememberScrollbarAdapter(scrollState)
        ) {
            Column(
                modifier = Modifier
                    .width(480.dp)
                    .fillMaxHeight()
                    .padding(8.dp)
                    .verticalScroll(scrollState)
            ) {
                Text(
                    sideNavItem.name,
                    style = FluentTheme.typography.title.copy(color = FluentTheme.colors.text.text.primary),
                    modifier = Modifier.padding(8.dp, vertical = 12.dp)
                )
                when (sideNavItem) {
                    SideNavItem.Main -> {
                        ParameterEditor(
                            mainParameters,
                            { mainParameters = it }
                        )
                    }

                    SideNavItem.Output -> {
                        ParameterEditor(
                            outputParameters,
                            { outputParameters = it }
                        )
                    }

                    SideNavItem.Viewer -> {
                        ParameterEditor(
                            viewerParameters,
                            { viewerParameters = it }
                        )
                    }

                    SideNavItem.Noise -> {
                        NoiseLayerEditor(noiseLayers)
                    }

                    SideNavItem.Setting -> {
                        ParameterEditor(
                            systemParameters,
                            { systemParameters = it }
                        )
                        CardExpanderItem(heading = { }, icon = null) {
                            Button(
                                onClick = { renderer.reloadShaders() },
                                buttonColors = ButtonDefaults.accentButtonColors()
                            ) {
                                Text("Reload Shaders")
                            }
                        }
                    }
                }
            }
        }
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoiseViewer(renderer: NoiseGeneratorRenderer, appState: AppState) {
    var viewerParameters by appState::viewerParameters
    Row {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged {
                    renderer.frameWidth = it.width
                    renderer.frameHeight = it.height
                }
                .scrollable(
                    orientation = Orientation.Vertical,
                    state = rememberScrollableState { delta ->
                        viewerParameters = viewerParameters.copy(
                            zoom = (viewerParameters.zoom + (delta / 1000.0).toBigDecimal()).round(roundingMode)
                        )
                        delta
                    }
                )
                .onDrag {
                    viewerParameters = viewerParameters.copy(
                        centerX = (viewerParameters.centerX - it.x.toBigDecimal()).round(roundingMode),
                        centerY = (viewerParameters.centerY - it.y.toBigDecimal()).round(roundingMode)
                    )
                }
        ) {}
    }
}

enum class SideNavItem(val icon: ImageVector) {
    Main(Icons.Default.Image),
    Noise(Icons.Default.GridDots),
    Output(Icons.Default.Filter),
    Viewer(Icons.Default.Eye),
    Setting(Icons.Default.Settings),
}