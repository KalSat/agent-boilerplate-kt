package ai.inspire.features.chess

import ai.koog.agents.core.agent.GraphAIAgent
import ai.koog.agents.features.eventHandler.feature.EventHandler
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/** 对弈模式 */
enum class GameMode {
    HumanVsHuman,
    HumanFirst,
    AIFirst,
}

/**
 * 中国象棋功能入口 —— 支持三种对弈模式
 */
suspend fun testChess() {
    println("=== 中国象棋 ===")
    println("请选择对弈模式:")
    println("  1. 人人对弈")
    println("  2. 人机对弈（你先手，执红）")
    println("  3. 人机对弈（你后手，执黑）")
    print("请输入选项 [1/2/3]: ")

    val mode = when (readlnOrNull()?.trim()) {
        "1" -> GameMode.HumanVsHuman
        "2" -> GameMode.HumanFirst
        "3" -> GameMode.AIFirst
        else -> GameMode.HumanFirst.also { println("无效选项，默认选择: 人机对弈（你先手）") }
    }

    val game = ChessGame()
    val aiSide = when (mode) {
        GameMode.AIFirst -> Player.Red
        GameMode.HumanFirst -> Player.Black
        GameMode.HumanVsHuman -> Player.None
    }
    val agent by lazy {
        createChessAgent(game, aiSide) {
            install(EventHandler) {
                onNodeExecutionCompleted { eventContext ->
                    logger.debug { "[节点完成] $eventContext" }
                }
                onNodeExecutionFailed {
                    logger.error { "[错误] 节点执行失败" }
                }
            }
        }
    }

    println()
    println(game.getColoredBoard())
    println()
    println("走子格式: <起点>-<终点>，例如 b2-e2")
    println("输入 quit 退出")
    println()

    // 对弈主循环：轮流执行 human turn 或 ai turn
    while (true) {
        val success =
            if (game.currentPlayer() == aiSide) {
                aiTurn(game, agent)
            } else {
                humanTurn(game)
            }
        if (success) {
            println()
            println(game.getColoredBoard())
            println("-----------------")
            println()
        } else {
            println("${game.currentPlayerName()}投子认输。")
            println("${game.currentPlayer().opponent().displayName}获胜！")
            println("游戏结束。")
            break
        }
    }
}

/**
 * 人类走棋回合
 * @return false 表示用户退出
 */
private fun humanTurn(game: ChessGame): Boolean {
    print("${game.currentPlayerName()}请走棋> ")
    val input = readlnOrNull()?.trim() ?: return false
    if (input.equals("quit", ignoreCase = true)) {
        return false
    }

    try {
        game.move(input)
    } catch (e: IllegalMoveException) {
        println("⚠ ${e.message}")
        println("请重新输入。")
        return humanTurn(game)
    } catch (e: Exception) {
        println("走子无效: ${e.message}")
        println("请重新输入。")
        return humanTurn(game)
    }

    return true
}

/**
 * AI 走棋回合
 * @return false 表示对局结束
 */
private suspend fun aiTurn(
    game: ChessGame,
    agent: GraphAIAgent<String, String>,
): Boolean {
    println("现在轮到${game.currentPlayerName()}走棋了，AI 思考中...")
    val message = buildString {
        appendLine("当前棋盘状态:")
        appendLine()
        appendLine(game.getBoard())
        appendLine()
        appendLine("当前双方剩余棋子:")
        appendLine(game.getPieceInventory())
        appendLine()
        appendLine("现在轮到你（${game.currentPlayerName()}）走棋了。")
    }
    val result = agent.run(message)
    if (game.resigned) {
        println("AI（${game.currentPlayerName()}）: $result")
        return false
    }
    println("AI: $result")
    return true
}
