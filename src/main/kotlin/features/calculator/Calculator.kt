package ai.inspire.features.calculator

import ai.koog.agents.features.eventHandler.feature.EventHandler
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

suspend fun testCalculator() {
    // Build Agent
    val agent = createCalculatorAgent {
        install(EventHandler) {
            // 1. 对应 LangGraph 的 stream_mode="updates"
            onNodeExecutionCompleted { eventContext ->
                // 注意：具体能点出哪些字段（如 nodeName, output），取决于 NodeExecutionCompletedContext 的内部定义
                logger.debug { "====== [Node Update] ======" }
                logger.debug { "节点执行成功完成！" }
                logger.debug { "事件上下文: $eventContext" }
                logger.debug { "===========================" }
            }

            onNodeExecutionFailed { eventContext ->
                logger.error { "[Node Error] 节点执行失败" }
            }

            // 2. 对应 LangGraph 的 stream_mode="messages" (Token 流)
//            onLLMStreamingFrameReceived { eventContext ->
//                // 这里可以拿到大模型实时吐出来的字或者 ToolCall
//                // 依据你源码中的注释示例：
//                // when (val frame = eventContext.streamFrame) { ... }
//                println("[Token] 收到流式帧: ${eventContext.streamFrame}")
//            }
        }
    }

    // Execute
    val result = agent.run("请计算123×456+800÷2。")
    println("Result: $result")
}
