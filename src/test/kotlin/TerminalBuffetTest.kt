package com.example

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import kotlin.test.assertContains
import kotlin.test.assertIsNot

class TerminalBufferTest {
    private fun setupTerminalBuffer(style: AnsiEffect? = null): TerminalBuffer {
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
        val rowIdx = 0
        val colIdx = 0
        val borderCell = terminalBuffer.grid.layout[rowIdx][colIdx]
        assertFalse(borderCell.modifiable)
    }

    @Test
    fun `text inside of border is modifiable`() {
        val terminalBuffer = setupTerminalBuffer()
        val rowIdx = 1
        val colIdx = 1
        val textCell = terminalBuffer.grid.layout[rowIdx][colIdx]
        assert(textCell.modifiable)
    }

    @Test
    fun `don't apply style to border, only colors`() {
        val style = AnsiEffect.UNDERLINE
        val terminalBuffer = setupTerminalBuffer(style)
        val rowIdx = 0
        val colIdx = 0
        val borderCell = terminalBuffer.grid.layout[rowIdx][colIdx]
        val rendered = borderCell.render()
        val foregroundSeq = borderCell.createAnsiSequence(
            AnsiPlacementIntensity.BRIGHT_FOREGROUND.code,
            borderCell.foregroundColor.code
        )
        val backgroundSeq = borderCell.createAnsiSequence(
            AnsiPlacementIntensity.STANDARD_BACKGROUND.code,
            borderCell.backgroundColor.code
        )
        val styleSeq = borderCell.createAnsiSequence(requireNotNull(style.on))
        assertContains(rendered, foregroundSeq)
        assertContains(rendered, backgroundSeq)
        assertFalse(styleSeq in rendered)
    }
}
