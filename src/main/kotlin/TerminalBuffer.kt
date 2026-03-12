package com.example

const val DEFAULT_WIDTH = 80
const val DEFAULT_HEIGHT = 24
const val DEFAULT_SCROLLBACK_MAX_SIZE = 100
val DEFAULT_FOREGROUND_COLOR = AnsiColor.WHITE
val DEFAULT_BACKGROUND_COLOR = AnsiColor.BLUE
val DEFAULT_STYLE = AnsiEffect.NONE

const val EMPTY_CHAR = ' '


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
        val cell = Cell(
            foregroundColor,
            backgroundColor,
            style,
        )
        // Account for border
        var charValue: Char
        var modifiable: Boolean
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
                    cell.modifiable = false
                    cell.char = '-'
                } else if (colIdx == 0 || colIdx == colCount - 1) {
                    cell.modifiable = false
                    cell.char = '|'
                } else {
                    cell.modifiable = true
                    cell.char = EMPTY_CHAR
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
    var char: Char = EMPTY_CHAR,
    var modifiable: Boolean = true,
)
