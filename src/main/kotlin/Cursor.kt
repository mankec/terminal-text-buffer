package com.example


object Cursor {
    var col: Int = 0
    var row: Int = 0

    fun moveUp(n: Int) = "${ANSI_CSI}${n}A"
    fun moveDown(n: Int) = "${ANSI_CSI}${n}B"
    fun moveRight(n: Int) = "${ANSI_CSI}${n}C"
    fun moveLeft(n: Int) = "${ANSI_CSI}${n}D"
}
