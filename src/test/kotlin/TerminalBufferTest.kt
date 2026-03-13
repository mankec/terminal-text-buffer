package com.example

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.io.File
import kotlin.collections.joinToString
import kotlin.test.BeforeTest
import kotlin.test.assertContains

const val TEST_WIDTH = 10
const val TEST_HEIGHT = 5
const val TEST_SCROLLBACK_MAX_SIZE = 5


class TerminalBufferTest {
    lateinit var terminal: TerminalBuffer

    private fun setupTerminalBuffer(style: AnsiEffect? = null): TerminalBuffer {
        val width = TEST_WIDTH
        val height = TEST_HEIGHT
        val scrollbackMaxSize = TEST_SCROLLBACK_MAX_SIZE
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
        terminal = setupTerminalBuffer()
    }

    @Test
    fun `wrap text`() {
        val text = "Hello world"
        for (ch in text.map { it.toString() }) { terminal.write(ch) }

        val screen = terminal.screen
        val cursor = terminal.cursor
        val firstLine = screen.lines[0].joinToString("") { it.value }
        val secondLine = screen.lines[1].joinToString("") { it.value }

        var expected = "Hello worl"
        assertEquals(expected, firstLine)

        expected = "d"
        assertEquals(expected, secondLine)
        assertEquals(expected.length, cursor.col)
    }

    @Test
    fun `overwrite existing content`() {
        val text = "Hello"
        for (ch in text.map { it.toString() }) { terminal.write(ch) }

        val cursor = terminal.cursor
        cursor.moveToStartOfLine()
        val newText = "Greetings"
        for (ch in newText.map { it.toString() }) { terminal.write(ch) }

        val line = terminal.screen.lines[0].joinToString("") { it.value }
        assertEquals(newText, line)
        assertEquals(newText.length, cursor.col)
    }
}
