package com.example

const val DEFAULT_HEIGHT = 80
const val DEFAULT_WIDTH = 24
const val DEFAULT_SCROLLBACK_MAX_SIZE = 100
val DEFAULT_FOREGROUND_COLOR = AnsiColor.WHITE
val DEFAULT_BACKGROUND_COLOR = AnsiColor.BLUE
val DEFAULT_STYLE = AnsiEffect.NONE


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
            null,
            foregroundColor,
            backgroundColor,
            style
        )
        val layout = Array(height) { Array(width) { cell } }
        grid = Grid(layout)

        return this
    }
}

data class Grid(
    val layout: Array<Array<Cell>>,
)

data class Cell(
    val char: Char?,
    val foregroundColor: AnsiColor,
    val backgroundColor: AnsiColor,
    val style: AnsiEffect
)
