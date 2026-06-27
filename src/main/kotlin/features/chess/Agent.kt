package ai.inspire.features.chess

import ai.inspire.shared.baseLlmModel
import ai.inspire.shared.promptExecutor
import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.GraphAIAgent
import ai.koog.agents.core.tools.ToolRegistry

/**
 * 创建中国象棋 Agent
 *
 * 使用自定义策略（含对话历史裁剪优化）：
 * - temperature = 0.0 确保确定性、策略性走棋
 * - maxIterations = 200 支持完整对局
 * - chessStrategy 包含历史裁剪，避免 Token 累积
 *
 * @param game 共享的 ChessGame 实例（工具和外部逻辑使用同一局棋）
 * @param aiSide AI 所执的方
 * @param installFeatures 可选的 Agent 特性安装回调（如 EventHandler）
 */
fun createChessAgent(
    game: ChessGame = ChessGame(),
    aiSide: Player = Player.Black,
    installFeatures: GraphAIAgent.FeatureContext.() -> Unit = {},
) = AIAgent(
    promptExecutor = promptExecutor,
    llmModel = baseLlmModel,
    strategy = chessStrategy,
    systemPrompt = chessSystemPrompt(aiSide),
    temperature = 0.0,
    toolRegistry = ToolRegistry {
        tools(ChessTools(game))
    },
    maxIterations = 200,
    installFeatures = installFeatures,
)
