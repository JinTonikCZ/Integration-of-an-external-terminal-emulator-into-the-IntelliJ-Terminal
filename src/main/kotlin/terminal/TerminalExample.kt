package terminal

fun main() {
    println("🚀 Starting the Terminal Emulator Engine...\n")

    // Initialize a small terminal: 20 columns, 4 rows, max 50 scrollback lines
    val buffer = TerminalBuffer(width = 20, height = 4, maxScrollback = 50)

    // Set attributes for the upcoming text
    buffer.setAttributes(CellAttributes(
        foreground = TerminalColor.GREEN,
        background = TerminalColor.BLACK,
        isBold = true
    ))

    // Write a long text to test line wrapping mechanics
    buffer.writeText("Hello, JetBrains! This text is too long for 20 columns.")

    // Test wide characters (Emoji/CJK support bonus)
    buffer.writeText(" 🐉")

    println("=== SCREEN (Before scrolling) ===")
    println(buffer.getScreenAsString())
    println("=================================\n")

    // Force scrollback by inserting empty lines at the bottom
    for (i in 1..3) {
        buffer.insertEmptyLineAtBottom()
    }

    buffer.setCursorPosition(0, 3)
    buffer.writeText("Bottom line now!")

    println("=== SCREEN (After 3 line breaks) ===")
    println(buffer.getScreenAsString())
    println("====================================\n")

    println("=== ENTIRE BUFFER CONTENT (Scrollback + Screen) ===")
    println(buffer.getEntireContentAsString())
    println("===================================================")
}