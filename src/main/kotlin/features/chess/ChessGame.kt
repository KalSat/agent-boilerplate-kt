package ai.inspire.features.chess

/**
 * 非法走子异常 —— 包含不合法的原因和该棋子的所有合法走法
 */
class IllegalMoveException(
    val reason: String,
    val validMoves: List<String>,
) : Exception(
        buildString {
            append("非法走子: $reason")
            if (validMoves.isNotEmpty()) {
                append("\n该棋子的合法走法: ${validMoves.joinToString("、")}")
            } else {
                append("\n该棋子当前没有合法走法")
            }
        },
    )

/**
 * 中国象棋游戏逻辑
 *
 * 本类维护棋盘状态和当前行棋方，并通过 [MoveValidator] 进行合法性校验。
 */
class ChessGame {
    private val board: ChessBoard = ChessBoard()
    private val validator: MoveValidator = MoveValidator(board)
    private var currentPlayer: Player = Player.Red

    /**
     * 执行一步走子（含合法性校验）
     *
     * @param move 走子字符串，格式为 "<起点>-<终点>"，如 "a0-a4"
     * @throws IllegalMoveException 走子不合法时抛出，包含原因和合法走法列表
     */
    fun move(move: String) {
        val parts = move.lowercase().split("-")
        require(parts.size == 2) { "走子格式错误: $move（应为 <起点>-<终点>，如 a0-a4）" }

        val (fromStr, toStr) = parts
        val from = Position(fromStr)
        val to = Position(toStr)

        // 基础棋子归属检查
        val piece = board.getPiece(from)
        if (piece.isNone()) {
            throw IllegalMoveException("起点 $fromStr 没有棋子", emptyList())
        }
        if (piece.player != currentPlayer) {
            throw IllegalMoveException(
                "当前应由${currentPlayerName()}走棋，但 $fromStr 上的棋子属于对方",
                emptyList(),
            )
        }

        // 合法性校验
        val error = validator.validate(from, to, currentPlayer)
        if (error != null) {
            throw IllegalMoveException(error, validator.generateValidMoves(from, currentPlayer))
        }

        // 执行走子
        movePiece(from, to)
        currentPlayer = currentPlayer.opponent()
    }

    /** 获取当前棋盘的文本表示 */
    fun getBoard(): String = board.toString()

    /** 获取带颜色的棋盘文本（控制台显示用） */
    fun getColoredBoard(): String = board.toColoredString()

    /** 获取双方场上棋子统计 */
    fun getPieceInventory(): String = board.pieceInventory()

    /** 获取当前行棋方名称 */
    fun currentPlayerName(): String = currentPlayer.displayName

    /** 获取当前行棋方 */
    fun currentPlayer(): Player = currentPlayer

    /** AI 是否已认输 */
    var resigned: Boolean = false
        private set

    /** AI 认输 */
    fun resign() {
        resigned = true
    }

    // --- 内部方法 ---

    private fun movePiece(
        from: Position,
        to: Position,
    ) {
        board.setPiece(to, board.getPiece(from))
        board.setPiece(from, Piece.None)
    }
}
