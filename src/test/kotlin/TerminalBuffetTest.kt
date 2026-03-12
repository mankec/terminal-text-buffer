package com.example

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.assertContains
import kotlin.test.assertIsNot

class TerminalBufferTest {
    lateinit var buffer: TerminalBuffer
    val pwd = File(".").canonicalPath

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

    @BeforeTest
    fun setUp() {
        Cursor.row = 0
        Cursor.col = 0
        buffer = setupTerminalBuffer()
    }

    @Test
    fun `border is not modifiable`() {
        val rowIdx = 0
        val colIdx = 0
        val borderCell = buffer.grid.layout[rowIdx][colIdx]
        assertFalse(borderCell.modifiable)
    }

    @Test
    fun `text inside of border is modifiable`() {
        val rowIdx = 1
        val colIdx = 1
        val textCell = buffer.grid.layout[rowIdx][colIdx]
        assert(textCell.modifiable)
    }

    @Test
    fun `don't apply style to border, only colors`() {
        val style = AnsiEffect.UNDERLINE
        val customBuffer = setupTerminalBuffer(style)
        val rowIdx = 0
        val colIdx = 0
        val borderCell = customBuffer.grid.layout[rowIdx][colIdx]
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
