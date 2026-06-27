@file:Suppress("UnstableApiUsage")

package ai.inspire.features.chess

import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.HistoryCompressionStrategy
import ai.koog.agents.core.dsl.extension.ReceivedToolResults
import ai.koog.agents.core.dsl.extension.nodeExecuteTools
import ai.koog.agents.core.dsl.extension.nodeLLMCompressHistory
import ai.koog.agents.core.dsl.extension.nodeLLMRequest
import ai.koog.agents.core.dsl.extension.nodeLLMSendToolResults
import ai.koog.agents.core.dsl.extension.onTextMessage
import ai.koog.agents.core.dsl.extension.onToolCalls

/**
 * 中国象棋 Agent 策略
 *
 * 基于 Koog 的图结构定义执行流程：
 * - Start → LLM 请求 → 工具执行 → 历史裁剪 → 发送结果 → ...
 * - LLM 返回文本消息时结束（表示对局评价或认输）
 * - LLM 返回工具调用时继续执行（走子）
 *
 * 核心优化：nodeTrimHistory 在每次工具执行后裁剪对话历史，
 * 只保留系统提示词和最后一条消息（当前棋盘状态），从而：
 * - 减少 Token 消耗，避免对话历史指数增长
 * - 保持足够的上下文（当前棋盘状态）
 * - 支持长局对弈而不触及 Token 上限
 */
val chessStrategy = strategy<String, String>("chess_strategy") {
    val nodeCallLLM by nodeLLMRequest("sendInput")
    val nodeExecuteTool by nodeExecuteTools("executeMove")
    val nodeSendToolResult by nodeLLMSendToolResults("sendMoveResult")
    val nodeTrimHistory by nodeLLMCompressHistory<ReceivedToolResults>(
        "trimHistory",
        HistoryCompressionStrategy.FromLastNMessages(1),
    )

    edge(nodeStart forwardTo nodeCallLLM)
    edge(nodeCallLLM forwardTo nodeExecuteTool onToolCalls { true })
    edge(nodeCallLLM forwardTo nodeFinish onTextMessage { true })
    edge(nodeExecuteTool forwardTo nodeTrimHistory)
    edge(nodeTrimHistory forwardTo nodeSendToolResult)
    edge(nodeSendToolResult forwardTo nodeFinish onTextMessage { true })
    edge(nodeSendToolResult forwardTo nodeExecuteTool onToolCalls { true })
}
