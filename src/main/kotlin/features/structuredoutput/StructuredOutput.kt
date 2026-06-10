package ai.inspire.features.structuredoutput

import ai.inspire.shared.promptExecutor
import ai.inspire.shared.smallFastLlmModel
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAIChatParams
import ai.koog.prompt.executor.clients.openai.base.models.ReasoningEffort
import ai.koog.prompt.executor.model.StructureFixingParser
import ai.koog.prompt.executor.model.executeStructured
import ai.koog.prompt.params.additionalPropertiesOf

suspend fun testStructuredOutput() {
    val structuredResponse = promptExecutor.executeStructured<Movie>(
        // Define the prompt (both system and user messages)
        prompt = prompt(
            id = "structured-data",
            params = OpenAIChatParams(
                temperature = 0.6,
                reasoningEffort = ReasoningEffort.NONE,
                additionalProperties = additionalPropertiesOf(
                    "top_p" to 0.95,
                ),
            ),
        ) {
            system("You are a movie recommendation assistant.")
            user("请为我推荐一部电影")
        },
        // Define the main model that will execute the request
        model = smallFastLlmModel,
        // Optional: provide examples to help the model understand the format
        examples = listOf(
            Movie(
                title = "星际穿越",
                year = 2010,
                director = "克里斯托弗·诺兰",
                rating = 9.4f,
            ),
            Movie(
                title = "霸王别姬",
                year = 1993,
                director = "陈凯歌",
                rating = 9.6f,
            ),
        ),
        // Optional: provide a fixing parser for error correction
        fixingParser = StructureFixingParser(
            model = smallFastLlmModel,
            retries = 3,
        ),
    )
    if (structuredResponse.isSuccess) {
        val movie = structuredResponse.getOrThrow()
        println("$movie")
    } else {
        println("获取电影推荐失败: ${structuredResponse.exceptionOrNull()?.message}")
    }
}
