package ai.inspire

import ai.inspire.shared.baseLlmModel
import ai.inspire.shared.promptExecutor
import ai.inspire.shared.smallFastLlmModel
import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.params.LLMParams

suspend fun main() {
    val agent = AIAgent(
        promptExecutor = promptExecutor,
        llmModel = baseLlmModel,
        temperature = 0.3,
    )

//    val result = agent.run("hello! who are you?")
    val result = promptExecutor.executeStreaming(
        prompt =
            prompt(
                id = "hello-koog",
                params = LLMParams(temperature = 0.3),
            ) {
                user("hello! who are you?")
            },
        model = smallFastLlmModel,
    )
    println(result)
}
