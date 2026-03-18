/**
 * TerminalBuffer.kt
 *
 * Core component for terminal state management.
 * [Ref: Clean Code, Ch.3: Functions] - Methods are kept small and focused on a single level of abstraction.
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

    private val history = ArrayDeque<TerminalLine>()
    private val screen = MutableList(height) { blankLine() }

    private var currentAttributes = TextAttributes()
    private val cursor = Cursor(0, 0)

    fun setAttributes(attributes: TextAttributes) {
        currentAttributes = attributes
    }

    fun getCurrentAttributes(): TextAttributes = currentAttributes

    fun getCursor(): Cursor = cursor.copy()

    fun setCursor(row: Int, col: Int) {
        cursor.row = row.coerceIn(0, height - 1)
        cursor.col = col.coerceIn(0, width - 1)
    }

    fun moveCursorUp(n: Int = 1) {
        cursor.row = (cursor.row - n).coerceAtLeast(0)
    }

    fun moveCursorDown(n: Int = 1) {
        cursor.row = (cursor.row + n).coerceAtMost(height - 1)
    }

    fun moveCursorLeft(n: Int = 1) {
        cursor.col = (cursor.col - n).coerceAtLeast(0)
    }

    fun moveCursorRight(n: Int = 1) {
        cursor.col = (cursor.col + n).coerceAtMost(width - 1)
    }

    fun writeText(text: String) {
        for (ch in text) {
            if (ch == '\n') {
                moveToNextLine()
                continue
            }

            screen[cursor.row].overwriteAt(cursor.col, Cell(ch, currentAttributes))
            advanceCursorAfterWrite()
        }
    }

    fun insertText(text: String) {
        for (ch in text) {
            if (ch == '\n') {
                moveToNextLine()
                continue
            }

            insertCharAt(cursor.row, cursor.col, Cell(ch, currentAttributes))
            advanceCursorAfterWrite()
        }
    }

    fun fillCurrentLine(char: Char? = null) {
        val fillCell = if (char == null) {
            Cell()
        } else {
            Cell(char, currentAttributes)
        }
        screen[cursor.row].fill(fillCell)
    }

    fun insertEmptyLineBottom() {
        pushTopScreenLineToHistory()
        screen.removeAt(0)
        screen.add(blankLine())
        cursor.row = (cursor.row - 1).coerceAtLeast(0)
    }

    fun clearScreen() {
        for (row in 0 until height) {
            screen[row] = blankLine()
        }
        setCursor(0, 0)
    }

    fun clearScreenAndScrollback() {
        history.clear()
        clearScreen()
    }

    fun getCharAt(globalRow: Int, col: Int): Char? {
        return getLineByGlobalRow(globalRow).getCell(col).char
    }

    fun getAttributesAt(globalRow: Int, col: Int): TextAttributes {
        return getLineByGlobalRow(globalRow).getCell(col).attributes
    }

    fun getLineAsString(globalRow: Int): String {
        return getLineByGlobalRow(globalRow).asPlainString()
    }

    fun getScreenLineAsString(screenRow: Int): String {
        require(screenRow in 0 until height) { "Screen row out of bounds: $screenRow" }
        return screen[screenRow].asPlainString()
    }

    fun getScreenAsString(): String {
        return screen.joinToString("\n") { it.asPlainString() }
    }

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

    fun totalLineCount(): Int = history.size + screen.size

    private fun blankLine(): TerminalLine = TerminalLine(width)

    private fun moveToNextLine() {
        cursor.col = 0
        if (cursor.row < height - 1) {
            cursor.row++
        } else {
            scrollUpOneLine()
        }
    }

    private fun advanceCursorAfterWrite() {
        if (cursor.col < width - 1) {
            cursor.col++
        } else {
            moveToNextLine()
        }
    }

    private fun scrollUpOneLine() {
        pushTopScreenLineToHistory()
        screen.removeAt(0)
        screen.add(blankLine())
        cursor.row = height - 1
        cursor.col = 0
    }

    private fun pushTopScreenLineToHistory() {
        val removed = screen.first()
        if (maxScrollback > 0) {
            history.addLast(copyLine(removed))
            while (history.size > maxScrollback) {
                history.removeFirst()
            }
        }
    }

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

    private fun copyLine(source: TerminalLine): TerminalLine {
        val copy = TerminalLine(source.width)
        for (i in 0 until source.width) {
            copy.setCell(i, source.getCell(i))
        }
        return copy
    }
}