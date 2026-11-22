package com.example

import com.example.feature.puzzles.CubeSolverEngine
import com.example.feature.puzzles.CubeState
import com.example.feature.puzzles.PyraminxState
import com.example.feature.puzzles.SkewbState
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PuzzleEngineTest {

    @Test
    fun rubiksCubeSolver_solves100RandomScrambles() {
        repeat(100) { idx ->
            val scramble = CubeState.generateWcaScramble(22)
            val state = CubeState().applyMoves(scramble)
            val validationErr = CubeSolverEngine.validate(state.stickers)
            assertNull("Validation failed on cube $idx: $validationErr", validationErr)

            val result = CubeSolverEngine.solve(state.stickers)
            assertTrue("Solve failed on cube $idx: ${result.errorMessage}", result.isValid)

            val verify = CubeState(state.stickers.copyOf()).applyMoves(result.allMoves)
            assertTrue("Cube $idx not solved after applying solution moves!", verify.isSolved())
        }
    }

    @Test
    fun rubiksCubeValidator_rejectsParityAndInvalidCubes() {
        // Flip UF edge (7 <-> 19) -> edge orientation parity error
        val edgeFlip = CubeState.solvedArray()
        val tmpE = edgeFlip[7]; edgeFlip[7] = edgeFlip[19]; edgeFlip[19] = tmpE
        assertNotNull(CubeSolverEngine.validate(edgeFlip))

        // Twist UFR corner (8 -> 20 -> 9 -> 8) -> corner twist parity error
        val cornerTwist = CubeState.solvedArray()
        val c8 = cornerTwist[8]; cornerTwist[8] = cornerTwist[9]; cornerTwist[9] = cornerTwist[20]; cornerTwist[20] = c8
        assertNotNull(CubeSolverEngine.validate(cornerTwist))

        // Swap two edges UF <-> UR -> permutation parity error
        val edgeSwap = CubeState.solvedArray()
        val a = edgeSwap[7]; val b = edgeSwap[19]
        edgeSwap[7] = edgeSwap[5]; edgeSwap[19] = edgeSwap[10]
        edgeSwap[5] = a; edgeSwap[10] = b
        assertNotNull(CubeSolverEngine.validate(edgeSwap))
    }

    @Test
    fun pyraminxSolver_solves30RandomScrambles() {
        repeat(30) { idx ->
            val scramble = PyraminxState.randomScramble()
            val state = PyraminxState()
            scramble.forEach { state.applyMove(it) }
            val solution = PyraminxState.solve(state)
            assertNotNull("Pyraminx solve returned null on $idx", solution)
            val verify = state.copy()
            solution!!.forEach { verify.applyMove(it) }
            assertTrue("Pyraminx $idx not solved!", verify.isSolved())
        }
    }

    @Test
    fun skewbSolver_solves30RandomScrambles() {
        repeat(30) { idx ->
            val scramble = SkewbState.randomScramble(12)
            val state = SkewbState()
            scramble.forEach { state.applyMove(it) }
            val solution = SkewbState.solve(state)
            assertNotNull("Skewb solve returned null on $idx", solution)
            val verify = state.copy()
            solution!!.forEach { verify.applyMove(it) }
            assertTrue("Skewb $idx not solved!", verify.isSolved())
        }
    }
}
