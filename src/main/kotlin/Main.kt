package com.example


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

    val terminal = TerminalBuffer(scrollbackMaxSize)
        .setup(width, height, foregroundColor, backgroundColor, style)
    val screen = terminal.screen

    terminal.insert("I am in scrollback!")
    terminal.enterNewLine()
    terminal.insert("Cras vitae pharetra risus. Ut eget velit.")
    terminal.enterNewLine()
    terminal.insert("Fusce euismod non lacus et semper.")
    terminal.enterNewLine()
    terminal.insert("Quisque ut fringilla turpis. Curabitur in eros velit.")
    terminal.enterNewLine()

    screen.resetCursor()
    val overwriteWithText = (
        "I have been subsequently inserted. " +
        "My job is to show you wrapped functionality. Might as well continue blabbering.."
    )
    overwriteWithText.forEach { terminal.write(it.toString()) }

    screen.lines.forEachIndexed { idx, line ->
        val flattenedRows = line.flatten()
        if (idx == 1) {
            println("Line $idx  ")
            line.forEach { row ->
                println(row.joinToString("") { it.render() })
            }
        } else if (line in screen.getNonFrozenLines()) {
            println("Line $idx  ${flattenedRows.joinToString("") { it.render() } }")
        } else {
            println("Line $idx  ${flattenedRows.joinToString("") { 
                it.foregroundColor = AnsiColor.WHITE 
                it.backgroundColor = AnsiColor.BLACK 
                it.render() 
            } }")
        }
    }

    println("")

    println("------------------------------------")
    println("SCREEN CONTENT AS STRING")
    println(terminal.getScreenContentAsString())
    println("------------------------------------")

    println("")

    println("------------------------------------")
    println("ENTIRE CONTENT AS STRING")
    println(terminal.getEntireContentAsString())
    println("------------------------------------")

}
