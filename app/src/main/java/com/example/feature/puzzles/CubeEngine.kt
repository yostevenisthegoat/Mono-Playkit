package com.example.feature.puzzles

import kotlin.random.Random

/**
 * Standard 3x3x3 Cube Representation & Offline Solver.
 * Face indices:
 *   0 = U (Yellow in solver, or White top depending on orientation; default: 0=U, 1=R, 2=F, 3=D, 4=L, 5=B)
 * Sticker indices 0..53:
 *   U: 0..8, R: 9..17, F: 18..26, D: 27..35, L: 36..44, B: 45..53
 * Within each face (looking straight at that face):
 *   0 1 2
 *   3 4 5
 *   6 7 8
 */
class CubeState(val stickers: IntArray = solvedArray()) {

    fun copy(): CubeState = CubeState(stickers.copyOf())

    fun isSolved(): Boolean {
        for (face in 0..5) {
            val center = stickers[face * 9 + 4]
            for (i in 0..8) {
                if (stickers[face * 9 + i] != center) return false
            }
        }
        return true
    }

    fun applyMove(move: String): CubeState {
        val base = move.firstOrNull()?.uppercaseChar() ?: return this
        val turns = when {
            move.endsWith("2") -> 2
            move.endsWith("'") -> 3
            else -> 1
        }
        repeat(turns) { quarterTurn(base) }
        return this
    }

    fun applyMoves(moves: List<String>): CubeState {
        for (m in moves) applyMove(m)
        return this
    }

    private fun rotateFaceCW(faceStart: Int) {
        val s = stickers
        val t0 = s[faceStart + 0]; val t1 = s[faceStart + 1]; val t2 = s[faceStart + 2]
        val t3 = s[faceStart + 3]; val t5 = s[faceStart + 5]
        val t6 = s[faceStart + 6]; val t7 = s[faceStart + 7]; val t8 = s[faceStart + 8]
        s[faceStart + 0] = t6; s[faceStart + 1] = t3; s[faceStart + 2] = t0
        s[faceStart + 3] = t7; s[faceStart + 5] = t1
        s[faceStart + 6] = t8; s[faceStart + 7] = t5; s[faceStart + 8] = t2
    }

    private fun cycle4(a: Int, b: Int, c: Int, d: Int) {
        val s = stickers
        val tmp = s[d]
        s[d] = s[c]
        s[c] = s[b]
        s[b] = s[a]
        s[a] = tmp
    }

    private fun quarterTurn(face: Char) {
        when (face) {
            'U' -> {
                rotateFaceCW(0)
                // F(18,19,20) -> L(36,37,38) -> B(45,46,47) -> R(9,10,11) -> F
                cycle4(18, 36, 45, 9)
                cycle4(19, 37, 46, 10)
                cycle4(20, 38, 47, 11)
            }
            'D' -> {
                rotateFaceCW(27)
                // F(24,25,26) -> R(15,16,17) -> B(51,52,53) -> L(42,43,44) -> F
                cycle4(24, 15, 51, 42)
                cycle4(25, 16, 52, 43)
                cycle4(26, 17, 53, 44)
            }
            'R' -> {
                rotateFaceCW(9)
                // U(2,5,8) -> B(51,48,45) -> D(29,32,35) -> F(20,23,26) -> U
                cycle4(2, 51, 29, 20)
                cycle4(5, 48, 32, 23)
                cycle4(8, 45, 35, 26)
            }
            'L' -> {
                rotateFaceCW(36)
                // U(0,3,6) -> F(18,21,24) -> D(27,30,33) -> B(53,50,47) -> U
                cycle4(0, 18, 27, 53)
                cycle4(3, 21, 30, 50)
                cycle4(6, 24, 33, 47)
            }
            'F' -> {
                rotateFaceCW(18)
                // U(6,7,8) -> R(9,12,15) -> D(29,28,27) -> L(44,41,38) -> U
                cycle4(6, 9, 29, 44)
                cycle4(7, 12, 28, 41)
                cycle4(8, 15, 27, 38)
            }
            'B' -> {
                rotateFaceCW(45)
                // U(2,1,0) -> L(36,39,42) -> D(33,34,35) -> R(17,14,11) -> U
                cycle4(2, 36, 33, 17)
                cycle4(1, 39, 34, 14)
                cycle4(0, 42, 35, 11)
            }
        }
    }

    companion object {
        fun solvedArray(): IntArray = IntArray(54) { it / 9 }

        fun generateWcaScramble(length: Int = 20): List<String> {
            val faces = arrayOf("U", "D", "R", "L", "F", "B")
            val modifiers = arrayOf("", "'", "2")
            val result = mutableListOf<String>()
            var lastFace = -1
            var secondLastFace = -1
            while (result.size < length) {
                val f = Random.nextInt(6)
                if (f == lastFace) continue
                // Avoid opposite-face redundancy like U D U
                if (secondLastFace != -1 && (f / 2 == lastFace / 2) && f == secondLastFace) continue
                result.add(faces[f] + modifiers[Random.nextInt(3)])
                secondLastFace = lastFace
                lastFace = f
            }
            return result
        }
    }
}

data class CubePhase(
    val title: String,
    val moves: List<String>,
)

data class CubeSolveResult(
    val isValid: Boolean,
    val errorMessage: String? = null,
    val phases: List<CubePhase> = emptyList(),
    val allMoves: List<String> = emptyList(),
)

object CubeSolverEngine {

    private val edgeStickers = arrayOf(
        7 to 19,  // 0: UF
        5 to 10,  // 1: UR
        1 to 46,  // 2: UB
        3 to 37,  // 3: UL
        28 to 25, // 4: DF
        32 to 16, // 5: DR
        34 to 52, // 6: DB
        30 to 43, // 7: DL
        23 to 12, // 8: FR
        21 to 41, // 9: FL
        48 to 14, // 10: BR
        50 to 39, // 11: BL
    )

    private val cornerStickers = arrayOf(
        intArrayOf(8, 20, 9),   // 0: UFR
        intArrayOf(6, 38, 18),  // 1: UFL
        intArrayOf(0, 47, 36),  // 2: UBL
        intArrayOf(2, 11, 45),  // 3: UBR
        intArrayOf(29, 15, 26), // 4: DFR
        intArrayOf(27, 24, 44), // 5: DFL
        intArrayOf(33, 42, 53), // 6: DBL
        intArrayOf(35, 51, 17), // 7: DBR
    )

    fun validate(rawStickers: IntArray): String? {
        if (rawStickers.size != 54) return "Cube must have 54 stickers."

        val centerColors = IntArray(6) { rawStickers[it * 9 + 4] }
        if (centerColors.toSet().size != 6) return "All 6 center stickers must be distinct colors."

        val counts = IntArray(6)
        for (c in rawStickers) {
            if (c !in 0..5) return "All stickers must have a valid color."
            counts[c]++
        }
        if (counts.any { it != 9 }) return "Each color must appear exactly 9 times."

        val normalized = normalizeByCenters(rawStickers)

        // Validate 12 edges
        val edgePerm = IntArray(12) { -1 }
        var edgeOriSum = 0
        for (slot in 0..11) {
            val (a, b) = edgeStickers[slot]
            val c1 = normalized[a]
            val c2 = normalized[b]
            val match = findCanonicalEdge(c1, c2) ?: return "Invalid edge colors at slot $slot."
            if (edgePerm.contains(match.first)) return "Duplicate edge piece detected."
            edgePerm[slot] = match.first
            edgeOriSum += match.second
        }
        if (edgeOriSum % 2 != 0) return "Edge parity error: one edge is flipped (unsolvable)."

        // Validate 8 corners
        val cornerPerm = IntArray(8) { -1 }
        var cornerOriSum = 0
        for (slot in 0..7) {
            val idx = cornerStickers[slot]
            val c0 = normalized[idx[0]]
            val c1 = normalized[idx[1]]
            val c2 = normalized[idx[2]]
            val match = findCanonicalCorner(c0, c1, c2) ?: return "Invalid corner piece colors."
            if (cornerPerm.contains(match.first)) return "Duplicate corner piece detected."
            cornerPerm[slot] = match.first
            cornerOriSum += match.second
        }
        if (cornerOriSum % 3 != 0) return "Corner twist error: one corner is twisted (unsolvable)."

        if (permParity(edgePerm) != permParity(cornerPerm)) {
            return "Swap parity error: two pieces are swapped (unsolvable)."
        }

        return null
    }

    fun solve(rawStickers: IntArray): CubeSolveResult {
        val error = validate(rawStickers)
        if (error != null) return CubeSolveResult(isValid = false, errorMessage = error)

        val normalized = normalizeByCenters(rawStickers)
        val state = CubeState(normalized)
        if (state.isSolved()) {
            return CubeSolveResult(isValid = true, phases = emptyList(), allMoves = emptyList())
        }

        val phases = mutableListOf<CubePhase>()

        // Phase 1: Bottom Cross (D face = 3)
        val crossMoves = solveBottomCross(state)
        if (crossMoves.isNotEmpty()) phases.add(CubePhase("Bottom Cross (D)", crossMoves))

        // Phase 2: Bottom Layer Corners (D face = 3)
        val cornerMoves = solveBottomCorners(state)
        if (cornerMoves.isNotEmpty()) phases.add(CubePhase("First Layer Corners", cornerMoves))

        // Phase 3: Middle Layer Edges
        val middleMoves = solveMiddleEdges(state)
        if (middleMoves.isNotEmpty()) phases.add(CubePhase("Middle Layer Edges", middleMoves))

        // Phase 4: Top Cross Orientation (OLL Cross, U = 0)
        val ollCrossMoves = orientTopCross(state)
        if (ollCrossMoves.isNotEmpty()) phases.add(CubePhase("Top Cross (OLL)", ollCrossMoves))

        // Phase 5: Top Corners Orientation (Sune OLL)
        val ollCornerMoves = orientTopCorners(state)
        if (ollCornerMoves.isNotEmpty()) phases.add(CubePhase("Orient Last Layer", ollCornerMoves))

        // Phase 6: Permute Top Corners (PLL Corners)
        val pllCornerMoves = permuteTopCorners(state)
        if (pllCornerMoves.isNotEmpty()) phases.add(CubePhase("Permute Top Corners", pllCornerMoves))

        // Phase 7: Permute Top Edges & Align U
        val pllEdgeMoves = permuteTopEdges(state)
        if (pllEdgeMoves.isNotEmpty()) phases.add(CubePhase("Permute Top Edges", pllEdgeMoves))

        if (!state.isSolved()) {
            val diag = buildString {
                val s = state.stickers
                val dCross = s[28] == 3 && s[25] == 2 && s[32] == 3 && s[16] == 1 && s[34] == 3 && s[52] == 5 && s[30] == 3 && s[43] == 4
                val dLayer = dCross && (27..35).all { s[it] == 3 } && s[24] == 2 && s[26] == 2 && s[15] == 1 && s[17] == 1 && s[51] == 5 && s[53] == 5 && s[42] == 4 && s[44] == 4
                val midLayer = dLayer && s[21] == 2 && s[23] == 2 && s[12] == 1 && s[14] == 1 && s[48] == 5 && s[50] == 5 && s[39] == 4 && s[41] == 4
                val uCross = midLayer && s[1] == 0 && s[3] == 0 && s[5] == 0 && s[7] == 0
                val uFace = uCross && (0..8).all { s[it] == 0 }
                val uCorners = uFace && s[18] == 2 && s[20] == 2 && s[9] == 1 && s[11] == 1 && s[45] == 5 && s[47] == 5 && s[36] == 4 && s[38] == 4
                append("cross=$dCross, f2lC=$dLayer, mid=$midLayer, ollC=$uCross, oll=$uFace, pllC=$uCorners")
            }
            return CubeSolveResult(isValid = false, errorMessage = "Solver could not verify solved state ($diag).")
        }

        val optimizedPhases = phases.mapNotNull { phase ->
            val clean = simplifyMoves(phase.moves)
            if (clean.isEmpty()) null else phase.copy(moves = clean)
        }
        val combined = simplifyMoves(optimizedPhases.flatMap { it.moves })
        return CubeSolveResult(
            isValid = true,
            phases = optimizedPhases,
            allMoves = combined,
        )
    }

    private fun normalizeByCenters(raw: IntArray): IntArray {
        val map = IntArray(6)
        for (face in 0..5) {
            val color = raw[face * 9 + 4]
            map[color] = face
        }
        return IntArray(54) { idx -> map[raw[idx]] }
    }

    private fun findCanonicalEdge(c1: Int, c2: Int): Pair<Int, Int>? {
        val solved = CubeState.solvedArray()
        for (id in 0..11) {
            val (a, b) = edgeStickers[id]
            if (c1 == solved[a] && c2 == solved[b]) return id to 0
            if (c1 == solved[b] && c2 == solved[a]) return id to 1
        }
        return null
    }

    private fun findCanonicalCorner(c0: Int, c1: Int, c2: Int): Pair<Int, Int>? {
        val solved = CubeState.solvedArray()
        for (id in 0..7) {
            val s = cornerStickers[id]
            val a = solved[s[0]]; val b = solved[s[1]]; val c = solved[s[2]]
            if (c0 == a && c1 == b && c2 == c) return id to 0
            if (c0 == c && c1 == a && c2 == b) return id to 1
            if (c0 == b && c1 == c && c2 == a) return id to 2
        }
        return null
    }

    private fun permParity(perm: IntArray): Int {
        var inversions = 0
        for (i in perm.indices) {
            for (j in i + 1 until perm.size) {
                if (perm[i] > perm[j]) inversions++
            }
        }
        return inversions % 2
    }

    private fun solveBottomCross(state: CubeState): List<String> {
        val allMoves = mutableListOf<String>()
        // Targets: DF(28,25)->(3,2), DR(32,16)->(3,1), DB(34,52)->(3,5), DL(30,43)->(3,4)
        val targets = listOf(
            Triple(28 to 25, 3 to 2, 0),
            Triple(32 to 16, 3 to 1, 1),
            Triple(34 to 52, 3 to 5, 2),
            Triple(30 to 43, 3 to 4, 3),
        )
        val candidateMoves = listOf(
            "U", "U'", "U2", "D", "D'", "D2",
            "R", "R'", "R2", "L", "L'", "L2",
            "F", "F'", "F2", "B", "B'", "B2",
        )
        for (stepIdx in targets.indices) {
            val (pair, colors, _) = targets[stepIdx]
            if (state.stickers[pair.first] == colors.first && state.stickers[pair.second] == colors.second) {
                continue
            }
            val found = bfsSolveCondition(state, candidateMoves, maxDepth = 7) { s ->
                var ok = true
                for (k in 0..stepIdx) {
                    val (p, c, _) = targets[k]
                    if (s.stickers[p.first] != c.first || s.stickers[p.second] != c.second) {
                        ok = false
                        break
                    }
                }
                ok
            }
            if (found != null) {
                state.applyMoves(found)
                allMoves.addAll(found)
            }
        }
        return allMoves
    }

    private fun solveBottomCorners(state: CubeState): List<String> {
        val allMoves = mutableListOf<String>()
        // DFR(29,15,26), DFL(27,24,44), DBL(33,42,53), DBR(35,51,17)
        val slots = listOf(
            Triple(intArrayOf(29, 15, 26), intArrayOf(3, 1, 2), listOf("R U R'", "R U' R'", "F' U' F", "R U2 R' U' R U R'")),
            Triple(intArrayOf(27, 24, 44), intArrayOf(3, 2, 4), listOf("F U F'", "F U' F'", "L' U' L", "F U2 F' U' F U F'")),
            Triple(intArrayOf(33, 42, 53), intArrayOf(3, 4, 5), listOf("L U L'", "L U' L'", "B' U' B", "L U2 L' U' L U L'")),
            Triple(intArrayOf(35, 51, 17), intArrayOf(3, 5, 1), listOf("B U B'", "B U' B'", "R' U' R", "B U2 B' U' B U B'")),
        )
        val uMoves = listOf("", "U", "U'", "U2")

        for (slotIdx in slots.indices) {
            val (pos, cols, triggers) = slots[slotIdx]
            if (cornerMatches(state, pos, cols)) continue

            // If target corner is stuck in another bottom slot (or wrong orientation in its own slot), pop it to U
            if (!findCornerInU(state, cols)) {
                for (popIdx in slotIdx until slots.size) {
                    val (pPos, _, pTriggers) = slots[popIdx]
                    if (cornerContainsColors(state, pPos, cols)) {
                        val popSeq = pTriggers.first().split(" ")
                        state.applyMoves(popSeq)
                        allMoves.addAll(popSeq)
                        break
                    }
                }
            }

            // Now align U and insert with matching trigger
            var solvedSlot = false
            for (u in uMoves) {
                for (trig in triggers) {
                    val seq = buildList {
                        if (u.isNotEmpty()) add(u)
                        addAll(trig.split(" "))
                    }
                    val trial = state.copy().applyMoves(seq)
                    if (cornerMatches(trial, pos, cols)) {
                        state.applyMoves(seq)
                        allMoves.addAll(seq)
                        solvedSlot = true
                        break
                    }
                }
                if (solvedSlot) break
            }
        }
        return allMoves
    }

    private fun cornerMatches(state: CubeState, pos: IntArray, cols: IntArray): Boolean =
        state.stickers[pos[0]] == cols[0] &&
            state.stickers[pos[1]] == cols[1] &&
            state.stickers[pos[2]] == cols[2]

    private fun cornerContainsColors(state: CubeState, pos: IntArray, cols: IntArray): Boolean {
        val set = setOf(state.stickers[pos[0]], state.stickers[pos[1]], state.stickers[pos[2]])
        return set == cols.toSet()
    }

    private fun findCornerInU(state: CubeState, cols: IntArray): Boolean {
        val uSlots = arrayOf(
            intArrayOf(8, 20, 9),
            intArrayOf(6, 38, 18),
            intArrayOf(0, 47, 36),
            intArrayOf(2, 11, 45),
        )
        return uSlots.any { cornerContainsColors(state, it, cols) }
    }

    private fun solveMiddleEdges(state: CubeState): List<String> {
        val allMoves = mutableListOf<String>()
        // Middle slots: FR(23,12)=(2,1), FL(21,41)=(2,4), BL(50,39)=(5,4), BR(48,14)=(5,1)
        val slots = listOf(
            Triple(23 to 12, 2 to 1, listOf("U R U' R' U' F' U F", "U' F' U F U R U' R'")),
            Triple(21 to 41, 2 to 4, listOf("U' L' U L U F U' F'", "U F U' F' U' L' U L")),
            Triple(50 to 39, 5 to 4, listOf("U L U' L' U' B' U B", "U' B' U B U L U' L'")),
            Triple(48 to 14, 5 to 1, listOf("U' R' U R U B U' B'", "U B U' B' U' R' U R")),
        )
        val uMoves = listOf("", "U", "U'", "U2")

        for (slotIdx in slots.indices) {
            val (pos, cols, algs) = slots[slotIdx]
            if (state.stickers[pos.first] == cols.first && state.stickers[pos.second] == cols.second) continue

            // Check if edge is trapped in any middle slot (including flipped in its own)
            var foundInU = findEdgeInU(state, cols)
            if (!foundInU) {
                for (popIdx in slotIdx until slots.size) {
                    val (pPos, _, pAlgs) = slots[popIdx]
                    val a = state.stickers[pPos.first]; val b = state.stickers[pPos.second]
                    if ((a == cols.first && b == cols.second) || (a == cols.second && b == cols.first)) {
                        val popSeq = pAlgs.first().split(" ")
                        state.applyMoves(popSeq)
                        allMoves.addAll(popSeq)
                        break
                    }
                }
            }

            var inserted = false
            for (u in uMoves) {
                for (alg in algs) {
                    val seq = buildList {
                        if (u.isNotEmpty()) add(u)
                        addAll(alg.split(" "))
                    }
                    val trial = state.copy().applyMoves(seq)
                    if (trial.stickers[pos.first] == cols.first && trial.stickers[pos.second] == cols.second) {
                        state.applyMoves(seq)
                        allMoves.addAll(seq)
                        inserted = true
                        break
                    }
                }
                if (inserted) break
            }
        }
        return allMoves
    }

    private fun findEdgeInU(state: CubeState, cols: IntArrayPair): Boolean {
        val uEdges = arrayOf(7 to 19, 5 to 10, 1 to 46, 3 to 37)
        return uEdges.any { (a, b) ->
            val sa = state.stickers[a]; val sb = state.stickers[b]
            (sa == cols.first && sb == cols.second) || (sa == cols.second && sb == cols.first)
        }
    }

    private fun orientTopCross(state: CubeState): List<String> {
        val moves = mutableListOf<String>()
        val fururf = listOf("F", "R", "U", "R'", "U'", "F'")
        repeat(8) {
            val s = state.stickers
            val ub = s[1] == 0; val ul = s[3] == 0; val ur = s[5] == 0; val uf = s[7] == 0
            if (ub && ul && ur && uf) return moves
            if (ul && ur && !ub && !uf) {
                state.applyMoves(fururf); moves.addAll(fururf)
            } else if (ub && uf && !ul && !ur) {
                state.applyMove("U"); moves.add("U")
                state.applyMoves(fururf); moves.addAll(fururf)
            } else if (ub && ul) {
                state.applyMoves(fururf); moves.addAll(fururf)
            } else if (ub || ul || ur || uf) {
                state.applyMove("U"); moves.add("U")
            } else {
                state.applyMoves(fururf); moves.addAll(fururf)
            }
        }
        return moves
    }

    private fun orientTopCorners(state: CubeState): List<String> {
        val moves = mutableListOf<String>()
        val sune = listOf("R", "U", "R'", "U", "R", "U2", "R'")
        val cornerU = intArrayOf(6, 8, 2, 0) // UFL, UFR, UBR, UBL

        repeat(14) {
            val oriented = cornerU.count { state.stickers[it] == 0 }
            if (oriented == 4) return moves
            when (oriented) {
                1 -> {
                    // Put the single oriented yellow sticker at UFL (index 6)
                    var guard = 0
                    while (state.stickers[6] != 0 && guard < 4) {
                        state.applyMove("U"); moves.add("U"); guard++
                    }
                    state.applyMoves(sune); moves.addAll(sune)
                }
                0 -> {
                    // Put yellow sticker on L face at UFL-left (index 38)
                    var guard = 0
                    while (state.stickers[38] != 0 && guard < 4) {
                        state.applyMove("U"); moves.add("U"); guard++
                    }
                    state.applyMoves(sune); moves.addAll(sune)
                }
                2 -> {
                    // Prefer yellow on F face at UFL-front (index 18), else L face at UFL-left (index 38)
                    var found18 = false
                    for (g in 0..3) {
                        if (state.stickers[18] == 0) {
                            found18 = true
                            break
                        }
                        state.applyMove("U"); moves.add("U")
                    }
                    if (!found18) {
                        var guard = 0
                        while (state.stickers[38] != 0 && guard < 4) {
                            state.applyMove("U"); moves.add("U"); guard++
                        }
                    }
                    state.applyMoves(sune); moves.addAll(sune)
                }
            }
        }
        return moves
    }

    private fun permuteTopCorners(state: CubeState): List<String> {
        val moves = mutableListOf<String>()
        // T-Perm: fixes UBL & UFL (headlights on L: 36==38) and swaps UFR <-> UBR
        val tPerm = listOf("R", "U", "R'", "U'", "R'", "F", "R2", "U'", "R'", "U'", "R", "U", "R'", "F'")

        fun allCornersPermuted(): Boolean {
            val s = state.stickers
            return s[18] == s[20] && s[9] == s[11] && s[45] == s[47] && s[36] == s[38]
        }

        if (!allCornersPermuted()) {
            // Find headlights and move to L (36==38)
            var foundHeadlights = false
            for (i in 0..3) {
                if (state.stickers[36] == state.stickers[38]) {
                    foundHeadlights = true
                    break
                }
                state.applyMove("U"); moves.add("U")
            }
            if (!foundHeadlights) {
                state.applyMoves(tPerm); moves.addAll(tPerm)
                for (i in 0..3) {
                    if (state.stickers[36] == state.stickers[38]) break
                    state.applyMove("U"); moves.add("U")
                }
            }
            state.applyMoves(tPerm); moves.addAll(tPerm)
        }

        // Align U corners with F center (color 2)
        var guard = 0
        while (state.stickers[18] != 2 && guard < 4) {
            state.applyMove("U"); moves.add("U"); guard++
        }
        return moves
    }

    private fun permuteTopEdges(state: CubeState): List<String> {
        val moves = mutableListOf<String>()
        val uaPerm = listOf("R", "U'", "R", "U", "R", "U", "R", "U'", "R'", "U'", "R2")
        // Top edges front/right/back/left stickers: F=19, R=10, B=46, L=37; corner references: 18, 9, 45, 36
        repeat(4) {
            if (state.isSolved()) return moves
            // If all 4 edges are unsolved, apply Ua once to solve one edge
            val bSolved = state.stickers[46] == state.stickers[45]
            val fSolved = state.stickers[19] == state.stickers[18]
            val rSolved = state.stickers[10] == state.stickers[9]
            val lSolved = state.stickers[37] == state.stickers[36]
            if (bSolved && fSolved && rSolved && lSolved) {
                while (!state.isSolved()) {
                    state.applyMove("U"); moves.add("U")
                }
                return moves
            }
            when {
                bSolved -> Unit
                fSolved -> { state.applyMove("U2"); moves.add("U2") }
                rSolved -> { state.applyMove("U'"); moves.add("U'") }
                lSolved -> { state.applyMove("U"); moves.add("U") }
            }
            state.applyMoves(uaPerm); moves.addAll(uaPerm)
            // Re-align corners
            var guard = 0
            while (state.stickers[18] != 2 && guard < 4) {
                state.applyMove("U"); moves.add("U"); guard++
            }
        }
        return moves
    }

    private fun bfsSolveCondition(
        start: CubeState,
        moves: List<String>,
        maxDepth: Int,
        goal: (CubeState) -> Boolean,
    ): List<String>? {
        if (goal(start)) return emptyList()
        fun crossKey(s: CubeState): Long {
            // Pack the 4 bottom-cross edge pairs + all 24 edge locations of colors containing 3 (D)
            var h = 0L
            for ((a, b) in edgeStickers) {
                val sa = s.stickers[a]
                val sb = s.stickers[b]
                val code = if (sa == 3 || sb == 3) (sa * 6 + sb + 1).toLong() else 0L
                h = h * 37L + code
            }
            return h
        }

        val visited = HashSet<Long>(2048)
        val queue = ArrayDeque<Pair<CubeState, List<String>>>()
        visited.add(crossKey(start))
        queue.add(start.copy() to emptyList())

        while (queue.isNotEmpty()) {
            val (cur, path) = queue.removeFirst()
            if (path.size >= maxDepth) continue
            val lastFace = path.lastOrNull()?.firstOrNull()
            for (m in moves) {
                if (m.first() == lastFace) continue
                val next = cur.copy().applyMove(m)
                if (goal(next)) return path + m
                val key = crossKey(next)
                if (visited.add(key)) {
                    queue.add(next to (path + m))
                }
            }
        }
        return null
    }

    fun simplifyMoves(moves: List<String>): List<String> {
        val stack = mutableListOf<Pair<Char, Int>>() // face to quarterTurns (1..3)
        for (m in moves) {
            if (m.isBlank()) continue
            val face = m.first().uppercaseChar()
            val q = when {
                m.endsWith("2") -> 2
                m.endsWith("'") -> 3
                else -> 1
            }
            if (stack.isNotEmpty() && stack.last().first == face) {
                val combined = (stack.last().second + q) % 4
                stack.removeAt(stack.lastIndex)
                if (combined != 0) stack.add(face to combined)
            } else {
                stack.add(face to q)
            }
        }
        return stack.map { (face, q) ->
            when (q) {
                1 -> "$face"
                2 -> "${face}2"
                else -> "$face'"
            }
        }
    }
}

private typealias IntArrayPair = Pair<Int, Int>
