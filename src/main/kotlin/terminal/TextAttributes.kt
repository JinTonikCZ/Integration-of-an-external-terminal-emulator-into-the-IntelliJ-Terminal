package terminal

data class TextAttributes(
    val foreground: TerminalColor? = null,
    val background: TerminalColor? = null,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false
)