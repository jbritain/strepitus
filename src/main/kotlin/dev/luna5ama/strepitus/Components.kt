package dev.luna5ama.strepitus

import androidx.compose.foundation.text.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.*
import io.github.composefluent.component.*
import java.math.BigDecimal

@Composable
fun ToggleSwitch(
    checked: Boolean,
    onCheckStateChange: (Boolean) -> Unit,
    textOn: String = "On",
    textOff: String = "Off",
) {
    Switcher(
        checked = checked,
        onCheckStateChange = onCheckStateChange,
        text = if (checked) textOn else textOff,
        textBefore = true
    )
}

@Composable
fun IntegerInput(
    value: Int,
    onValueChange: (Int) -> Unit,
    enabled: Boolean = true,
) {
    TextField(
        value = value.toString(),
        onValueChange = { str ->
            str.toIntOrNull()?.let {
                onValueChange(it)
            }
        },
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        keyboardActions = KeyboardActions.Default,
    )
}

@Composable
fun DecimalInput(
    value: BigDecimal,
    onValueChange: (BigDecimal) -> Unit,
    enabled: Boolean = true,
) {
    var typedValue by remember { mutableStateOf(value.toString()) }
    TextField(
        value = typedValue,
        onValueChange = { str ->
            typedValue = str
            str.toBigDecimalOrNull()?.let {
                onValueChange(it)
            }
        },
        enabled = enabled,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        keyboardActions = KeyboardActions.Default,
        modifier = Modifier.onFocusChanged { state ->
            if (!state.isFocused) {
                typedValue = value.toString()
            }
        }
    )
}