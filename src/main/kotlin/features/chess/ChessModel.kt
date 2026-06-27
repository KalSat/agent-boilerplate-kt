package ai.inspire.features.chess

// 中国象棋领域模型 —— 玩家、棋子类型、棋子、棋盘坐标

/** 玩家 */
enum class Player(
    val displayName: String,
) {
    Red("红方"),
    Black("黑方"),
    None(""),
    ;

    fun opponent(): Player =
        when (this) {
            Red -> Black
            Black -> Red
            None -> throw IllegalArgumentException("None 没有对手")
        }
}

// --- 棋子类型 ---

/**
 * 棋子类型及其英文缩写（走子记录中使用）
 * - 帅/将 (King)
 * - 仕/士 (Advisor)
 * - 相/象 (Elephant)
 * - 傌/马 (Horse)
 * - 俥/车 (Rook)
 * - 砲/炮 (Cannon)
 * - 兵/卒 (Pawn)
 */
enum class PieceType {
    King,
    Advisor,
    Elephant,
    Horse,
    Rook,
    Cannon,
    Pawn,
    None,
}

// --- 棋子 ---

data class Piece(
    val pieceType: PieceType,
    val player: Player,
) {
    init {
        require((pieceType == PieceType.None) == (player == Player.None)) {
            "无效棋子: $pieceType $player"
        }
    }

    /** 返回棋子的中文显示字符 */
    fun toDisplayChar(): Char =
        when (player) {
            Player.Red -> RED_CHARS.getValue(pieceType)
            Player.Black -> BLACK_CHARS.getValue(pieceType)
            Player.None -> '．'
        }

    fun isNone(): Boolean = pieceType == PieceType.None

    companion object {
        val None = Piece(PieceType.None, Player.None)

        // 红方棋子中文字符映射
        private val RED_CHARS = mapOf(
            PieceType.King to '帅',
            PieceType.Advisor to '仕',
            PieceType.Elephant to '相',
            PieceType.Horse to '傌',
            PieceType.Rook to '俥',
            PieceType.Cannon to '砲',
            PieceType.Pawn to '兵',
            PieceType.None to '．',
        )

        // 黑方棋子中文字符映射
        private val BLACK_CHARS = mapOf(
            PieceType.King to '将',
            PieceType.Advisor to '士',
            PieceType.Elephant to '象',
            PieceType.Horse to '马',
            PieceType.Rook to '车',
            PieceType.Cannon to '炮',
            PieceType.Pawn to '卒',
            PieceType.None to '．',
        )
    }
}

// --- 坐标 ---

/**
 * 棋盘坐标
 * - row: 0-9（0 = 红方底线，9 = 黑方底线）
 * - col: 0-8（对应 a-i）
 */
data class Position(
    val row: Int,
    val col: Int,
) {
    init {
        require(row in 0..9 && col in 0..8) {
            "无效坐标: row=$row, col=$col（行需在 0-9，列需在 0-8）"
        }
    }

    /** 从棋谱记法解析，如 "e0"、"a9" */
    constructor(notation: String) : this(
        parseRow(notation),
        parseCol(notation),
    )

    /** 转为棋谱记法，如 "e0" */
    override fun toString(): String = "${'a' + col}$row"

    companion object {
        private fun parseRow(notation: String): Int {
            require(notation.length in 2..3) { "无效坐标记法: $notation" }
            return notation.substring(1).toIntOrNull()
                ?: throw IllegalArgumentException("无效坐标记法: $notation")
        }

        private fun parseCol(notation: String): Int {
            val c = notation[0]
            require(c in 'a'..'i') { "无效列标: $c（需在 a-i 范围内）" }
            return c - 'a'
        }
    }
}
