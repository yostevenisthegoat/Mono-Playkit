package com.example.feature.puzzles

import kotlin.random.Random

/**
 * Pyraminx State & Optimal Bidirectional BFS Solver.
 * Faces: 0=F (Green), 1=R (Red), 2=L (Blue), 3=D (Yellow).
 * Each face has 9 triangular stickers (row 0 = top tip [0], row 1 = [1,2,3], row 2 = [4,5,6,7,8]).
 * Specifically for each face looking straight at it:
 *       0 (top tip)
 *     1 2 3 (1=left-mid, 2=center-top, 3=right-mid)
 *   4 5 6 7 8 (4=left tip, 5=left-bot edge, 6=bot center, 7=right-bot edge, 8=right tip)
 * Wait: Pyraminx has 4 vertices (U, L, R, B). Let's represent Pyraminx cleanly by its exact piece model:
 * - 4 Tips: U, L, R, B (each has orientation 0, 1, 2)
 * - 4 Centers: U, L, R, B (each has orientation 0, 1, 2)
 * - 6 Edges: UL, UR, UB, LR, LB, RB (each has permutation 0..5 and flip 0..1)
 */
data class PyraminxState(
    val tips: IntArray = IntArray(4) { 0 },
    val centers: IntArray = IntArray(4) { 0 },
    val edgePerm: IntArray = IntArray(6) { it },
    val edgeFlip: IntArray = IntArray(6) { 0 },
) {
    fun copy(): PyraminxState = PyraminxState(
        tips.copyOf(),
        centers.copyOf(),
        edgePerm.copyOf(),
        edgeFlip.copyOf(),
    )

    fun isSolved(): Boolean =
        tips.all { it == 0 } &&
            centers.all { it == 0 } &&
            edgePerm.indices.all { edgePerm[it] == it } &&
            edgeFlip.all { it == 0 }

    fun applyMove(move: String): PyraminxState {
        val prime = move.endsWith("'")
        val delta = if (prime) 2 else 1
        when (move.first()) {
            'u' -> tips[0] = (tips[0] + delta) % 3
            'l' -> tips[1] = (tips[1] + delta) % 3
            'r' -> tips[2] = (tips[2] + delta) % 3
            'b' -> tips[3] = (tips[3] + delta) % 3
            'U' -> {
                centers[0] = (centers[0] + delta) % 3
                // Edges around U: 0(UL), 1(UR), 2(UB)
                // CW (looking down at U): UB -> UR -> UL -> UB? Let's define:
                // 0:UL, 1:UR, 2:UB, 3:LR, 4:LB, 5:RB
                cycleEdges(2, 1, 0, prime, flipA = false, flipB = false, flipC = false)
            }
            'L' -> {
                centers[1] = (centers[1] + delta) % 3
                // Edges around L: 0(UL), 3(LR), 4(LB)
                cycleEdges(0, 3, 4, prime, flipA = true, flipB = true, flipC = false)
            }
            'R' -> {
                centers[2] = (centers[2] + delta) % 3
                // Edges around R: 1(UR), 5(RB), 3(LR)
                cycleEdges(1, 5, 3, prime, flipA = true, flipB = true, flipC = false)
            }
            'B' -> {
                centers[3] = (centers[3] + delta) % 3
                // Edges around B: 2(UB), 4(LB), 5(RB)
                cycleEdges(2, 4, 5, prime, flipA = true, flipB = true, flipC = false)
            }
        }
        return this
    }

    private fun cycleEdges(
        a: Int,
        b: Int,
        c: Int,
        reverse: Boolean,
        flipA: Boolean,
        flipB: Boolean,
        flipC: Boolean,
    ) {
        if (!reverse) {
            val tp = edgePerm[c]; val tf = edgeFlip[c]
            edgePerm[c] = edgePerm[b]; edgeFlip[c] = edgeFlip[b] xor (if (flipC) 1 else 0)
            edgePerm[b] = edgePerm[a]; edgeFlip[b] = edgeFlip[a] xor (if (flipB) 1 else 0)
            edgePerm[a] = tp; edgeFlip[a] = tf xor (if (flipA) 1 else 0)
        } else {
            val tp = edgePerm[a]; val tf = edgeFlip[a]
            edgePerm[a] = edgePerm[b]; edgeFlip[a] = edgeFlip[b] xor (if (flipB) 1 else 0)
            edgePerm[b] = edgePerm[c]; edgeFlip[b] = edgeFlip[c] xor (if (flipC) 1 else 0)
            edgePerm[c] = tp; edgeFlip[c] = tf xor (if (flipA) 1 else 0)
        }
    }

    fun encodeBody(): Long {
        var code = 0L
        for (i in 0..3) code = code * 3L + centers[i]
        for (i in 0..5) code = code * 6L + edgePerm[i]
        for (i in 0..5) code = code * 2L + edgeFlip[i]
        return code
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PyraminxState) return false
        return tips.contentEquals(other.tips) &&
            centers.contentEquals(other.centers) &&
            edgePerm.contentEquals(other.edgePerm) &&
            edgeFlip.contentEquals(other.edgeFlip)
    }

    override fun hashCode(): Int {
        var result = tips.contentHashCode()
        result = 31 * result + centers.contentHashCode()
        result = 31 * result + edgePerm.contentHashCode()
        result = 31 * result + edgeFlip.contentHashCode()
        return result
    }

    companion object {
        private val bodyMoves = listOf("U", "U'", "L", "L'", "R", "R'", "B", "B'")

        fun randomScramble(): List<String> {
            val seq = mutableListOf<String>()
            var last = -1
            repeat(10) {
                var f: Int
                do { f = Random.nextInt(4) } while (f == last)
                last = f
                val base = listOf("U", "L", "R", "B")[f]
                seq.add(if (Random.nextBoolean()) base else "$base'")
            }
            listOf("u", "l", "r", "b").forEach { tip ->
                when (Random.nextInt(3)) {
                    1 -> seq.add(tip)
                    2 -> seq.add("$tip'")
                }
            }
            return seq
        }

        fun inverseMove(m: String): String =
            if (m.endsWith("'")) m.dropLast(1) else "$m'"

        fun solve(start: PyraminxState): List<String>? {
            // Check parity: edge permutation must be even, edge flip sum % 2 == 0
            var inv = 0
            for (i in 0..5) for (j in i + 1..5) if (start.edgePerm[i] > start.edgePerm[j]) inv++
            if (inv % 2 != 0) return null
            if (start.edgeFlip.sum() % 2 != 0) return null

            val tipMoves = mutableListOf<String>()
            val tipNames = arrayOf("u", "l", "r", "b")
            for (i in 0..3) {
                when (start.tips[i]) {
                    1 -> tipMoves.add("${tipNames[i]}'")
                    2 -> tipMoves.add(tipNames[i])
                }
            }

            val startBody = start.copy().apply { tips.fill(0) }
            val goalBody = PyraminxState()
            if (startBody.encodeBody() == goalBody.encodeBody()) return tipMoves

            // Bidirectional BFS up to depth 6 + 6
            val frontVisited = HashMap<Long, List<String>>(4096)
            val backVisited = HashMap<Long, List<String>>(4096)
            val frontQueue = ArrayDeque<Pair<PyraminxState, List<String>>>()
            val backQueue = ArrayDeque<Pair<PyraminxState, List<String>>>()

            frontVisited[startBody.encodeBody()] = emptyList()
            backVisited[goalBody.encodeBody()] = emptyList()
            frontQueue.add(startBody to emptyList())
            backQueue.add(goalBody to emptyList())

            for (depth in 1..6) {
                val fSize = frontQueue.size
                repeat(fSize) {
                    val (cur, path) = frontQueue.removeFirst()
                    val lastFace = path.lastOrNull()?.firstOrNull()
                    for (m in bodyMoves) {
                        if (m.first() == lastFace) continue
                        val next = cur.copy().applyMove(m)
                        val code = next.encodeBody()
                        if (code !in frontVisited) {
                            val newPath = path + m
                            frontVisited[code] = newPath
                            val backMatch = backVisited[code]
                            if (backMatch != null) {
                                return newPath + backMatch.reversed().map { inverseMove(it) } + tipMoves
                            }
                            frontQueue.add(next to newPath)
                        }
                    }
                }
                val bSize = backQueue.size
                repeat(bSize) {
                    val (cur, path) = backQueue.removeFirst()
                    val lastFace = path.lastOrNull()?.firstOrNull()
                    for (m in bodyMoves) {
                        if (m.first() == lastFace) continue
                        val next = cur.copy().applyMove(m)
                        val code = next.encodeBody()
                        if (code !in backVisited) {
                            val newPath = path + m
                            backVisited[code] = newPath
                            val frontMatch = frontVisited[code]
                            if (frontMatch != null) {
                                return frontMatch + newPath.reversed().map { inverseMove(it) } + tipMoves
                            }
                            backQueue.add(next to newPath)
                        }
                    }
                }
            }
            return null
        }
    }
}

/**
 * Skewb State & Optimal Bidirectional BFS Solver (FKT / Rubiks-style U, R, L, B moves).
 * Pieces:
 * - 6 Centers: 0=U, 1=R, 2=F, 3=D, 4=L, 5=B (permutation of 0..5)
 * - 4 Fixed-orbit corners (always stay in their tetrahedron, only twist 0..2): 0=UFR, 1=UBL, 2=DFL, 3=DBR
 * - 4 Free-orbit corners (permute & twist 0..2): 0=UFL, 1=UBR, 2=DFR, 3=DBL
 */
data class SkewbState(
    val centers: IntArray = IntArray(6) { it },
    val fixedCornerOri: IntArray = IntArray(4) { 0 },
    val freeCornerPerm: IntArray = IntArray(4) { it },
    val freeCornerOri: IntArray = IntArray(4) { 0 },
) {
    fun copy(): SkewbState = SkewbState(
        centers.copyOf(),
        fixedCornerOri.copyOf(),
        freeCornerPerm.copyOf(),
        freeCornerOri.copyOf(),
    )

    fun isSolved(): Boolean =
        centers.indices.all { centers[it] == it } &&
            fixedCornerOri.all { it == 0 } &&
            freeCornerPerm.indices.all { freeCornerPerm[it] == it } &&
            freeCornerOri.all { it == 0 }

    fun applyMove(move: String): SkewbState {
        val prime = move.endsWith("'")
        val turns = if (prime) 2 else 1
        repeat(turns) {
            when (move.first()) {
                'R' -> {
                    // Fixed corner 3(DBR) twists; free corners 1(UBR), 2(DFR), 3(DBL) cycle; centers R(1), B(5), D(3) cycle
                    fixedCornerOri[3] = (fixedCornerOri[3] + 1) % 3
                    cycle3Centers(1, 5, 3)
                    cycle3FreeCorners(1, 3, 2, intArrayOf(1, 2, 0))
                }
                'L' -> {
                    // Fixed corner 2(DFL) twists; free corners 0(UFL), 2(DFR), 3(DBL) cycle; centers L(4), F(2), D(3) cycle
                    fixedCornerOri[2] = (fixedCornerOri[2] + 1) % 3
                    cycle3Centers(4, 2, 3)
                    cycle3FreeCorners(0, 2, 3, intArrayOf(1, 2, 0))
                }
                'U' -> {
                    // Fixed corner 1(UBL) twists; free corners 0(UFL), 1(UBR), 3(DBL) cycle; centers U(0), B(5), L(4) cycle
                    fixedCornerOri[1] = (fixedCornerOri[1] + 1) % 3
                    cycle3Centers(0, 5, 4)
                    cycle3FreeCorners(0, 3, 1, intArrayOf(1, 2, 0))
                }
                'B' -> {
                    // Fixed corner 0(UFR) twists? Wait: B is DBL/UBR/DBR -> let's use standard F/B/U/R/L:
                    // Corner UFR is fixed 0 -> let's assign 'F' to UFR-centered move, or 'B' to B-axis (centers B(5), U(0), R(1) cycle, fixed 1(UBL)? Wait: UBR is free 1; UBL is fixed 1, DBR is fixed 3).
                    // Around UFR (fixed 0): centers U(0), R(1), F(2); free corners 0(UFL), 1(UBR), 2(DFR).
                    fixedCornerOri[0] = (fixedCornerOri[0] + 1) % 3
                    cycle3Centers(0, 1, 2)
                    cycle3FreeCorners(0, 1, 2, intArrayOf(1, 2, 0))
                }
            }
        }
        return this
    }

    private fun cycle3Centers(a: Int, b: Int, c: Int) {
        val t = centers[c]
        centers[c] = centers[b]
        centers[b] = centers[a]
        centers[a] = t
    }

    private fun cycle3FreeCorners(a: Int, b: Int, c: Int, twistDelta: IntArray) {
        val tp = freeCornerPerm[c]
        val to = freeCornerOri[c]
        freeCornerPerm[c] = freeCornerPerm[b]
        freeCornerOri[c] = (freeCornerOri[b] + twistDelta[2]) % 3
        freeCornerPerm[b] = freeCornerPerm[a]
        freeCornerOri[b] = (freeCornerOri[a] + twistDelta[1]) % 3
        freeCornerPerm[a] = tp
        freeCornerOri[a] = (to + twistDelta[0]) % 3
    }

    fun encode(): Long {
        var code = 0L
        for (c in centers) code = code * 6L + c
        for (o in fixedCornerOri) code = code * 3L + o
        for (p in freeCornerPerm) code = code * 4L + p
        for (o in freeCornerOri) code = code * 3L + o
        return code
    }

    companion object {
        private val moves = listOf("R", "R'", "L", "L'", "U", "U'", "B", "B'")

        fun randomScramble(length: Int = 11): List<String> {
            val seq = mutableListOf<String>()
            var last = -1
            repeat(length) {
                var f: Int
                do { f = Random.nextInt(4) } while (f == last)
                last = f
                val base = listOf("R", "L", "U", "B")[f]
                seq.add(if (Random.nextBoolean()) base else "$base'")
            }
            return seq
        }

        fun inverseMove(m: String): String =
            if (m.endsWith("'")) m.dropLast(1) else "$m'"

        fun solve(start: SkewbState): List<String>? {
            if (start.isSolved()) return emptyList()
            val goal = SkewbState()

            val frontVisited = HashMap<Long, List<String>>(4096)
            val backVisited = HashMap<Long, List<String>>(4096)
            val frontQueue = ArrayDeque<Pair<SkewbState, List<String>>>()
            val backQueue = ArrayDeque<Pair<SkewbState, List<String>>>()

            frontVisited[start.encode()] = emptyList()
            backVisited[goal.encode()] = emptyList()
            frontQueue.add(start.copy() to emptyList())
            backQueue.add(goal to emptyList())

            for (depth in 1..6) {
                val fSize = frontQueue.size
                repeat(fSize) {
                    val (cur, path) = frontQueue.removeFirst()
                    val lastFace = path.lastOrNull()?.firstOrNull()
                    for (m in moves) {
                        if (m.first() == lastFace) continue
                        val next = cur.copy().applyMove(m)
                        val code = next.encode()
                        if (code !in frontVisited) {
                            val newPath = path + m
                            frontVisited[code] = newPath
                            val backMatch = backVisited[code]
                            if (backMatch != null) {
                                return newPath + backMatch.reversed().map { inverseMove(it) }
                            }
                            frontQueue.add(next to newPath)
                        }
                    }
                }
                val bSize = backQueue.size
                repeat(bSize) {
                    val (cur, path) = backQueue.removeFirst()
                    val lastFace = path.lastOrNull()?.firstOrNull()
                    for (m in moves) {
                        if (m.first() == lastFace) continue
                        val next = cur.copy().applyMove(m)
                        val code = next.encode()
                        if (code !in backVisited) {
                            val newPath = path + m
                            backVisited[code] = newPath
                            val frontMatch = frontVisited[code]
                            if (frontMatch != null) {
                                return frontMatch + newPath.reversed().map { inverseMove(it) }
                            }
                            backQueue.add(next to newPath)
                        }
                    }
                }
            }
            return null
        }
    }
}
