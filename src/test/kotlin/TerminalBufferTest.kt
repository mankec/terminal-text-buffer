package com.example

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.io.File
import kotlin.collections.joinToString
import kotlin.math.exp
import kotlin.test.BeforeTest
import kotlin.test.assertContains

const val TEST_WIDTH = 10
const val TEST_HEIGHT = 5
const val TEST_SCROLLBACK_MAX_SIZE = 5


class TerminalBufferTest {
    lateinit var terminal: TerminalBuffer

    private fun setupTerminalBuffer(
        height: Int? = null, scrollbackMaxSize: Int? = null
    ): TerminalBuffer {
        val width = TEST_WIDTH
        val height = height ?: TEST_HEIGHT
        val scrollbackMaxSize = scrollbackMaxSize ?: TEST_SCROLLBACK_MAX_SIZE
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
        Cursor.line = 0
        terminal = setupTerminalBuffer()
    }

    @Test
    fun `wrap text`() {
        val text = "Hello world"
        for (ch in text.map { it.toString() }) { terminal.write(ch) }

        val screen = terminal.screen
        val cursor = terminal.cursor
        val firstRow = screen.lines[0][0].joinToString("") { it.value }
        val secondRow = screen.lines[0][1].joinToString("") { it.value }

        var expected = "Hello worl"
        assertEquals(expected, firstRow)

        expected = "d"
        assertEquals(expected, secondRow)
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

        val row = terminal.screen.lines[0][0].joinToString("") { it.value }
        assertEquals(newText, row)
        assertEquals(newText.length, cursor.col)
    }

    @Test
    fun `insert text on new line`() {
        val text = "Hello"
        terminal.insert(text)

        val row = terminal.screen.lines[0][0].joinToString("") { it.value }
        assertEquals(text, row)
    }

    @Test
    fun `insert text without spaces between existing content`() {
        val hello = "Hello"
        val text = "$hello world"
        for (ch in text.map { it.toString() }) { terminal.write(ch) }

        val cursor = terminal.cursor
        cursor.moveToStartOfLine()
        cursor.row = 0
        cursor.moveRight(hello.length)

        val newText = "greetings"
        terminal.insert(newText)

        val firstRow = terminal.screen.lines[0][0].joinToString("") { it.value }
        val secondRow = terminal.screen.lines[0][1].joinToString("") { it.value }

        var expected = "Hellogreet"
        assertEquals(expected, firstRow)

        expected = "ings world"
        assertEquals(expected, secondRow)
    }

    @Test
    fun `insert text with spaces between existing content`() {
        val hello = "Hello"
        val text = "$hello world"
        for (ch in text.map { it.toString() }) { terminal.write(ch) }

        val cursor = terminal.cursor
        cursor.moveToStartOfLine()
        cursor.row = 0
        cursor.moveRight(hello.length)

        val newText = " greetings "
        terminal.insert(newText)

        val firstRow = terminal.screen.lines[0][0].joinToString("") { it.value }
        val secondRow = terminal.screen.lines[0][1].joinToString("") { it.value }
        val thirdRow = terminal.screen.lines[0][2].joinToString("") { it.value }

        var expected = "Hello gree"
        assertEquals(expected, firstRow)

        expected = "tings  wor"
        assertEquals(expected, secondRow)

        expected = "ld"
        assertEquals(expected, thirdRow)
    }

    @Test
    fun `fill a line with a character (or empty)`() {
        val text = "Hello"
        terminal.insert(text)

        var expected = text
        var firstRow = terminal.screen.lines[0][0].joinToString("") { it.value }
        assertEquals(expected, firstRow)

        val ch = "|"
        terminal.fill(ch)
        expected = ch.repeat(terminal.screen.width)
        firstRow = terminal.screen.lines[0][0].joinToString("") { it.value }
        assertEquals(expected, firstRow)

        terminal.fill()
        expected = EMPTY_STRING.repeat(terminal.screen.width)
        firstRow = terminal.screen.lines[0][0].joinToString("") { it.value }
        assertEquals(expected, firstRow)
    }

    @Test
    fun `move oldest line to scrollback if line count exceeds screen height`() {
        val height = 2
        val terminal = setupTerminalBuffer(height)
        val text1 = "Hello!"
        val text2 = "Hello!!"
        val text3 = "Hello!!!"

        terminal.insert(text1)
        terminal.enterNewLine()
        terminal.insert(text2)

        val screen = terminal.screen
        var firstLineRow = screen.lines[0][0].joinToString("") { it.value }
        var secondLineRow = screen.lines[1][0].joinToString("") { it.value }
        assertEquals(text1, firstLineRow)
        assertEquals(text2, secondLineRow)

        terminal.enterNewLine()
        terminal.insert(text3)
        assertEquals(2, screen.lines.size)

        firstLineRow = screen.lines[0][0].joinToString("") { it.value }
        secondLineRow = screen.lines[1][0].joinToString("") { it.value }
        assertEquals(text2, firstLineRow)
        assertEquals(text3, secondLineRow)

        val scrollbackFirstLineRow =
            terminal.scrollback.lines[0][0].joinToString("") { it.value }
        assertEquals(text1, scrollbackFirstLineRow)
    }

    @Test
    fun `remove oldest line from scrollback if scrollback max capacity is exceeded`() {
        val height = 1
        val scrollbackMaxSize = 1
        val terminal = setupTerminalBuffer(height, scrollbackMaxSize)
        val text1 = "Hello!"
        val text2 = "Hello!!"
        val text3 = "Hello!!!"

        terminal.insert(text1)
        terminal.enterNewLine()
        terminal.insert(text2)

        val screen = terminal.screen
        val scrollback = terminal.scrollback
        var firstLineRow = screen.lines[0][0].joinToString("") { it.value }
        assertEquals(text2, firstLineRow)

        var scrollbackFirstLineRow =
            scrollback.lines[0][0].joinToString("") { it.value }
        assertEquals(text1, scrollbackFirstLineRow)

        terminal.enterNewLine()
        terminal.insert(text3)
        assertEquals(1, screen.lines.size)
        assertEquals(1, scrollback.lines.size)

        firstLineRow = screen.lines[0][0].joinToString("") { it.value }
        assertEquals(text3, firstLineRow)

        scrollbackFirstLineRow =
            scrollback.lines[0][0].joinToString("") { it.value }
        assertEquals(text2, scrollbackFirstLineRow)
    }

    @Test
    fun `insert an empty line at the bottom of the screen`() {
        val text = "Hello"
        terminal.insert(text)
        terminal.insertEmptyLineAtBottomOfScreen()

        assertEquals(2, terminal.screen.lines.size)
        // Cursor position remains unchanged
        assertEquals(text.length, terminal.cursor.col)
        assertEquals(0, terminal.cursor.row)
        assertEquals(0, terminal.cursor.line)
    }

    @Test
    fun `clear entire screen`() {
        val text = "Hello"
        terminal.insert(text)
        terminal.insertEmptyLineAtBottomOfScreen()

        val firstRow = terminal.screen.lines[0][0].joinToString("") { it.value }
        assertEquals(text, firstRow)
        assertEquals(2, terminal.screen.lines.size)

        terminal.clearScreen()
        assertEquals(0, terminal.screen.lines.size)
        assertEquals(0, terminal.cursor.col)
        assertEquals(0, terminal.cursor.row)
        assertEquals(0, terminal.cursor.line)
    }
}
