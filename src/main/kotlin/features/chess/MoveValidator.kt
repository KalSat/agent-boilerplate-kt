package ai.inspire.features.chess

import kotlin.math.abs
import kotlin.math.sign

/**
 * 中国象棋走子合法性检测器
 *
 * 检测内容包括：
 * 1. 各棋子的行走规则（帅、仕、相、傌、俥、砲、兵）
 * 2. 蹩马脚、蹩象眼
 * 3. 九宫格限制（帅/将、仕/士）
 * 4. 过河限制（相/象、兵/卒）
 * 5. 走后是否导致己方被将军（含飞将/将帅对面）
 */
class MoveValidator(
    private val board: ChessBoard,
) {
    // --- 四个正交方向 ---
    private val orthogonalDirs = listOf(0 to 1, 0 to -1, 1 to 0, -1 to 0)

    // --- 马的八个跳跃偏移 ---
    private val horseMoves = listOf(
        2 to 1,
        2 to -1,
        -2 to 1,
        -2 to -1,
        1 to 2,
        1 to -2,
        -1 to 2,
        -1 to -2,
    )

    // --- 公开 API ---

    /**
     * 校验一步走子是否合法
     * @return null 表示合法；非 null 表示不合法的原因描述
     */
    fun validate(
        from: Position,
        to: Position,
        player: Player,
    ): String? {
        val piece = board.getPiece(from)

        // 基础检查
        if (piece.isNone()) return "起点 $from 没有棋子"
        if (piece.player != player) return "该棋子不属于当前行棋方"
        if (from == to) return "起点和终点不能相同"

        val target = board.getPiece(to)
        if (!target.isNone() && target.player == player) return "终点 $to 有己方棋子，不能吃自己的子"

        // 棋子行走规则
        val ruleError = checkPieceRule(piece.pieceType, from, to, player)
        if (ruleError != null) return ruleError

        // 走后不能让己方帅/将被将军（含飞将）
        if (wouldLeaveKingInCheck(from, to, player)) {
            return "此步走后己方帅/将会被将军（或导致帅将面对面）"
        }

        return null
    }

    /**
     * 生成指定位置棋子的所有合法走法（记法格式）
     */
    fun generateValidMoves(
        from: Position,
        player: Player,
    ): List<String> {
        val piece = board.getPiece(from)
        if (piece.isNone() || piece.player != player) return emptyList()

        return candidateDestinations(piece.pieceType, from, player)
            .filter { to ->
                val target = board.getPiece(to)
                if (!target.isNone() && target.player == player) return@filter false
                if (checkPieceRule(piece.pieceType, from, to, player) != null) return@filter false
                !wouldLeaveKingInCheck(from, to, player)
            }.map { to -> "$from-$to" }
    }

    // --- 各棋子行走规则校验（返回 null 表示合规） ---

    private fun checkPieceRule(
        type: PieceType,
        from: Position,
        to: Position,
        player: Player,
    ): String? =
        when (type) {
            PieceType.King -> checkKing(from, to, player)
            PieceType.Advisor -> checkAdvisor(from, to, player)
            PieceType.Elephant -> checkElephant(from, to, player)
            PieceType.Horse -> checkHorse(from, to)
            PieceType.Rook -> checkRook(from, to)
            PieceType.Cannon -> checkCannon(from, to)
            PieceType.Pawn -> checkPawn(from, to, player)
            PieceType.None -> "无效棋子类型"
        }

    // --- 帅/将 ---

    private fun checkKing(
        from: Position,
        to: Position,
        player: Player,
    ): String? {
        val dr = to.row - from.row
        val dc = to.col - from.col
        if (abs(dr) + abs(dc) != 1) return "帅/将每步只能走一格（横或竖）"
        if (!inPalace(to, player)) return "帅/将不能离开九宫格（${palaceDesc(player)}）"
        return null
    }

    // --- 仕/士 ---

    private fun checkAdvisor(
        from: Position,
        to: Position,
        player: Player,
    ): String? {
        val dr = to.row - from.row
        val dc = to.col - from.col
        if (abs(dr) != 1 || abs(dc) != 1) return "仕/士只能沿对角线走一格"
        if (!inPalace(to, player)) return "仕/士不能离开九宫格（${palaceDesc(player)}）"
        return null
    }

    // --- 相/象 ---

    private fun checkElephant(
        from: Position,
        to: Position,
        player: Player,
    ): String? {
        val dr = to.row - from.row
        val dc = to.col - from.col
        if (abs(dr) != 2 || abs(dc) != 2) return "相/象走\"田\"字（对角两格）"

        // 不能过河
        if (player == Player.Red && to.row > 4) return "相不能过河（红方相只能在行 0-4 活动）"
        if (player == Player.Black && to.row < 5) return "象不能过河（黑方象只能在行 5-9 活动）"

        // 蹩象眼
        val eyePos = Position(from.row + dr / 2, from.col + dc / 2)
        if (!board.getPiece(eyePos).isNone()) {
            return "相/象被蹩眼，$eyePos 位置有棋子（${board.getPiece(eyePos).toDisplayChar()}）阻挡"
        }

        return null
    }

    // --- 傌/马 ---

    private fun checkHorse(
        from: Position,
        to: Position,
    ): String? {
        val dr = to.row - from.row
        val dc = to.col - from.col

        val isLShape = (abs(dr) == 2 && abs(dc) == 1) || (abs(dr) == 1 && abs(dc) == 2)
        if (!isLShape) return "马走\"日\"字形（先直一格再斜一格）"

        // 蹩马脚：先直走的那一格不能有子
        val legPos = if (abs(dr) == 2) {
            Position(from.row + dr.sign, from.col)
        } else {
            Position(from.row, from.col + dc.sign)
        }
        if (!board.getPiece(legPos).isNone()) {
            return "马被蹩脚，$legPos 位置有棋子（${board.getPiece(legPos).toDisplayChar()}）阻挡"
        }

        return null
    }

    // --- 俥/车 ---

    private fun checkRook(
        from: Position,
        to: Position,
    ): String? {
        if (from.row != to.row && from.col != to.col) return "车只能直线行走（横或竖）"

        val between = countPiecesBetween(from, to)
        if (between > 0) return "车不能跨越棋子，路径上有 $between 个棋子阻挡"

        return null
    }

    // --- 砲/炮 ---

    private fun checkCannon(
        from: Position,
        to: Position,
    ): String? {
        if (from.row != to.row && from.col != to.col) return "炮只能直线移动（横或竖）"

        val between = countPiecesBetween(from, to)
        val target = board.getPiece(to)

        if (target.isNone()) {
            // 不吃子时，路径上不能有棋子
            if (between > 0) return "炮不吃子时不能跨越棋子，路径上有 $between 个棋子"
        } else {
            // 吃子时，必须恰好隔一个棋子（炮架）
            if (between != 1) return "炮吃子必须隔恰好一个棋子（炮架），当前路径上有 $between 个棋子"
        }

        return null
    }

    // --- 兵/卒 ---

    private fun checkPawn(
        from: Position,
        to: Position,
        player: Player,
    ): String? {
        val dr = to.row - from.row
        val dc = to.col - from.col

        // 每步只能走一格
        if (abs(dr) + abs(dc) != 1) return "兵/卒每步只能走一格"

        // 不能后退
        if (player == Player.Red && dr < 0) return "兵不能后退"
        if (player == Player.Black && dr > 0) return "卒不能后退"

        // 未过河只能前进，不能左右
        val crossedRiver = if (player == Player.Red) from.row >= 5 else from.row <= 4
        if (!crossedRiver && dc != 0) {
            return if (player == Player.Red) "兵未过河，只能向前走（不能左右移动）" else "卒未过河，只能向前走（不能左右移动）"
        }

        return null
    }

    // --- 将军检测 ---

    /**
     * 模拟走一步后，检测己方帅/将是否被将军
     */
    private fun wouldLeaveKingInCheck(
        from: Position,
        to: Position,
        player: Player,
    ): Boolean {
        val movingPiece = board.getPiece(from)
        val capturedPiece = board.getPiece(to)

        // 临时执行走子
        board.setPiece(to, movingPiece)
        board.setPiece(from, Piece.None)

        val inCheck = isKingInCheck(player)

        // 撤销走子
        board.setPiece(from, movingPiece)
        board.setPiece(to, capturedPiece)

        return inCheck
    }

    /**
     * 检测指定玩家的帅/将是否正在被将军
     * 涵盖：车、炮、马、兵/卒的攻击，以及飞将（帅将对面）
     */
    private fun isKingInCheck(player: Player): Boolean {
        val kingPos = board.findKing(player)
        val opponent = player.opponent()

        // 1. 检查车和飞将（沿直线找到的第一个棋子）
        for ((dr, dc) in orthogonalDirs) {
            var r = kingPos.row + dr
            var c = kingPos.col + dc
            while (r in 0..9 && c in 0..8) {
                val piece = board.getPiece(Position(r, c))
                if (!piece.isNone()) {
                    if (piece.player == opponent) {
                        if (piece.pieceType == PieceType.Rook) return true
                        if (piece.pieceType == PieceType.King) return true // 飞将
                    }
                    break
                }
                r += dr
                c += dc
            }
        }

        // 2. 检查炮（沿直线隔一个子攻击）
        for ((dr, dc) in orthogonalDirs) {
            var r = kingPos.row + dr
            var c = kingPos.col + dc
            var foundScreen = false
            while (r in 0..9 && c in 0..8) {
                val piece = board.getPiece(Position(r, c))
                if (!foundScreen) {
                    if (!piece.isNone()) foundScreen = true
                } else {
                    if (!piece.isNone()) {
                        if (piece.player == opponent &&
                            piece.pieceType == PieceType.Cannon
                        ) {
                            return true
                        }
                        break
                    }
                }
                r += dr
                c += dc
            }
        }

        // 3. 检查马（从马的位置攻击帅/将）
        for ((dr, dc) in horseMoves) {
            val r = kingPos.row + dr
            val c = kingPos.col + dc
            if (r !in 0..9 || c !in 0..8) continue
            val piece = board.getPiece(Position(r, c))
            if (piece.player == opponent && piece.pieceType == PieceType.Horse) {
                // 检查马从 (r,c) 跳到 kingPos 是否被蹩脚
                val drH = kingPos.row - r
                val dcH = kingPos.col - c
                val legPos = if (abs(drH) == 2) {
                    Position(r + drH.sign, c)
                } else {
                    Position(r, c + dcH.sign)
                }
                if (board.getPiece(legPos).isNone()) return true
            }
        }

        // 4. 检查兵/卒
        if (opponent == Player.Red) {
            // 红兵向上攻击：在帅下方一格
            if (kingPos.row - 1 >= 0) {
                val p = board.getPiece(Position(kingPos.row - 1, kingPos.col))
                if (p.player == Player.Red && p.pieceType == PieceType.Pawn) return true
            }
            // 红兵过河后可左右攻击：在帅同一行的左右一格
            for (dc in listOf(-1, 1)) {
                val c = kingPos.col + dc
                if (c !in 0..8) continue
                val pos = Position(kingPos.row, c)
                val p = board.getPiece(pos)
                // 红兵在行 5+ 说明已过河，可以左右
                if (p.player == Player.Red && p.pieceType == PieceType.Pawn &&
                    pos.row >= 5
                ) {
                    return true
                }
            }
        } else {
            // 黑卒向下攻击：在帅上方一格
            if (kingPos.row + 1 <= 9) {
                val p = board.getPiece(Position(kingPos.row + 1, kingPos.col))
                if (p.player == Player.Black && p.pieceType == PieceType.Pawn) return true
            }
            // 黑卒过河后可左右攻击
            for (dc in listOf(-1, 1)) {
                val c = kingPos.col + dc
                if (c !in 0..8) continue
                val pos = Position(kingPos.row, c)
                val p = board.getPiece(pos)
                if (p.player == Player.Black && p.pieceType == PieceType.Pawn &&
                    pos.row <= 4
                ) {
                    return true
                }
            }
        }

        return false
    }

    // --- 候选目标位置生成（供 generateValidMoves 使用） ---

    private fun candidateDestinations(
        type: PieceType,
        from: Position,
        player: Player,
    ): List<Position> =
        when (type) {
            PieceType.King -> {
                orthogonalDirs
                    .mapNotNull { (dr, dc) -> posOrNull(from.row + dr, from.col + dc) }
            }

            PieceType.Advisor -> {
                listOf(1 to 1, 1 to -1, -1 to 1, -1 to -1)
                    .mapNotNull { (dr, dc) -> posOrNull(from.row + dr, from.col + dc) }
            }

            PieceType.Elephant -> {
                listOf(2 to 2, 2 to -2, -2 to 2, -2 to -2)
                    .mapNotNull { (dr, dc) -> posOrNull(from.row + dr, from.col + dc) }
            }

            PieceType.Horse -> {
                horseMoves
                    .mapNotNull { (dr, dc) -> posOrNull(from.row + dr, from.col + dc) }
            }

            PieceType.Rook -> {
                straightLineCandidates(from)
            }

            PieceType.Cannon -> {
                straightLineCandidates(from)
            }

            PieceType.Pawn -> {
                pawnCandidates(from, player)
            }

            PieceType.None -> {
                emptyList()
            }
        }

    /** 生成直线（横/竖）方向上所有可能的目标位置（车、炮共用） */
    private fun straightLineCandidates(from: Position): List<Position> {
        val result = mutableListOf<Position>()
        for ((dr, dc) in orthogonalDirs) {
            var r = from.row + dr
            var c = from.col + dc
            while (r in 0..9 && c in 0..8) {
                result.add(Position(r, c))
                // 遇到棋子后还需包含该位置（可能是吃子），但再往后就不用了
                if (!board.getPiece(Position(r, c)).isNone()) {
                    // 对炮来说还要继续找"炮架"后面的子，所以多走一段
                    r += dr
                    c += dc
                    while (r in 0..9 && c in 0..8) {
                        result.add(Position(r, c))
                        if (!board.getPiece(Position(r, c)).isNone()) break
                        r += dr
                        c += dc
                    }
                    break
                }
                r += dr
                c += dc
            }
        }
        return result
    }

    /** 生成兵/卒的候选位置 */
    private fun pawnCandidates(
        from: Position,
        player: Player,
    ): List<Position> {
        val result = mutableListOf<Position>()
        val forward = if (player == Player.Red) 1 else -1
        posOrNull(from.row + forward, from.col)?.let { result.add(it) }
        // 过河后可左右
        posOrNull(from.row, from.col - 1)?.let { result.add(it) }
        posOrNull(from.row, from.col + 1)?.let { result.add(it) }
        return result
    }

    // --- 辅助方法 ---

    /** 安全创建坐标，越界返回 null */
    private fun posOrNull(
        row: Int,
        col: Int,
    ): Position? = if (row in 0..9 && col in 0..8) Position(row, col) else null

    /** 判断坐标是否在指定玩家的九宫格内 */
    private fun inPalace(
        pos: Position,
        player: Player,
    ): Boolean {
        val rowRange = if (player == Player.Red) 0..2 else 7..9
        return pos.row in rowRange && pos.col in 3..5
    }

    /** 九宫格范围描述 */
    private fun palaceDesc(player: Player): String =
        if (player == Player.Red) "行 0-2，列 d-f" else "行 7-9，列 d-f"

    /** 计算两点之间直线路径上的棋子数量（不含起终点） */
    private fun countPiecesBetween(
        from: Position,
        to: Position,
    ): Int {
        var count = 0
        if (from.row == to.row) {
            val minCol = minOf(from.col, to.col) + 1
            val maxCol = maxOf(from.col, to.col)
            for (c in minCol until maxCol) {
                if (!board.getPiece(Position(from.row, c)).isNone()) count++
            }
        } else {
            val minRow = minOf(from.row, to.row) + 1
            val maxRow = maxOf(from.row, to.row)
            for (r in minRow until maxRow) {
                if (!board.getPiece(Position(r, from.col)).isNone()) count++
            }
        }
        return count
    }
}
