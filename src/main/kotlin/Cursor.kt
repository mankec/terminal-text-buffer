package com.example


object Cursor {
    var row: Int = 0
    var col: Int = 0

    // "$ESC[${n}A"
    fun moveUp(n: Int) { row -= n }
    // "$ESC[${n}B"
    fun moveDown(n: Int) { row += n }
    // "$ESC[${n}C"
    fun moveRight(n: Int) { col += n }
    // "$ESC[${n}D"
    fun moveLeft(n: Int) { col -= n }

    fun moveToStartOfLine() { col = 0 }
}
