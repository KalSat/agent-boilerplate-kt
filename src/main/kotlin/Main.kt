package ai.inspire

import ai.inspire.features.chatbot.testChatbot

fun testStructuredOutput() = println("电影推荐 (结构化输出) - 待实现")

fun testCalculator() = println("计算器 (工具调用) - 待实现")

val options: List<Pair<String, () -> Unit>> = listOf(
    "聊天机器人 (流式输出)" to ::testChatbot,
    "电影推荐 (结构化输出)" to ::testStructuredOutput,
    "计算器 (工具调用)" to ::testCalculator,
)

fun main() {
    println("Select an option by number:")
    options.forEachIndexed { idx, option ->
        println("${idx + 1}. ${option.first}")
    }

    val choice = readlnOrNull()?.trim() ?: run {
        println("No input received. Exiting.")
        return
    }

    val idx = choice.toIntOrNull() ?: run {
        println("Invalid selection.")
        return
    }

    if (idx < 1 || idx > options.size) {
        println("Invalid selection.")
        return
    }

    options[idx - 1].second()
}
