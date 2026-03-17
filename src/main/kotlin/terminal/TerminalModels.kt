/**
 * TerminalModels.kt
 * * [Ref: Effective Java, Item 17: Minimize mutability]
 * CellAttributes is implemented as an immutable data class to prevent unintended side effects
 * when sharing attributes between multiple cells.
 */
package terminal

enum class TerminalColor {
    DEFAULT,
    BLACK, RED, GREEN, YELLOW, BLUE, MAGENTA, CYAN, WHITE,
    BRIGHT_BLACK, BRIGHT_RED, BRIGHT_GREEN, BRIGHT_YELLOW,
    BRIGHT_BLUE, BRIGHT_MAGENTA, BRIGHT_CYAN, BRIGHT_WHITE
}

data class CellAttributes(
    val foreground: TerminalColor = TerminalColor.DEFAULT,
    val background: TerminalColor = TerminalColor.DEFAULT,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false
)

/**
 * [Ref: Clean Code, Ch.6: Objects and Data Structures]
 * Exposes behavior (copy) rather than just data, maintaining state integrity for wide characters.
 */
class TerminalCell(
    var char: Char = ' ',
    var attributes: CellAttributes = CellAttributes(),
    var isWide: Boolean = false,           // True if this cell contains a 2-column character
    var isWidePlaceholder: Boolean = false // True if this cell is the dummy 2nd half of a wide char
) {
    fun copy(): TerminalCell {
        return TerminalCell(this.char, this.attributes.copy(), this.isWide, this.isWidePlaceholder)
    }
}