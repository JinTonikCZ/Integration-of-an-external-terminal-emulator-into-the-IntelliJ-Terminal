/**
 * TerminalLine.kt
 *
 * This file defines a fixed-width terminal row.
 * A terminal line manages cell-level operations inside a single screen row.
 */
package terminal

/**
 * Represents one fixed-width line of the terminal.
 *
 * Internally, the line stores a mutable list of cells and exposes
 * helper operations for reading, writing, inserting, filling, and clearing.
 */

class TerminalLine(
    val width: Int
) {
    /**
     * Internal storage for the cells of this line.
     *
     * The list is initialized with empty cells so that the line
     * always has exactly [width] positions.
     */
    private val cells: MutableList<Cell> = MutableList(width) { Cell() }

    /**
     * Returns the cell at the given column.
     *
     * @throws IllegalArgumentException if the column is outside valid bounds
     */

    fun getCell(col: Int): Cell {
        require(col in 0 until width) { "Column out of bounds: $col" }
        return cells[col]
    }
    /**
     * Replaces the cell at the given column.
     *
     * @throws IllegalArgumentException if the column is outside valid bounds
     */
    fun setCell(col: Int, cell: Cell) {
        require(col in 0 until width) { "Column out of bounds: $col" }
        cells[col] = cell
    }
    /**
     * Overwrites the cell at the given column.
     *
     * This method is semantically used for regular terminal writes,
     * where existing content is replaced rather than shifted.
     */
    fun overwriteAt(col: Int, cell: Cell) {
        setCell(col, cell)
    }
    /**
     * Inserts a cell at the given column and shifts the rest of the line right.
     *
     * The rightmost cell may overflow out of the line.
     *
     * @return the overflow cell if a meaningful cell was pushed out,
     *         or null if the overflowed cell was effectively empty
     *
     * @throws IllegalArgumentException if the column is outside valid bounds
     */

    fun insertAt(col: Int, cell: Cell): Cell? {
        require(col in 0 until width) { "Column out of bounds: $col" }

        val overflow = cells[width - 1]
        for (i in width - 1 downTo col + 1) {
            cells[i] = cells[i - 1]
        }
        cells[col] = cell

        return if (overflow.char != null || overflow.attributes != TextAttributes()) {
            overflow
        } else {
            null
        }
    }

    /**
     * Fills the entire line with the same cell value.
     *
     * This is useful for line reset operations or bulk redraw behavior.
     */

    fun fill(cell: Cell) {
        for (i in 0 until width) {
            cells[i] = cell
        }
    }
    /**
     * Clears the line by filling it with empty cells.
     */

    fun clear() {
        fill(Cell())
    }

    /**
     * Converts the line into a plain visible string.
     *
     * Each cell contributes its display character, so empty cells
     * become spaces and the line keeps its fixed width.
     */

    fun asPlainString(): String {
        return buildString(width) {
            for (cell in cells) append(cell.displayChar())
        }
    }
}