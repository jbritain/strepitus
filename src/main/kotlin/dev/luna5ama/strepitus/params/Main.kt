package dev.luna5ama.strepitus.params

import dev.luna5ama.glwrapper.base.*
import io.github.composefluent.icons.*
import io.github.composefluent.icons.regular.*

data class MainParameters(
    val width: Int = 512,
    val height: Int = 512,
    val slices: Int = 1
) {
    companion object {
        val icon = Icons.Default.Image
    }
}

data class OutputSpec(
    val channels: Int,
    val pixelType: Int,
    val pixelSize: Long
) {
    val format
        get() = when (channels) {
            1 -> GL_RED
            2 -> GL_RG
            3 -> GL_RGB
            4 -> GL_RGBA
            else -> throw IllegalArgumentException("Invalid number of channels: $channels")
        }
}