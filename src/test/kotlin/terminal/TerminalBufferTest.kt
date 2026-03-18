package terminal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TerminalBufferTest {

    @Test
    fun `writeText overwrites characters and moves cursor`() {
        val buffer = TerminalBuffer(width = 5, height = 3, maxScrollback = 10)

        buffer.writeText("abc")

        assertEquals("abc  ", buffer.getScreenLineAsString(0))
        assertEquals(Cursor(0, 3), buffer.getCursor())
    }

    @Test
    fun `writeText wraps to next line`() {
        val buffer = TerminalBuffer(width = 5, height = 3, maxScrollback = 10)

        buffer.writeText("abcdef")

        assertEquals("abcde", buffer.getScreenLineAsString(0))
        assertEquals("f    ", buffer.getScreenLineAsString(1))
        assertEquals(Cursor(1, 1), buffer.getCursor())
    }

    @Test
    fun `insertText shifts existing content to the right`() {
        val buffer = TerminalBuffer(width = 5, height = 3, maxScrollback = 10)

        buffer.writeText("abcd")
        buffer.setCursor(0, 1)
        buffer.insertText("X")

        assertEquals("aXbcd", buffer.getScreenLineAsString(0))
        assertEquals(Cursor(0, 2), buffer.getCursor())
    }

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

    @Test
    fun `clearScreenAndScrollback clears everything`() {
        val buffer = TerminalBuffer(width = 4, height = 2, maxScrollback = 10)

        buffer.writeText("abcdefghijk")
        buffer.clearScreenAndScrollback()

        assertEquals(2, buffer.totalLineCount())
        assertEquals("    \n    ", buffer.getAllContentAsString())
    }

    @Test
    fun `scrollback keeps old lines when screen scrolls`() {
        val buffer = TerminalBuffer(width = 3, height = 2, maxScrollback = 10)

        buffer.writeText("abcdefghi")

        assertEquals(3, buffer.totalLineCount())
        assertEquals("abc", buffer.getLineAsString(0))
        assertEquals("def", buffer.getLineAsString(1))
        assertEquals("ghi", buffer.getLineAsString(2))
    }

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

    @Test
    fun `empty cell returns null char`() {
        val buffer = TerminalBuffer(width = 3, height = 2, maxScrollback = 10)

        assertNull(buffer.getCharAt(0, 0))
    }
}