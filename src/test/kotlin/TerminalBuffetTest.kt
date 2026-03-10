package com.example

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class TerminalBufferTest {
    @Test
    fun `proper setup`() {
        val width = 80
        val height = 24
        val scrollbackMaxSize = 100
        val terminalBuffer = TerminalBuffer(scrollbackMaxSize).setup(width, height)
        val grid = terminalBuffer.grid
        val rows = grid.size
        val cols = grid[0].size
        assertEquals(grid.size, rows)
        assertEquals(grid[0].size, cols)
    }
}
