package ai.inspire.features.chess

/**
 * 中国象棋棋盘 —— 10 行 × 9 列
 *
 * 行号 0-9（0 = 红方底线，9 = 黑方底线）
 * 列号 0-8（对应 a-i）
 */
class ChessBoard {
    /** 棋盘内部存储：board[row][col]，row 0 在底部（红方） */
    private val board: List<MutableList<Piece>> = buildInitialBoard()

    fun getPiece(pos: Position): Piece = board[pos.row][pos.col]

    fun setPiece(
        pos: Position,
        piece: Piece,
    ) {
        board[pos.row][pos.col] = piece
    }

    /** 查找指定玩家的帅/将位置 */
    fun findKing(player: Player): Position {
        for (row in 0..9) {
            for (col in 0..8) {
                val piece = board[row][col]
                if (piece.pieceType == PieceType.King && piece.player == player) {
                    return Position(row, col)
                }
            }
        }
        throw IllegalStateException("找不到${if (player == Player.Red) "帅" else "将"}")
    }

    /** 获取双方场上棋子统计，按将士象马车炮卒顺序排列 */
    fun pieceInventory(): String =
        buildString {
            val redPieces = mutableMapOf<PieceType, Int>()
            val blackPieces = mutableMapOf<PieceType, Int>()
            for (row in 0..9) {
                for (col in 0..8) {
                    val piece = board[row][col]
                    if (piece.isNone()) continue
                    when (piece.player) {
                        Player.Red -> {
                            redPieces[piece.pieceType] = (redPieces[piece.pieceType] ?: 0) + 1
                        }

                        Player.Black -> {
                            blackPieces[piece.pieceType] = (blackPieces[piece.pieceType] ?: 0) + 1
                        }

                        else -> {}
                    }
                }
            }

            fun format(
                pieces: Map<PieceType, Int>,
                player: Player,
            ) = PIECE_DISPLAY_ORDER
                .filter { pieces.containsKey(it) }
                .joinToString("，") { "${Piece(it, player).toDisplayChar()}x${pieces[it]}" }

            append("红方(下方): ${format(redPieces, Player.Red)}")
            append("\n")
            append("黑方(上方): ${format(blackPieces, Player.Black)}")
        }

    override fun toString(): String =
        buildString {
            for (row in 9 downTo 0) {
                append(row)
                append(' ')
                append(board[row].joinToString("") { it.toDisplayChar().toString() })
                appendLine()
                if (row == 5) appendLine("～～～楚河汉界～～～")
            }
            append("  ａｂｃｄｅｆｇｈｉ")
        }

    /** 带 ANSI 颜色的棋盘字符串（红方棋子显示为红色），用于控制台输出 */
    fun toColoredString(): String =
        buildString {
            for (row in 9 downTo 0) {
                append(row)
                append(' ')
                for (col in 0..8) {
                    val piece = board[row][col]
                    val ch = piece.toDisplayChar().toString()
                    when (piece.player) {
                        Player.Red -> append("\u001B[31m$ch\u001B[0m")
                        else -> append(ch)
                    }
                }
                appendLine()
                if (row == 5) appendLine("～～～楚河汉界～～～")
            }
            append("  ａｂｃｄｅｆｇｈｉ")
        }

    companion object {
        /** 首行棋子排列（车马象仕帅仕象马车） */
        private val BACK_ROW = listOf(
            PieceType.Rook,
            PieceType.Horse,
            PieceType.Elephant,
            PieceType.Advisor,
            PieceType.King,
            PieceType.Advisor,
            PieceType.Elephant,
            PieceType.Horse,
            PieceType.Rook,
        )

        /** 兵/卒所在的列号（a, c, e, g, i） */
        private val PAWN_COLS = listOf(0, 2, 4, 6, 8)

        /** 砲/炮所在的列号（b, h） */
        private val CANNON_COLS = listOf(1, 7)

        /** 构建初始棋盘状态 */
        private fun buildInitialBoard(): List<MutableList<Piece>> {
            val rows = List(10) { MutableList(9) { Piece.None } }

            // 红方底线（row 0）
            for (col in 0..8) rows[0][col] = Piece(BACK_ROW[col], Player.Red)
            // 红方砲（row 2）
            for (col in CANNON_COLS) rows[2][col] = Piece(PieceType.Cannon, Player.Red)
            // 红方兵（row 3）
            for (col in PAWN_COLS) rows[3][col] = Piece(PieceType.Pawn, Player.Red)

            // 黑方底线（row 9）
            for (col in 0..8) rows[9][col] = Piece(BACK_ROW[col], Player.Black)
            // 黑方炮（row 7）
            for (col in CANNON_COLS) rows[7][col] = Piece(PieceType.Cannon, Player.Black)
            // 黑方卒（row 6）
            for (col in PAWN_COLS) rows[6][col] = Piece(PieceType.Pawn, Player.Black)

            return rows
        }
    }
}

/** 棋子库存展示顺序：将/帅、士/仕、象/相、马/傌、车/俥、炮/砲、卒/兵 */
private val PIECE_DISPLAY_ORDER = listOf(
    PieceType.King,
    PieceType.Advisor,
    PieceType.Elephant,
    PieceType.Horse,
    PieceType.Rook,
    PieceType.Cannon,
    PieceType.Pawn,
)
