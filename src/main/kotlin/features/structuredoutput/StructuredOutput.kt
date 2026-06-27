package ai.inspire.features.structuredoutput

import ai.inspire.shared.promptExecutor
import ai.inspire.shared.smallFastLlmModel
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAIChatParams
import ai.koog.prompt.executor.clients.openai.base.models.ReasoningEffort
import ai.koog.prompt.executor.model.StructureFixingParser
import ai.koog.prompt.executor.model.executeStructured
import ai.koog.prompt.params.additionalPropertiesOf
import ai.koog.prompt.structure.StructuredRequest
import ai.koog.prompt.structure.StructuredRequestConfig
import ai.koog.prompt.structure.json.JsonStructure
import ai.koog.prompt.structure.json.generator.StandardJsonSchemaGenerator
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

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
        config = StructuredRequestConfig(
            default = StructuredRequest.Manual(
                JsonStructure.create<Movie>(
                    schemaGenerator = StandardJsonSchemaGenerator,
                    examples = listOf(
                        Movie("星际穿越", 2010, "克里斯托弗·诺兰", 9.4f),
                        Movie("霸王别姬", 1993, "陈凯歌", 9.6f),
                    ),
                ),
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
        logger.error { "获取电影推荐失败: ${structuredResponse.exceptionOrNull()?.message}" }
    }
}
