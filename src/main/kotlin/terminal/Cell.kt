package terminal
data class Cell(
    val char: Char? = null,
    val attributes: TextAttributes = TextAttributes()
) {
    fun displayChar(): Char = char ?: ' '
}