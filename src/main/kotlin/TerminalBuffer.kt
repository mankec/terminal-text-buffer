package com.example


const val DEFAULT_WIDTH = 20
const val DEFAULT_HEIGHT = 3
const val DEFAULT_SCROLLBACK_MAX_SIZE = 100
val DEFAULT_FOREGROUND_COLOR = AnsiColor.MAGENTA
val DEFAULT_BACKGROUND_COLOR = AnsiColor.BLACK
val DEFAULT_STYLE = AnsiEffect.NONE

const val EMPTY_STRING = " "


class TerminalBuffer(
    val scrollbackMaxSize: Int,
) {
    lateinit var screen: Screen
    lateinit var cursor: Cursor

    fun setup(
        width: Int,
        height: Int,
        foregroundColor: AnsiColor,
        backgroundColor: AnsiColor,
        style: AnsiEffect,
    ): TerminalBuffer {
        screen = Screen.init(
            this,
            width,
            height,
            scrollbackMaxSize,
            foregroundColor,
            backgroundColor,
            style,
        )
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
            val newCell = screen.createCell()
            lineRow.add(newCell)
            newCell
        }

        if (cell.frozen)
            return

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
        screen.lines.removeIf { !it[0][0].frozen }
        screen.resetCursor()
    }

    fun clearAll() {
        screen.lines.clear()
        screen.resetCursor()
    }

    fun getAttrFromPosition(attr: String, col: Int, lineIdx: Int): Any {
        val line = screen.lines[lineIdx]
        val flattenedRows = line.flatten()
        val cell = flattenedRows[col]

        when (attr) {
            "fg" -> {
                return cell.foregroundColor
            }
            "bg" -> {
                return cell.backgroundColor
            }
            "style" -> {
                return cell.style
            }
            "val" -> {
                return cell.value
            }
            else -> {
                throw RuntimeException("Unknown attribute: $attr")
            }
        }
    }

    fun getLineAsString(
        lineIdx: Int? = null, line: MutableList<MutableList<Cell>>? = null): String
    {
        val flattenedRows =
            if (lineIdx != null) screen.lines[lineIdx].flatten()
            else line?.flatten()
            ?: throw IllegalArgumentException("Provide either lineIdx or line")
        return flattenedRows.joinToString(separator = "") { it.value }
    }

    fun getScreenContentAsString(): String {
        val nonFrozenLines = screen.getNonFrozenLines()
        val stringifiedLines = nonFrozenLines.map { line ->
            getLineAsString(line = line)
        }
        return stringifiedLines.joinToString("\n")
    }

    fun getEntireContentAsString(): String {
        val stringifiedLines = List(screen.lines.size) { idx ->
            getLineAsString(idx)
        }
        return stringifiedLines.joinToString("\n")
    }
}


data class Cell(
    var foregroundColor: AnsiColor,
    var backgroundColor: AnsiColor,
    val style: AnsiEffect,
    var value: String = EMPTY_STRING,
    var frozen: Boolean = false,
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
            if (!frozen || style == AnsiEffect.NONE) ""
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
    var scrollbackMaxSize = 0
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
        scrollbackMaxSize: Int,
        foregroundColor: AnsiColor,
        backgroundColor: AnsiColor,
        style: AnsiEffect,
    ): Screen {
        this.terminal = terminal
        this.width = width
        this.height = height
        this.scrollbackMaxSize = scrollbackMaxSize
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
        val cell = createCell()
        val newLine = mutableListOf(mutableListOf(cell))
        lines.add(newLine)

        val nonFrozenLines = getNonFrozenLines()
        if (nonFrozenLines.size > height) {
            nonFrozenLines[0].forEach { row ->
                row.forEach { it.frozen = true }
            }
        }

        val frozenLines = lines.filter { it[0][0].frozen }.toMutableList()
        if (frozenLines.size > scrollbackMaxSize) {
            lines.remove(frozenLines[0])
            terminal.cursor.line--
        }

        return newLine
    }

    fun isLineFrozen(lineIdx: Int): Boolean {
        return lines[lineIdx][0][0].frozen
    }

    fun getNonFrozenLines(): MutableList<MutableList<MutableList<Cell>>> {
        return lines.filter { !it[0][0].frozen }.toMutableList()
    }

    fun resetCursor() {
        val cursor = terminal.cursor
        cursor.row = 0
        cursor.moveToStartOfLine()
        val nonFrozenLines = getNonFrozenLines()
        val resetToLine =
            if (nonFrozenLines.isNotEmpty()) lines.indexOf(getNonFrozenLines()[0])
            else 0
        cursor.line = resetToLine
    }
}
