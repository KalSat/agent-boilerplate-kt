package ai.inspire.features.calculator

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet

val calculatorTools = Tools()

@LLMDescription("Tools for calculating")
class Tools : ToolSet {
    @Tool
    @LLMDescription("Adds `a` and `b`")
    fun add(
        @LLMDescription("First int")
        a: Int,
        @LLMDescription("Second int")
        b: Int,
    ): Int = a + b

    @Tool
    @LLMDescription("Subtracts `b` from `a`")
    fun subtract(
        @LLMDescription("First int")
        a: Int,
        @LLMDescription("Second int")
        b: Int,
    ): Int = a - b

    @Tool
    @LLMDescription("Multiplies `a` and `b`")
    fun multiply(
        @LLMDescription("First int")
        a: Int,
        @LLMDescription("Second int")
        b: Int,
    ): Int = a * b

    @Tool
    @LLMDescription("Divides `a` and `b`")
    fun divide(
        @LLMDescription("First int")
        a: Int,
        @LLMDescription("Second int")
        b: Int,
    ): Float = a / b.toFloat()
}
