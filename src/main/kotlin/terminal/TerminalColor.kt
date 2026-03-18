/**
 * TerminalColor.kt
 *
 * This file defines the fixed set of supported terminal colors.
 * These values are used by TextAttributes.
 */
package terminal

/**
 * Enumerates supported terminal colors.
 *
 * The enum provides a strongly typed alternative to raw string-based colors.
 */
enum class TerminalColor {
    BLACK,
    RED,
    GREEN,
    YELLOW,
    BLUE,
    MAGENTA,
    CYAN,
    WHITE,
    BRIGHT_BLACK,
    BRIGHT_RED,
    BRIGHT_GREEN,
    BRIGHT_YELLOW,
    BRIGHT_BLUE,
    BRIGHT_MAGENTA,
    BRIGHT_CYAN,
    BRIGHT_WHITE
}