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
    lateinit var grid: Grid

    fun setup(
        width: Int,
        height: Int,
        foregroundColor: AnsiColor,
        backgroundColor: AnsiColor,
        style: AnsiEffect,
    ): TerminalBuffer {
        // Account for border
        val rowCount = height + 2
        val colCount = width + 2
        val layout = Array(rowCount) { rowIdx ->
            Array(colCount) { colIdx ->
                val cell = Cell(
                    foregroundColor,
                    backgroundColor,
                    style,
                )
                if (rowIdx == 0 || rowIdx == rowCount - 1) {
                    cell.value = "-"
                } else if (colIdx == 0 || colIdx == colCount - 1) {
                    cell.value = "|"
                } else {
                    cell.modifiable = true
                }
                cell
            }
        }
        grid = Grid(layout)

        return this
    }
}

data class Grid(
    val layout: Array<Array<Cell>>,
)

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
