package dev.luna5ama.strepitus.params

import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.regular.Eye
import java.math.BigDecimal


enum class DisplayColorMode {
    Grayscale,
    Alpha,
    RGB,
}

data class ViewerParameters(
    val colorMode: DisplayColorMode = DisplayColorMode.Grayscale,
    val tilling: Boolean = false,
    @DisplayName("Center X")
    val centerX: BigDecimal = 0.0.toBigDecimal(),
    @DisplayName("Center Y")
    val centerY: BigDecimal = 0.0.toBigDecimal(),
    val slice: BigDecimal = 0.0.toBigDecimal(),
    val zoom: BigDecimal = 0.0.toBigDecimal(),
) {
    companion object {
        val icon = Icons.Default.Eye
    }
}