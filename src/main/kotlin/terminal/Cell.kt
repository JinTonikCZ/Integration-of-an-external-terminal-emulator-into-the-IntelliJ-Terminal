/**
 * Cell.kt
 *
 * This file defines the smallest visible unit of the terminal buffer.
 * A cell stores both the character content and its text attributes.
 */

package terminal

/**
 * Represents a single terminal cell.
 *
 * Each cell stores:
 * - an optional character
 * - the visual attributes associated with that character
 *
 * A null character means that the cell is logically empty.
 */
data class Cell(
    val char: Char? = null,
    val attributes: TextAttributes = TextAttributes()
) {
    /**
     * Returns the visible representation of the cell.
     *
     * If the cell has no character, a blank space is returned so that
     * rendering methods can still produce a fixed-width line string.
     */
    fun displayChar(): Char = char ?: ' '
}