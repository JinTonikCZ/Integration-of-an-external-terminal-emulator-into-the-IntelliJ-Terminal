/**
 * TextAttributes.kt
 *
 * This file defines the visual formatting metadata attached to each cell.
 * It stores color and style information independently from character content.
 */

package terminal

/**
 * Stores visual formatting for a terminal cell.
 *
 * The class supports:
 * - foreground color
 * - background color
 * - bold flag
 * - italic flag
 * - underline flag
 *
 * Nullable colors allow a cell to keep "no explicit color" as a valid state.
 */

data class TextAttributes(
    val foreground: TerminalColor? = null,
    val background: TerminalColor? = null,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false
)