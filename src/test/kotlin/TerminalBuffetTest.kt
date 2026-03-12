package com.example

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import kotlin.test.assertIsNot

class TerminalBufferTest {
    private fun setupTerminalBuffer(): TerminalBuffer {
        val width = DEFAULT_WIDTH
        val height = DEFAULT_HEIGHT
        val scrollbackMaxSize = DEFAULT_SCROLLBACK_MAX_SIZE
        val foregroundColor = DEFAULT_FOREGROUND_COLOR
        val backgroundColor = DEFAULT_BACKGROUND_COLOR
        val style = DEFAULT_STYLE
        val terminalBuffer = TerminalBuffer(scrollbackMaxSize)
            .setup(width, height, foregroundColor, backgroundColor, style)
        return terminalBuffer
    }

    @Test
    fun `border is not modifiable`() {
        val terminalBuffer = setupTerminalBuffer()
        var rowIdx = 0
        var colIdx = 0
        var cell = terminalBuffer.grid.layout[rowIdx][colIdx]
        assertFalse(cell.modifiable)
    }

    @Test
    fun `text inside of border is modifiable`() {
        val terminalBuffer = setupTerminalBuffer()
        var rowIdx = 1
        var colIdx = 1
        var cell = terminalBuffer.grid.layout[rowIdx][colIdx]
        assert(cell.modifiable)
    }
}
