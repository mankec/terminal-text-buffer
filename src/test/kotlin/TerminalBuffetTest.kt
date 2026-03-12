package com.example

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class TerminalBufferTest {
    @Test
    fun `proper setup`() {
        val width = DEFAULT_WIDTH
        val height = DEFAULT_HEIGHT
        val scrollbackMaxSize = DEFAULT_SCROLLBACK_MAX_SIZE
        val foregroundColor = DEFAULT_FOREGROUND_COLOR
        val backgroundColor = DEFAULT_BACKGROUND_COLOR
        val style = DEFAULT_STYLE
        val terminalBuffer = TerminalBuffer(scrollbackMaxSize)
            .setup(width, height, foregroundColor, backgroundColor, style)

        val grid = terminalBuffer.grid
        val rows = grid.layout.size
        val cols = grid.layout[0].size
        assertEquals(rows, grid.layout.size)
        assertEquals(cols, grid.layout[0].size)

        val cell = grid.layout[0][0]
        assertEquals(null, cell.char)
        assertEquals(foregroundColor, cell.foregroundColor)
        assertEquals(backgroundColor, cell.backgroundColor)
        assertEquals(style, cell.style)
    }
}
