package com.example


object NumberPrompt {
    fun collectUserInput(item: String): Int {
        print("Set $item: ")
        var value = readln().toIntOrNull()
        while (value == null || value <= 0) {
            print(
                "${
                    item.replaceFirstChar { it.uppercase() }
                } must be positive number, try again: "
            )
            value = readln().toIntOrNull()
        }
        return value
    }
}


object AnsiColorPrompt {
     fun collectUserInput(msg: String): AnsiColor {
        println(msg)
        val entries = AnsiColor.entries.filter { it != AnsiColor.RESET }
        entries.forEachIndexed { idx, color ->
            println(
                "${idx+1}. ${
                    color.name.lowercase().replaceFirstChar { it.uppercase() }
                }"
            )
        }
         val maxNum = entries.size
         var value = readln().toIntOrNull()
         while (value == null || value !in 1..maxNum) {
             print("Choose number between 1 and $maxNum, try again: ")
             value = readln().toIntOrNull()
         }
         val ordinal = value - 1
         return entries[ordinal]
     }
}


object AnsiEffectPrompt {
    fun collectUserInput(msg: String): AnsiEffect {
        println(msg)
        val entries = AnsiEffect.entries
        entries.forEach { effect ->
            println(
                "${effect.ordinal + 1}. ${
                    effect.name.lowercase().replaceFirstChar { it.uppercase() }
                }"
            )
        }
        var value = readln().toIntOrNull()
        val maxNum = entries.size + 1
        while (value == null || value !in 1..maxNum) {
            print("Choose number between 1 and $maxNum, try again: ")
            value = readln().toIntOrNull()
        }
        val ordinal = value - 1
        return entries[ordinal]
    }
}
