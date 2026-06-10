package ai.inspire.features.calculator

import ai.inspire.shared.baseLlmModel
import ai.inspire.shared.promptExecutor
import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.GraphAIAgent
import ai.koog.agents.core.tools.ToolRegistry

fun createCalculatorAgent(installFeatures: GraphAIAgent.FeatureContext.() -> Unit = {}) =
    AIAgent(
        promptExecutor = promptExecutor,
        systemPrompt = SYSTEM_PROMPT.trimIndent(),
        llmModel = baseLlmModel,
        temperature = 0.6,
        toolRegistry = ToolRegistry {
            tools(calculatorTools)
        },
        installFeatures = installFeatures,
    )
