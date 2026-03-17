/**
 * TerminalBuffer.kt
 *
 * Core component for terminal state management.
 * [Ref: Clean Code, Ch.3: Functions] - Methods are kept small and focused on a single level of abstraction.
 */
package terminal

class TerminalBuffer(
    var width: Int,
    var height: Int,
    private val maxScrollback: Int
) {
    // [Ref: Effective Java, Item 64: Refer to objects by their interfaces]
    // Using ArrayDeque interface for the scrollback implementation
    private val scrollback = ArrayDeque<Array<TerminalCell>>()

    private var screen: Array<Array<TerminalCell>> = Array(height) {
        Array(width) { TerminalCell() }
    }

    var cursorX: Int = 0
        private set
    var cursorY: Int = 0
        private set

    var currentAttributes: CellAttributes = CellAttributes()
        private set

    fun setAttributes(attributes: CellAttributes) {
        this.currentAttributes = attributes
    }

    fun setCursorPosition(x: Int, y: Int) {
        cursorX = x.coerceIn(0, width - 1)
        cursorY = y.coerceIn(0, height - 1)
    }

    fun moveCursor(dx: Int, dy: Int) {
        setCursorPosition(cursorX + dx, cursorY + dy)
    }

    /**
     * Helper to detect Wide Characters (e.g., CJK ideographs).
     * [Ref: Clean Code, Ch.2: Meaningful Names] - Intention-revealing name.
     */
    private fun isWideChar(ch: Char): Boolean {
        val block = Character.UnicodeBlock.of(ch)
        return block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS ||
                block == Character.UnicodeBlock.HIRAGANA ||
                block == Character.UnicodeBlock.KATAKANA ||
                block == Character.UnicodeBlock.HANGUL_SYLLABLES
    }

    /**
     * Writes text to the buffer, properly handling 2-cell wide characters.
     */
    fun writeText(text: String) {
        for (char in text) {
            val isWide = isWideChar(char)

            // If it's a wide character and we are at the very last column,
            // it doesn't fit. Wrap to the next line early.
            if (isWide && cursorX == width - 1) {
                wrapLine()
            }

            // Write the main character
            screen[cursorY][cursorX].apply {
                this.char = char
                this.attributes = currentAttributes
                this.isWide = isWide
                this.isWidePlaceholder = false
            }

            // Handle the second cell for wide characters
            if (isWide) {
                cursorX++
                screen[cursorY][cursorX].apply {
                    this.char = ' ' // Placeholder space
                    this.attributes = currentAttributes
                    this.isWide = false
                    this.isWidePlaceholder = true
                }
            }

            cursorX++
            if (cursorX >= width) {
                wrapLine()
            }
        }
    }

    private fun wrapLine() {
        cursorX = 0
        if (cursorY < height - 1) {
            cursorY++
        } else {
            insertEmptyLineAtBottom()
        }
    }

    /**
     * Shifts screen up. Top row goes to scrollback.
     */
    fun insertEmptyLineAtBottom() {
        val topLine = screen[0].map { it.copy() }.toTypedArray()

        if (maxScrollback > 0) {
            if (scrollback.size >= maxScrollback) {
                scrollback.removeFirst()
            }
            scrollback.addLast(topLine)
        }

        for (y in 0 until height - 1) {
            screen[y] = screen[y + 1] // Faster array reference swap instead of cell-by-cell copy
        }

        screen[height - 1] = Array(width) { TerminalCell() }
    }

    /**
     * BONUS FEATURE: Resizes the terminal grid.
     * Content truncation strategy: If shrinking, rightmost columns are truncated.
     * If height grows, we pull lines back from the scrollback history (standard terminal behavior).
     */
    fun resize(newWidth: Int, newHeight: Int) {
        require(newWidth > 0 && newHeight > 0) { "Dimensions must be positive" }

        val newScreen = Array(newHeight) { Array(newWidth) { TerminalCell() } }

        // Determine how many lines to pull from scrollback if height increased
        val heightDiff = newHeight - height
        var linesToPull = 0
        if (heightDiff > 0) {
            linesToPull = minOf(heightDiff, scrollback.size)
        }

        // 1. Pull lines from scrollback (if applicable)
        for (i in 0 until linesToPull) {
            val oldLine = scrollback.removeLast()
            copyLine(oldLine, newScreen[i], newWidth)
        }

        // 2. Copy existing screen content
        val rowsToCopy = minOf(height, newHeight - linesToPull)
        for (y in 0 until rowsToCopy) {
            val oldY = if (heightDiff < 0) y - heightDiff else y // Skip top lines if shrinking vertically
            val newY = y + linesToPull
            copyLine(screen[oldY], newScreen[newY], newWidth)
        }

        this.width = newWidth
        this.height = newHeight
        this.screen = newScreen

        // Ensure cursor is within new bounds
        setCursorPosition(cursorX, cursorY + linesToPull)
    }

    private fun copyLine(source: Array<TerminalCell>, target: Array<TerminalCell>, newWidth: Int) {
        val colsToCopy = minOf(source.size, newWidth)
        for (x in 0 until colsToCopy) {
            target[x] = source[x].copy()
        }
    }

    fun clearScreen() {
        screen = Array(height) { Array(width) { TerminalCell() } }
        setCursorPosition(0, 0)
    }

    fun clearAll() {
        clearScreen()
        scrollback.clear()
    }

    private fun getCellAt(x: Int, y: Int): TerminalCell {
        require(x in 0 until width) { "X coordinate out of bounds" }
        if (y in 0 until height) return screen[y][x]
        if (y < 0 && y >= -scrollback.size) return scrollback[scrollbackIndex(y)][x]
        throw IllegalArgumentException("Y coordinate out of bounds: $y")
    }

    private fun scrollbackIndex(y: Int) = scrollback.size + y

    fun getCharAt(x: Int, y: Int): Char = getCellAt(x, y).char

    fun getAttributesAt(x: Int, y: Int): CellAttributes = getCellAt(x, y).attributes

    fun getLineAsString(y: Int): String {
        val builder = java.lang.StringBuilder(width)
        for (x in 0 until width) {
            val cell = getCellAt(x, y)
            if (!cell.isWidePlaceholder) {
                builder.append(cell.char)
            }
        }
        return builder.toString()
    }

    fun getScreenAsString(): String = (0 until height).joinToString("\n") { getLineAsString(it) }
    /**
     * Fills the current line with a specific character using current attributes.
     * Does not move the cursor.
     */
    fun fillLine(char: Char) {
        for (x in 0 until width) {
            screen[cursorY][x].apply {
                this.char = char
                this.attributes = currentAttributes
                this.isWide = false
                this.isWidePlaceholder = false
            }
        }
    }

    /**
     * Inserts text at the current cursor position.
     * Shifts the existing text on the line to the right, possibly wrapping it.
     */
    fun insertText(text: String) {
        // 1. Save everything to the right of the cursor
        val remainder = StringBuilder()
        for (x in cursorX until width) {
            val cell = screen[cursorY][x]
            if (!cell.isWidePlaceholder && cell.char != ' ') {
                remainder.append(cell.char)
            }
        }

        // 2. Insert the new text (cursor will move automatically)
        writeText(text)

        // 3. Remember where the cursor ended up after insertion
        val endOfInsertX = cursorX
        val endOfInsertY = cursorY

        // 4. Append the "tail" of the old text so it shifts instead of disappearing
        if (remainder.isNotEmpty()) {
            writeText(remainder.toString())
        }

        // 5. Return the cursor to the end of the newly inserted text
        setCursorPosition(endOfInsertX, endOfInsertY)
    }

    /**
     * Returns the entire content of the terminal (Scrollback history + current screen).
     */
    fun getEntireContentAsString(): String {
        val builder = java.lang.StringBuilder()

        // First, append the scrollback history
        for (line in scrollback) {
            for (cell in line) {
                if (!cell.isWidePlaceholder) {
                    builder.append(cell.char)
                }
            }
            builder.append("\n")
        }

        // Then, append the current screen
        builder.append(getScreenAsString())
        return builder.toString()
    }
}
