package dev.luna5ama.strepitus.params

import androidx.compose.runtime.*
import dev.luna5ama.strepitus.DecimalInput
import dev.luna5ama.strepitus.IntegerInput
import dev.luna5ama.strepitus.ToggleSwitch
import dev.luna5ama.strepitus.camelCaseToWords
import io.github.composefluent.component.*
import kotlinx.serialization.Transient
import java.math.BigDecimal
import kotlin.reflect.KAnnotatedElement
import kotlin.reflect.KClass
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties

@Composable
inline fun <reified T : Any> ParameterEditor(
    parameters: T,
    noinline onChange: (T) -> Unit
) = ParameterEditor(
    clazz = T::class,
    parameters = parameters,
    onChange = onChange
)

@Suppress("UNCHECKED_CAST")
@Composable
fun <T : Any> ParameterEditor(
    clazz: KClass<T>,
    parameters: T,
    onChange: (T) -> Unit
) {
    val copyFunc = clazz.memberFunctions.first { member -> member.name == "copy" }
    val copyFunParameterOrder = copyFunc.parameters.drop(1).withIndex().associate { it.value.name!! to it.index }
    val properties = clazz.memberProperties
        .filter { it.annotations.none { ann -> ann is Transient } }
        .sortedBy { copyFunParameterOrder[it.name] ?: Int.MAX_VALUE }

    properties.forEachIndexed { index, it ->
        val propName = it.displayName ?: camelCaseToWords(it.name)
        val propValue = it.get(parameters)!!
        val newParameterFunc = { newValue: Any ->
            val newParameters = copyFunc.callBy(
                mapOf(
                    copyFunc.parameters[0] to parameters,
                    copyFunc.parameters[1 + index] to newValue
                )
            ) as T
            onChange(newParameters)
        }
        val propType = it.returnType.classifier!! as KClass<Any>
        CardExpanderItem(heading = { Text(propName) }) {
            ParameterField(propType, propValue, newParameterFunc)
        }
    }
}

@Suppress("UNCHECKED_CAST")
@Composable
private fun ParameterField(
    propType: KClass<Any>,
    propValue: Any,
    newParameterFunc: (Any) -> Unit
) {
    when (propType) {
        Int::class -> {
            IntegerInput(value = propValue as Int, onValueChange = newParameterFunc)
        }

        BigDecimal::class -> {
            DecimalInput(value = propValue as BigDecimal, onValueChange = newParameterFunc)
        }

        Boolean::class -> {
            ToggleSwitch(
                checked = propValue as Boolean,
                onCheckStateChange = newParameterFunc
            )
        }

        else -> when {
            propType.isData -> {
                ParameterEditor(
                    clazz = propType,
                    parameters = propValue,
                    onChange = newParameterFunc
                )
            }

            Enum::class.isSuperclassOf(propType) -> {
                var enumDropdownExpanded by remember { mutableStateOf(false) }
                DropDownButton(
                    onClick = { enumDropdownExpanded = true },
                ) {
                    Text((propValue as Enum<*>).name)
                    DropdownMenu(
                        expanded = enumDropdownExpanded,
                        onDismissRequest = { enumDropdownExpanded = false },
                    ) {
                        val enumClass = propType.java as Class<out Enum<*>>
                        enumClass.enumConstants.forEach { enumConst ->
                            DropdownMenuItem(
                                onClick = {
                                    newParameterFunc(enumConst)
                                    enumDropdownExpanded = false
                                },
                            ) {
                                Text(enumConst.name)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class DisplayName(val name: String)

val KAnnotatedElement.displayName: String?
    get() = this.annotations.filterIsInstance<DisplayName>().firstOrNull()?.name
