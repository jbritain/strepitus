package dev.luna5ama.strepitus.params

enum class DarkModeOption {
    Auto,
    Dark,
    Light,
}

data class SystemParameters(
    val darkMode: DarkModeOption = DarkModeOption.Auto,
)