package com.example


// https://jakob-bagterp.github.io/colorist-for-python/ansi-escape-codes/standard-16-colors/

const val ESC = "\u001B"
const val ANSI_CSI = "\u001B["

enum class AnsiColor(val code: Int) {
    BLACK(0),
    RED(1),
    GREEN(2),
    YELLOW(3),
    BLUE(4),
    MAGENTA(5),
    CYAN(6),
    WHITE(7),
    RESET(0),
}


enum class AnsiPlacementIntensity(val code: Int) {
    STANDARD_FOREGROUND(3),
    STANDARD_BACKGROUND(4),
    BRIGHT_FOREGROUND(9),
    BRIGHT_BACKGROUND(10),
}


enum class AnsiEffect(val on: Int?, val off: Int?) {
    NONE(null, null),
    BOLD(1, 21),
    DIM(2, 22),
    UNDERLINE(4, 24),
    BLINK(5, 25),
    REVERSE(7, 27),
    HIDE(8, 28),
}
