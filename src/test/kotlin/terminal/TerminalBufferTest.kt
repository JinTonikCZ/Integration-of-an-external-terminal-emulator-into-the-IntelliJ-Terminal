/**
 * TerminalBufferTest.kt
 *
 * This file contains unit tests for the terminal buffer implementation.
 * The tests verify the core buffer behavior expected by the task.
 */
package terminal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for TerminalBuffer.
 *
 * The test suite validates:
 * - overwrite writes
 * - wrapping
 * - insertion
 * - line filling
 * - screen clearing
 * - scrollback behavior
 * - attribute persistence
 * - empty-cell behavior
 */

class TerminalBufferTest {
    /**
     * Verifies that regular writes overwrite content and move the cursor forward.
     */
    @Test
    fun `writeText overwrites characters and moves cursor`() {
        val buffer = TerminalBuffer(width = 5, height = 3, maxScrollback = 10)

        buffer.writeText("abc")

        assertEquals("abc  ", buffer.getScreenLineAsString(0))
        assertEquals(Cursor(0, 3), buffer.getCursor())
    }
    /**
     * Verifies that writing past the end of a line wraps onto the next line.
     */
    @Test
    fun `writeText wraps to next line`() {
        val buffer = TerminalBuffer(width = 5, height = 3, maxScrollback = 10)

        buffer.writeText("abcdef")

        assertEquals("abcde", buffer.getScreenLineAsString(0))
        assertEquals("f    ", buffer.getScreenLineAsString(1))
        assertEquals(Cursor(1, 1), buffer.getCursor())
    }
    /**
     * Verifies that insert mode shifts existing characters to the right.
     */
    @Test
    fun `insertText shifts existing content to the right`() {
        val buffer = TerminalBuffer(width = 5, height = 3, maxScrollback = 10)

        buffer.writeText("abcd")
        buffer.setCursor(0, 1)
        buffer.insertText("X")

        assertEquals("aXbcd", buffer.getScreenLineAsString(0))
        assertEquals(Cursor(0, 2), buffer.getCursor())
    }
    /**
     * Verifies that filling the current line uses the current text attributes.
     */
    @Test
    fun `fillCurrentLine fills visible line with symbol using current attributes`() {
        val buffer = TerminalBuffer(width = 5, height = 3, maxScrollback = 10)

        val attrs = TextAttributes(
            foreground = TerminalColor.GREEN,
            background = TerminalColor.BLACK,
            bold = true
        )

        buffer.setAttributes(attrs)
        buffer.setCursor(1, 2)
        buffer.fillCurrentLine('#')

        assertEquals("#####", buffer.getScreenLineAsString(1))
        assertEquals(attrs, buffer.getAttributesAt(1, 0))
    }
    /**
     * Verifies that clearing the screen affects only the visible screen,
     * while preserving global structure and resetting the cursor.
     */
    @Test
    fun `clearScreen clears only visible screen`() {
        val buffer = TerminalBuffer(width = 4, height = 2, maxScrollback = 10)

        buffer.writeText("abcdefghijk")

        val before = buffer.totalLineCount()
        assertTrue(before >= 2)

        buffer.clearScreen()

        assertEquals("    \n    ", buffer.getScreenAsString())
        assertEquals(Cursor(0, 0), buffer.getCursor())
    }
    /**
     * Verifies that the full reset operation clears both screen and scrollback.
     */
    @Test
    fun `clearScreenAndScrollback clears everything`() {
        val buffer = TerminalBuffer(width = 4, height = 2, maxScrollback = 10)

        buffer.writeText("abcdefghijk")
        buffer.clearScreenAndScrollback()

        assertEquals(2, buffer.totalLineCount())
        assertEquals("    \n    ", buffer.getAllContentAsString())
    }
    /**
     * Verifies that old lines are preserved when screen content scrolls upward.
     */
    @Test
    fun `scrollback keeps old lines when screen scrolls`() {
        val buffer = TerminalBuffer(width = 3, height = 2, maxScrollback = 10)

        buffer.writeText("abcdefghi")

        assertEquals(3, buffer.totalLineCount())
        assertEquals("abc", buffer.getLineAsString(0))
        assertEquals("def", buffer.getLineAsString(1))
        assertEquals("ghi", buffer.getLineAsString(2))
    }
    /**
     * Verifies that text attributes are stored together with character content.
     */
    @Test
    fun `attributes are stored per cell`() {
        val buffer = TerminalBuffer(width = 5, height = 2, maxScrollback = 10)

        val attrs = TextAttributes(
            foreground = TerminalColor.RED,
            background = TerminalColor.WHITE,
            italic = true,
            underline = true
        )

        buffer.setAttributes(attrs)
        buffer.writeText("A")

        assertEquals('A', buffer.getCharAt(0, 0))
        assertEquals(attrs, buffer.getAttributesAt(0, 0))
    }
    /**
     * Verifies that inserting a blank line at the bottom scrolls the visible screen upward.
     */
    @Test
    fun `insertEmptyLineBottom scrolls screen upward`() {
        val buffer = TerminalBuffer(width = 3, height = 2, maxScrollback = 10)

        buffer.writeText("abc")
        buffer.writeText("def")

        buffer.insertEmptyLineBottom()

        assertEquals("abc", buffer.getLineAsString(0))
        assertEquals("def", buffer.getLineAsString(1))
        assertEquals("   ", buffer.getLineAsString(2))
    }
    /**
     * Verifies that an untouched cell returns null as its logical character value.
     */
    @Test
    fun `empty cell returns null char`() {
        val buffer = TerminalBuffer(width = 3, height = 2, maxScrollback = 10)

        assertNull(buffer.getCharAt(0, 0))
    }
}