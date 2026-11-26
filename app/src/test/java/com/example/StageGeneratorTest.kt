package com.example

import com.example.brickquest.engine.ARENA_COLS
import com.example.brickquest.engine.BrickQuestEngine
import com.example.brickquest.engine.BrickQuestMode
import com.example.brickquest.engine.DANGER_ROW
import com.example.brickquest.engine.MAX_STAGE
import com.example.brickquest.engine.StageGenerator
import com.example.brickquest.engine.TurnPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StageGeneratorTest {

    @Test
    fun `every stage is valid`() {
        for (s in 1..MAX_STAGE) {
            val bp = StageGenerator.generate(s)
            val cells = HashSet<Int>()
            assertTrue("stage $s has too few bricks", bp.bricks.size >= 10)
            for (b in bp.bricks) {
                assertTrue("stage $s brick col", b.col in 0 until ARENA_COLS)
                assertTrue("stage $s brick row", b.row in 1 until DANGER_ROW)
                assertTrue("stage $s brick hp", b.hp in 1..999)
                assertTrue("stage $s duplicate brick", cells.add(b.row * 100 + b.col))
            }
            for (p in bp.pickups) {
                assertTrue("stage $s pickup row", p.row in 1 until DANGER_ROW)
                assertTrue("stage $s pickup overlaps", cells.add(p.row * 100 + p.col))
            }
        }
    }

    @Test
    fun `generation is deterministic`() {
        for (s in listOf(1, 77, 500, MAX_STAGE)) {
            val a = StageGenerator.generate(s)
            val b = StageGenerator.generate(s)
            assertEquals(a.bricks, b.bricks)
            assertEquals(a.pickups, b.pickups)
        }
    }

    @Test
    fun `a shot always ends thanks to auto speed-up and recall`() {
        val e = BrickQuestEngine(BrickQuestMode.STAGE, 300)
        e.aimAngleRad = (-Math.PI / 2 + 0.05).toFloat()
        e.isDraggingAim = true
        e.fireVolley()
        var t = 0f
        while (e.phase == TurnPhase.SHOOTING && t < 60f) {
            e.update(1f / 60f)
            t += 1f / 60f
        }
        assertTrue("shot took $t s", t < BrickQuestEngine.AUTO_RECALL_AFTER_SEC + 3f)
    }
}
