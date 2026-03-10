package com.example


fun main() {
    val isDevelopment =
        System.getenv("TERMINAL_TEXT_BUFFER_ENVIRONMENT") == "development"

    var width: Int
    var height: Int
    var scrollbackMaxSize: Int

    if (isDevelopment) {
        width = 80
        height = 24
        scrollbackMaxSize = 100
    } else {
        print("Set width: ")
        var widthValue = readln().toIntOrNull()
        while (widthValue == null || widthValue <= 0) {
            print("Width must be positive number, set again: ")
            widthValue = readln().toIntOrNull()
        }
        width = widthValue

        print("Set height: ")
        var heightValue = readln().toIntOrNull()
        while (heightValue == null || heightValue <= 0) {
            print("Height must be positive number, set again: ")
            heightValue = readln().toIntOrNull()
        }
        height = heightValue

        print("Set scrollback maximum size: ")
        var scrollbackMaxSizeValue = readln().toIntOrNull()
        while (scrollbackMaxSizeValue == null || scrollbackMaxSizeValue <= 0) {
            print("Height must be positive number, set again: ")
            scrollbackMaxSizeValue = readln().toIntOrNull()
        }
        scrollbackMaxSize = scrollbackMaxSizeValue
    }

    val terminalBuffer = TerminalBuffer(scrollbackMaxSize)
        .setup(width, height)

    println("TERMINAL TEXT BUFFER")

    terminalBuffer.grid.forEach { println(it.joinToString("")) }

    while (true) {
    }
}
