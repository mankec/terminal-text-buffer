package com.example


const val DEFAULT_WIDTH = 80
const val DEFAULT_HEIGHT = 24
const val DEFAULT_SCROLLBACK_MAX_SIZE = 100
val DEFAULT_FOREGROUND_COLOR = AnsiColor.MAGENTA
val DEFAULT_BACKGROUND_COLOR = AnsiColor.BLACK
val DEFAULT_STYLE = AnsiEffect.NONE

const val EMPTY_STRING = " "


class TerminalBuffer(
    val scrollbackMaxSize: Int,
) {
    lateinit var screen: Screen
    lateinit var scrollback: Scrollback
    lateinit var cursor: Cursor

    fun setup(
        width: Int,
        height: Int,
        foregroundColor: AnsiColor,
        backgroundColor: AnsiColor,
        style: AnsiEffect,
    ): TerminalBuffer {
        screen = Screen.init(
            this, width, height, foregroundColor, backgroundColor, style
        )
        scrollback = Scrollback.init(width, height, scrollbackMaxSize)
        cursor = Cursor

        return this
    }

    fun write(ch: String) {
        val line = screen.lines.getOrElse(cursor.line) {
            screen.createLine()
        }
        val lineRow =
            line.getOrElse(cursor.row) {
                val newRow = mutableListOf<Cell>()
                line.add(newRow)
                newRow
            }
        val cell = lineRow.getOrElse(cursor.col) {
            val newCell = Screen.createCell()
            lineRow.add(newCell)
            newCell
        }
        cell.value = ch

        if (cursor.col == screen.eolIdx) {
            cursor.moveToStartOfLine()
            cursor.row++
        } else {
            cursor.col++
        }
    }

    fun insert(text: String) {
        var chars: List<String>
        val line = screen.lines.getOrNull(cursor.line)
        if (line == null) {
            chars = text.map { it.toString() }
        } else {
            var moveText = ""
            val wrap = cursor.col + text.length > screen.eolIdx
            if (wrap) {
                line.subList(cursor.row, line.size)
                    .mapIndexed { rowIdx, row ->
                        moveText +=
                            if (rowIdx == 0) {
                                row.subList(cursor.col, row.size)
                                    .joinToString(separator = "") { it.value }
                            } else {
                                row.joinToString(separator = "") { it.value }
                            }
                    }
            }
            val insertText = text + moveText
            chars = insertText.map { it.toString() }
        }
        for (ch in chars) { write(ch) }
    }

    fun fill(ch: String = EMPTY_STRING) {
        val chars = ch.repeat(screen.width).map { it.toString() }

        cursor.moveToStartOfLine()
        cursor.row = 0
        for (ch in chars) { write(ch) }
    }

    fun enterNewLine() {
        cursor.moveToStartOfLine()
        cursor.row = 0
        cursor.line++
    }

    fun insertEmptyLineAtBottomOfScreen() {
        screen.createLine()
    }

    fun clearScreen() {
        screen.lines.clear()
        cursor.reset()
    }

    fun clearAll() {
        screen.lines.clear()
        scrollback.lines.clear()
        cursor.reset()
    }
}


data class Cell(
    val foregroundColor: AnsiColor,
    val backgroundColor: AnsiColor,
    val style: AnsiEffect,
    var value: String = EMPTY_STRING,
    var modifiable: Boolean = false,
) {
    fun render(): String {
        val foregroundSeq = createAnsiSequence(
            AnsiPlacementIntensity.BRIGHT_FOREGROUND.code,
            foregroundColor.code
        )
        val backgroundSeq = createAnsiSequence(
            AnsiPlacementIntensity.STANDARD_BACKGROUND.code,
            backgroundColor.code
        )
        val styleSeq =
            if (!modifiable || style == AnsiEffect.NONE) ""
            else createAnsiSequence(requireNotNull(style.on))
        val resetSeq = "${ANSI_CSI}${AnsiColor.RESET.code}m"
        return "${backgroundSeq}${foregroundSeq}${styleSeq}${value}${resetSeq}"
    }

    fun createAnsiSequence(code1: Int, code2: Int? = null): String {
        return "${ANSI_CSI}${code1}${code2 ?: ""}m"
    }
}


object Screen {
    lateinit var terminal: TerminalBuffer
    var width: Int = 0
    var height: Int = 0
    var eolIdx: Int = 0
    lateinit var foregroundColor: AnsiColor
    lateinit var backgroundColor: AnsiColor
    lateinit var style: AnsiEffect
    // Line can have multiple rows in case wrapping happens
    lateinit var lines: MutableList<MutableList<MutableList<Cell>>>

    fun init(
        terminal: TerminalBuffer,
        width: Int,
        height: Int,
        foregroundColor: AnsiColor,
        backgroundColor: AnsiColor,
        style: AnsiEffect,
    ): Screen {
        this.terminal = terminal
        this.width = width
        this.height = height
        this.eolIdx = width - 1
        this.foregroundColor = foregroundColor
        this.backgroundColor = backgroundColor
        this.style = style
        this.lines = mutableListOf()
        return this
    }

    fun createCell(): Cell = Cell(
        foregroundColor,
        backgroundColor,
        style,
        EMPTY_STRING,
        false,
    )

    fun createLine(): MutableList<MutableList<Cell>> {
        val newLine = mutableListOf<MutableList<Cell>>()
        lines.add(newLine)
        if (lines.size > height) {
            terminal.cursor.line--
            moveFirstLineToScrollback()
        }
        return newLine
    }

    private fun moveFirstLineToScrollback() {
        val immutableLine = lines[0].map { it.toList() }.toList()
        lines.removeFirst()
        terminal.scrollback.addLine(immutableLine)
    }
}


object Scrollback {
    var maxSize: Int = 0
    lateinit var lines: MutableList<List<List<Cell>>>

    fun init(width: Int, height: Int, maxSize: Int): Scrollback {
        this.maxSize = maxSize
        this.lines = mutableListOf()
        return this
    }

    fun addLine(line: List<List<Cell>>) {
        lines.add(line)
        if (lines.size > maxSize)
            lines.removeFirst()
    }
}
