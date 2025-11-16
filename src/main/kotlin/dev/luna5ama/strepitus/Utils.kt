package dev.luna5ama.strepitus

fun camelCaseToWords(input: String): String {
    return buildString {
        input.forEachIndexed { index, c ->
            if (c.isUpperCase() && index != 0) {
                append(' ')
            }
            if (index == 0) {
                append(c.uppercase())
            } else {
                append(c.lowercase())
            }
        }
    }
}

fun camelCaseToTitle(input: String): String {
    return buildString {
        input.forEachIndexed { index, c ->
            if (c.isUpperCase() && index != 0) {
                append(' ')
            }
            if (index == 0) {
                append(c.uppercase())
            } else {
                append(c)
            }
        }
    }
}