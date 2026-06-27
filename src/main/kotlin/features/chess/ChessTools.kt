package ai.inspire.features.chess

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet

/**
 * 中国象棋工具集 —— 供 AI Agent 通过工具调用与棋盘交互
 */
@LLMDescription("中国象棋工具集，用于查看棋盘和执行走子")
class ChessTools(
    private val game: ChessGame,
) : ToolSet {
    @Tool
    @LLMDescription("认输。当你判断局面已无法挽回、必败无疑时，调用此工具主动认输。认输后对局立即结束。")
    fun resign(): String {
        game.resign()
        return "已认输，对局结束。"
    }

    @Tool
    @LLMDescription(
        """执行一步走子。参数 move 格式为 '<起点>-<终点>'，例如 'a0-a4' 表示将 a0 位置的棋子走到 a4。
坐标说明: 列: a-i（从左到右）；行: 0-9（0 = 红方底线，9 = 黑方底线）；例: e0 = 红方帅的初始位置，e9 = 黑方将的初始位置。""",
    )
    fun makeMove(
        @LLMDescription("走子字符串，格式: <起点>-<终点>，如 b2-e2")
        move: String,
    ): String =
        try {
            game.move(move)
            buildString {
                appendLine("当前棋盘状态:")
                appendLine()
                appendLine(game.getBoard())
                appendLine()
                appendLine(game.getPieceInventory())
                appendLine()
                appendLine("下一步行棋方: ${game.currentPlayerName()}")
            }
        } catch (e: IllegalMoveException) {
            buildString {
                appendLine("走子失败: ${e.reason}")
                if (e.validMoves.isNotEmpty()) {
                    appendLine("该棋子的合法走法: ${e.validMoves.joinToString("、")}")
                } else {
                    appendLine("该棋子当前没有合法走法")
                }
                appendLine("请根据以上提示重新走子。")
            }
        } catch (e: Exception) {
            "走子失败: ${e.message}"
        }
}
