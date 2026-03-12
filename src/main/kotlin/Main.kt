package com.example

import java.awt.Color


fun main() {
    val isDevelopment =
        System.getenv("TERMINAL_TEXT_BUFFER_ENVIRONMENT") == "development"

    val width =
        if (isDevelopment) DEFAULT_WIDTH else NumberPrompt.collectUserInput("width")
    val height =
        if (isDevelopment) DEFAULT_HEIGHT else NumberPrompt.collectUserInput("height")
    val scrollbackMaxSize =
        if (isDevelopment) DEFAULT_SCROLLBACK_MAX_SIZE
        else NumberPrompt.collectUserInput("scrollback maximum size")
    val foregroundColor =
        if (isDevelopment) DEFAULT_FOREGROUND_COLOR
        else AnsiColorPrompt.collectUserInput(
            "Choose foreground (text) color: "
        )
    val backgroundColor =
        if (isDevelopment) DEFAULT_BACKGROUND_COLOR
        else AnsiColorPrompt.collectUserInput(
            "Choose background color: "
        )
    val style =
        if (isDevelopment) DEFAULT_STYLE
        else AnsiEffectPrompt.collectUserInput(
            "Choose text style: "
        )

    val terminalBuffer = TerminalBuffer(scrollbackMaxSize)
        .setup(width, height, foregroundColor, backgroundColor, style)
0
    println("TERMINAL TEXT BUFFER")

    terminalBuffer.grid.layout.forEach { println(it.joinToString("")) }

    while (true) {
    }
}
