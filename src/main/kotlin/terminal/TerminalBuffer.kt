/**
 * TerminalBuffer.kt
 *
 * This file defines the main terminal buffer controller.
 * The buffer coordinates cursor movement, visible screen content,
 * scrollback history, text insertion, overwriting, and clearing operations.
 */
package terminal

import java.util.ArrayDeque

class TerminalBuffer(
    val width: Int,
    val height: Int,
    val maxScrollback: Int
) {
    init {
        require(width > 0) { "Width must be > 0" }
        require(height > 0) { "Height must be > 0" }
        require(maxScrollback >= 0) { "maxScrollback must be >= 0" }
    }
    /**
     * Stores lines that have scrolled out of the visible screen.
     */
    private val history = ArrayDeque<TerminalLine>()

    /**
     * Stores the currently visible terminal screen.
     *
     * The screen always contains exactly [height] lines.
     */

    private val screen = MutableList(height) { blankLine() }
    /**
     * Stores the attributes that will be applied to newly written text.
     */
    private var currentAttributes = TextAttributes()
    /**
     * Stores the current cursor position.
     */
    private val cursor = Cursor(0, 0)
    /**
     * Updates the attributes used for future write/insert operations.
     */
    private var wrapPending = false

    fun setAttributes(attributes: TextAttributes) {
        currentAttributes = attributes
    }
    /**
     * Returns the attributes currently used for future writes.
     */
    fun getCurrentAttributes(): TextAttributes = currentAttributes
    /**
     * Returns a defensive copy of the current cursor state.
     *
     * Returning a copy prevents callers from mutating the internal cursor directly.
     */
    fun getCursor(): Cursor = cursor.copy()
    /**
     * Sets the cursor position inside valid screen bounds.
     *
     * Values outside the visible screen are clamped.
     */
    fun setCursor(row: Int, col: Int) {
        cursor.row = row.coerceIn(0, height - 1)
        cursor.col = col.coerceIn(0, width - 1)
        wrapPending = false

    }
    /**
     * Moves the cursor up by [n] rows without leaving the visible screen.
     */
    fun moveCursorUp(n: Int = 1) {
        cursor.row = (cursor.row - n).coerceAtLeast(0)
    }
    /**
     * Moves the cursor down by [n] rows without leaving the visible screen.
     */
    fun moveCursorDown(n: Int = 1) {
        cursor.row = (cursor.row + n).coerceAtMost(height - 1)
    }
    /**
     * Moves the cursor left by [n] columns without leaving the visible screen.
     */
    fun moveCursorLeft(n: Int = 1) {
        cursor.col = (cursor.col - n).coerceAtLeast(0)
    }
    /**
     * Moves the cursor right by [n] columns without leaving the visible screen.
     */
    fun moveCursorRight(n: Int = 1) {
        cursor.col = (cursor.col + n).coerceAtMost(width - 1)
    }
    /**
     * Writes text in overwrite mode starting at the current cursor position.
     *
     * Characters replace existing cells. A newline triggers a move to the next line.
     */
    fun writeText(text: String) {
        for (ch in text) {
            if (ch == '\n') {
                moveToNextLine()
                continue
            }
            if (wrapPending) {
                moveToNextLine()
                wrapPending = false
            }

            screen[cursor.row].overwriteAt(cursor.col, Cell(ch, currentAttributes))
            advanceCursorAfterWrite()
        }
    }
    /**
     * Inserts text at the current cursor position.
     *
     * Existing content on the line is shifted right. Overflow may continue
     * to following lines and can trigger screen scrolling.
     */
    fun insertText(text: String) {
        for (ch in text) {
            if (ch == '\n') {
                moveToNextLine()
                continue
            }
            if (wrapPending) {
                moveToNextLine()
                wrapPending = false
            }

            insertCharAt(cursor.row, cursor.col, Cell(ch, currentAttributes))
            advanceCursorAfterWrite()
        }
    }
    /**
     * Fills the current visible line with either blank cells or a repeated character.
     *
     * If [char] is null, the line becomes empty.
     * Otherwise, all cells on the line receive the same character and current attributes.
     */
    fun fillCurrentLine(char: Char? = null) {
        val fillCell = if (char == null) {
            Cell()
        } else {
            Cell(char, currentAttributes)
        }
        screen[cursor.row].fill(fillCell)
    }
    /**
     * Inserts a blank line at the bottom of the visible screen.
     *
     * The top visible line is moved to scrollback history,
     * the screen shifts upward, and the cursor row is adjusted accordingly.
     */
    fun insertEmptyLineBottom() {
        pushTopScreenLineToHistory()
        screen.removeAt(0)
        screen.add(blankLine())
        cursor.row = (cursor.row - 1).coerceAtLeast(0)
    }
    /**
     * Clears only the visible screen and resets the cursor.
     *
     * Scrollback history is preserved.
     */
    fun clearScreen() {
        for (row in 0 until height) {
            screen[row] = blankLine()
        }
        setCursor(0, 0)
        wrapPending = false
    }

    /**
     * Clears both the visible screen and the scrollback history.
     */
    fun clearScreenAndScrollback() {
        history.clear()
        clearScreen()
        wrapPending = false
    }
    /**
     * Returns the character stored at a global line/column position.
     *
     * Global rows include both history and visible screen lines.
     */
    fun getCharAt(globalRow: Int, col: Int): Char? {
        return getLineByGlobalRow(globalRow).getCell(col).char
    }
    /**
     * Returns the attributes stored at a global line/column position.
     *
     * Global rows include both history and visible screen lines.
     */
    fun getAttributesAt(globalRow: Int, col: Int): TextAttributes {
        return getLineByGlobalRow(globalRow).getCell(col).attributes
    }
    /**
     * Returns one global line as a plain visible string.
     *
     * Global rows include history first, then visible screen rows.
     */
    fun getLineAsString(globalRow: Int): String {
        return getLineByGlobalRow(globalRow).asPlainString()
    }
    /**
     * Returns one visible screen line as a plain string.
     */
    fun getScreenLineAsString(screenRow: Int): String {
        require(screenRow in 0 until height) { "Screen row out of bounds: $screenRow" }
        return screen[screenRow].asPlainString()
    }
    /**
     * Returns the full visible screen as a multiline string.
     */
    fun getScreenAsString(): String {
        return screen.joinToString("\n") { it.asPlainString() }
    }
    /**
     * Returns the complete terminal content as a multiline string.
     *
     * The returned content is ordered as:
     * - scrollback history
     * - visible screen
     */
    fun getAllContentAsString(): String {
        val allLines = mutableListOf<String>()
        for (line in history) {
            allLines += line.asPlainString()
        }
        for (line in screen) {
            allLines += line.asPlainString()
        }
        return allLines.joinToString("\n")
    }
    /**
     * Returns the total number of lines currently stored by the terminal,
     * including both scrollback history and visible screen rows.
     */
    fun totalLineCount(): Int = history.size + screen.size
    /**
     * Creates a new empty line with the current terminal width.
     */
    private fun blankLine(): TerminalLine = TerminalLine(width)
    /**
     * Moves the cursor to the next line.
     *
     * If the cursor is already on the bottom row, the screen scrolls up.
     */
    private fun moveToNextLine() {
        cursor.col = 0
        if (cursor.row < height - 1) {
            cursor.row++
        } else {
            scrollUpOneLine()
        }
        wrapPending = false
    }
    /**
     * Advances the cursor after writing one character.
     *
     * If the current row is full, the cursor moves to the next line.
     */
    private fun advanceCursorAfterWrite() {
        if (cursor.col < width - 1) {
            cursor.col++
        } else {
            wrapPending = true
        }
    }
    /**
     * Scrolls the visible screen upward by one line.
     *
     * The top line is moved into history, all visible rows shift up,
     * and a new empty line appears at the bottom.
     */
    private fun scrollUpOneLine() {
        pushTopScreenLineToHistory()
        screen.removeAt(0)
        screen.add(blankLine())
        cursor.row = height - 1
        cursor.col = 0
    }
    /**
     * Moves the current top visible line into scrollback history.
     *
     * The history size is trimmed so that it never exceeds [maxScrollback].
     */

    private fun pushTopScreenLineToHistory() {
        val removed = screen.first()
        if (maxScrollback > 0) {
            history.addLast(copyLine(removed))
            while (history.size > maxScrollback) {
                history.removeFirst()
            }
        }
    }
    /**
     * Inserts one character cell into the screen, shifting content right.
     *
     * If the insertion overflows, the overflow cell is propagated to following lines.
     * On the bottom row, overflow causes the screen to scroll upward.
     */
    private fun insertCharAt(row: Int, col: Int, cell: Cell) {
        var currentRow = row
        var currentCol = col
        var carry: Cell? = cell

        while (carry != null) {
            val line = screen[currentRow]
            val overflow = line.insertAt(currentCol, carry)
            carry = overflow

            if (carry != null) {
                if (currentRow == height - 1) {
                    scrollUpOneLine()
                    currentRow = height - 1
                    currentCol = 0
                } else {
                    currentRow++
                    currentCol = 0
                }
            }
        }
    }
    /**
     * Resolves a global row index into a concrete line object.
     *
     * Global indexing order:
     * 1. history lines
     * 2. visible screen lines
     */
    private fun getLineByGlobalRow(globalRow: Int): TerminalLine {
        require(globalRow in 0 until totalLineCount()) {
            "Global row out of bounds: $globalRow"
        }

        return if (globalRow < history.size) {
            history.elementAt(globalRow)
        } else {
            screen[globalRow - history.size]
        }
    }
    /**
     * Creates a shallow structural copy of a terminal line.
     *
     * The line object itself is copied so that history and screen do not share
     * the same line instance. Individual cells are reused as immutable values.
     */
    private fun copyLine(source: TerminalLine): TerminalLine {
        val copy = TerminalLine(source.width)
        for (i in 0 until source.width) {
            copy.setCell(i, source.getCell(i))
        }
        return copy
    }
}