/**
 * Cursor.kt
 *
 * This file defines the terminal cursor model.
 * The cursor stores the current writable position in row/column form.
 */
package terminal
/**
 * Represents the current cursor position inside the visible screen.
 *
 * The cursor is mutable because the terminal buffer continuously updates it
 * during write, insert, clear, and scrolling operations.
 */
data class Cursor(
    var row: Int = 0,
    var col: Int = 0
)