package com.example

class TerminalBuffer(
    val scrollbackMaxSize: Int,
) {
    lateinit var grid: Array<Array<Char>>

    fun setup(width: Int, height: Int): TerminalBuffer {
        grid = Array(height) { Array(width) { '|' } }
        return this
    }
}
