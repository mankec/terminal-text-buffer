package com.example

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import kotlin.collections.joinToString
import kotlin.test.BeforeTest
import kotlin.test.assertFailsWith


const val TEST_WIDTH = 10
const val TEST_HEIGHT = 5
const val TEST_SCROLLBACK_MAX_SIZE = 5


class TerminalBufferTest {
    lateinit var terminal: TerminalBuffer

    private fun setupTerminalBuffer(
        height: Int? = null, scrollbackMaxSize: Int? = null, style: AnsiEffect? = null
    ): TerminalBuffer {
        val width = TEST_WIDTH
        val height = height ?: TEST_HEIGHT
        val scrollbackMaxSize = scrollbackMaxSize ?: TEST_SCROLLBACK_MAX_SIZE
        val foregroundColor = DEFAULT_FOREGROUND_COLOR
        val backgroundColor = DEFAULT_BACKGROUND_COLOR
        val style = style ?: DEFAULT_STYLE
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
        var screenFirstLineRow = screen.lines[0][0].joinToString("") { it.value }
        assertEquals(text1, screenFirstLineRow)
        var screenSecondLineRow = screen.lines[1][0].joinToString("") { it.value }
        assertEquals(text2, screenSecondLineRow)

        terminal.enterNewLine()
        terminal.insert(text3)
        assertEquals(3, screen.lines.size)

        // Scrollback
        val lineIdx = 0
        val scrollbackFirstLineRow =
            terminal.screen.lines[lineIdx][0].joinToString("") { it.value }
        assertEquals(text1, scrollbackFirstLineRow)
        assertTrue(screen.isLineFrozen(lineIdx))

        // Screen
        screenFirstLineRow = screen.lines[1][0].joinToString("") { it.value }
        assertEquals(text2, screenFirstLineRow)
        screenSecondLineRow = screen.lines[2][0].joinToString("") { it.value }
        assertEquals(text3, screenSecondLineRow)
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
        var scrollbackFirstLineRow =
            screen.lines[0][0].joinToString("") { it.value }
        assertEquals(text1, scrollbackFirstLineRow)

        var screenFirstLineRow = screen.lines[1][0].joinToString("") { it.value }
        assertEquals(text2, screenFirstLineRow)

        terminal.enterNewLine()
        terminal.insert(text3)
        assertEquals(2, screen.lines.size)

        scrollbackFirstLineRow =
            screen.lines[0][0].joinToString("") { it.value }
        assertEquals(text2, scrollbackFirstLineRow)

        screenFirstLineRow = screen.lines[1][0].joinToString("") { it.value }
        assertEquals(text3, screenFirstLineRow)

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
    fun `clear screen`() {
        val height = 1
        val scrollbackMaxSize = 1
        val terminal = setupTerminalBuffer(height, scrollbackMaxSize)
        val text1 = "Hello!"
        val text2 = "Hello!!"

        terminal.insert(text1)
        terminal.enterNewLine()
        terminal.insert(text2)

        assertEquals(2, terminal.screen.lines.size)

        var scrollbackFirstLineRow =
            terminal.screen.lines[0][0].joinToString("") { it.value }
        assertEquals(text1, scrollbackFirstLineRow)
        val screenFirstLineRow =
            terminal.screen.lines[1][0].joinToString("") { it.value }
        assertEquals(text2, screenFirstLineRow)

        terminal.clearScreen()
        assertEquals(1, terminal.screen.lines.size)

        scrollbackFirstLineRow =
            terminal.screen.lines[0][0].joinToString("") { it.value }
        assertEquals(text1, scrollbackFirstLineRow)

        assertEquals(0, terminal.cursor.col)
        assertEquals(0, terminal.cursor.row)
        assertEquals(0, terminal.cursor.line)
    }


    @Test
    fun `clear all (screen and scrollback)`() {
        val height = 1
        val scrollbackMaxSize = 1
        val terminal = setupTerminalBuffer(height, scrollbackMaxSize)
        val text1 = "Hello!"
        val text2 = "Hello!!"

        terminal.insert(text1)
        terminal.enterNewLine()
        terminal.insert(text2)

        val screen = terminal.screen
        val scrollbackFirstLineRow =
            screen.lines[0][0].joinToString("") { it.value }
        assertEquals(text1, scrollbackFirstLineRow)

        val firstScreenLineRow = screen.lines[1][0].joinToString("") { it.value }
        assertEquals(text2, firstScreenLineRow)


        terminal.clearAll()
        assertEquals(0, terminal.screen.lines.size)
        assertEquals(0, terminal.cursor.col)
        assertEquals(0, terminal.cursor.row)
        assertEquals(0, terminal.cursor.line)
    }

    @Test
    fun `get attributes at position (from screen and scrollback)`() {
        val text1 = "Hello!!!       Match this -> A"
        val text2 = "Yes?"
        val height = 1
        val scrollbackMaxSize = 1
        val style = AnsiEffect.UNDERLINE
        val terminal = setupTerminalBuffer(height, scrollbackMaxSize, style)

        terminal.insert(text1)
        terminal.enterNewLine()
        terminal.insert(text2)

        // From scrollback
        var line = 0
        var expectedValue = "A"
        var col = text1.indexOf(expectedValue)
        var cellFg = terminal.getAttrFromPosition("fg", col, line)
        var cellBg = terminal.getAttrFromPosition("bg", col, line)
        var cellStyle = terminal.getAttrFromPosition("style", col, line)
        var cellValue = terminal.getAttrFromPosition("val", col, line)
        assertEquals(DEFAULT_FOREGROUND_COLOR, cellFg)
        assertEquals(DEFAULT_BACKGROUND_COLOR, cellBg)
        assertEquals(AnsiEffect.UNDERLINE, cellStyle)
        assertEquals(expectedValue, cellValue)

        // From screen
        line = 1
        expectedValue = "?"
        col = text2.indexOf(expectedValue)
        cellFg = terminal.getAttrFromPosition("fg", col, line)
        cellBg = terminal.getAttrFromPosition("bg", col, line)
        cellStyle = terminal.getAttrFromPosition("style", col, line)
        cellValue = terminal.getAttrFromPosition("val", col, line)
        assertEquals(DEFAULT_FOREGROUND_COLOR, cellFg)
        assertEquals(DEFAULT_BACKGROUND_COLOR, cellBg)
        assertEquals(AnsiEffect.UNDERLINE, cellStyle)
        assertEquals(expectedValue, cellValue)

        val invalid = "invalid"
        val exception = assertFailsWith<RuntimeException> {
            terminal.getAttrFromPosition("invalid", col, line)
        }
        assertEquals("Unknown attribute: $invalid", exception.message)
    }

    @Test
    fun `get line as string (from screen and scrollback)`() {
        val text1 = "Hello there this is a simple sentence"
        val text2 = " Yet  another   simple  sentence "
        val height = 1
        val scrollbackMaxSize = 1
        val terminal = setupTerminalBuffer(height, scrollbackMaxSize)
        val screen = terminal.screen

        terminal.insert(text1)
        terminal.enterNewLine()
        terminal.insert(text2)

        // Scrollback
        var lineIdx = 0
        val result1 = terminal.getLineAsString(lineIdx)
        assertTrue(screen.isLineFrozen(lineIdx))
        assertEquals(text1, result1)

        // Screen
        lineIdx++
        val result2 = terminal.getLineAsString(lineIdx)
        assertEquals(text2, result2)
    }

    @Test
    fun `disallow editing scrollback`() {
        val text1 = "Hello!!!"
        val text2 = "Yes?"
        val height = 1
        val scrollbackMaxSize = 1
        val terminal = setupTerminalBuffer(height, scrollbackMaxSize)
        val screen = terminal.screen
        val cursor = terminal.cursor

        terminal.insert(text1)
        terminal.enterNewLine()
        terminal.insert(text2)

        cursor.moveToStartOfLine()
        cursor.row = 0
        cursor.line = 0

        val newText1 = "Hey!"
        terminal.insert(newText1)

        var lineIdx = 0
        val scrollbackFirstLineRow =
            screen.lines[lineIdx][0].joinToString("") { it.value }
        print(scrollbackFirstLineRow)
        assertEquals(text1, scrollbackFirstLineRow)
        assertTrue(screen.isLineFrozen(lineIdx))

        cursor.moveToStartOfLine()
        cursor.row = 0
        cursor.line = 1
        val newText2 = "Yes?????"
        terminal.insert(newText2)

        lineIdx++
        val screenFirstLineRow =
            screen.lines[lineIdx][0].joinToString("") { it.value }
        assertEquals(newText2, screenFirstLineRow)
        assertFalse(screen.isLineFrozen(lineIdx))
    }
}
