package com.example


const val DEFAULT_WIDTH = 10
const val DEFAULT_HEIGHT = 5
const val DEFAULT_SCROLLBACK_MAX_SIZE = 5
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
            width, height, foregroundColor, backgroundColor, style
        )
        scrollback = Scrollback.init(width, height, scrollbackMaxSize)
        cursor = Cursor

        return this
    }

    fun write(ch: String) {
        var cell: Cell?
        val maxCol = screen.width - 1
        val line = screen.lines.getOrNull(cursor.row)

        if (line != null) {
            cell = line.getOrNull(cursor.col)
            if (cell != null) {
                cell.value = ch
            } else {
                cell = Screen.createCell(ch)
                line.add(cell)
            }
            line[cursor.col] = cell
        } else {
            cell = Screen.createCell(ch)
            screen.lines.add(mutableListOf(cell))
        }

        if (cursor.col == maxCol) {
            cursor.row++
            cursor.col = 0
        } else {
            cursor.col++
        }
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
    var width: Int = 0
    var height: Int = 0
    lateinit var foregroundColor: AnsiColor
    lateinit var backgroundColor: AnsiColor
    lateinit var style: AnsiEffect
    lateinit var lines: MutableList<MutableList<Cell>>

    fun init(
        width: Int,
        height: Int,
        foregroundColor: AnsiColor,
        backgroundColor: AnsiColor,
        style: AnsiEffect,
    ): Screen {
        this.width = width
        this.height = height
        this.foregroundColor = foregroundColor
        this.backgroundColor = backgroundColor
        this.style = style
        this.lines = mutableListOf()
        return this
    }

    fun createCell(
        value: String = EMPTY_STRING, modifiable: Boolean = false
    ): Cell = Cell(
        foregroundColor,
        backgroundColor,
        style,
        value,
        modifiable,
    )
}


object Scrollback {
    var width: Int = 0
    var height: Int = 0
    var maxSize: Int = 0

    fun init(width: Int, height: Int, maxSize: Int): Scrollback {
        this.width = width
        this.height = height
        this.maxSize = maxSize
        return this
    }
}
