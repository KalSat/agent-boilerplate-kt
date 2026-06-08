package ai.inspire.shared

import ai.koog.prompt.executor.clients.openai.OpenAIClientSettings
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.model.PromptExecutor
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel

val baseLlmModel = LLModel(
    provider = LLMProvider.OpenAI,
    id = Config.model,
    capabilities = listOf(
        LLMCapability.Temperature,
        LLMCapability.Schema.JSON.Standard,
        LLMCapability.Tools,
        LLMCapability.Thinking,
        LLMCapability.Completion,
        LLMCapability.OpenAIEndpoint.Completions,
    ),
)

val smallFastLlmModel = LLModel(
    provider = LLMProvider.OpenAI,
    id = Config.smallFastModel,
    capabilities = listOf(
        LLMCapability.Temperature,
        LLMCapability.Schema.JSON.Standard,
        LLMCapability.Tools,
        LLMCapability.Thinking,
        LLMCapability.Completion,
        LLMCapability.OpenAIEndpoint.Completions,
    ),
)

val llmClient = OpenAILLMClient(
    apiKey = Config.apiKey,
    settings = OpenAIClientSettings(baseUrl = Config.baseUrl),
)

val promptExecutor = PromptExecutor
    .builder()
    .addClient(llmClient)
    .build()
