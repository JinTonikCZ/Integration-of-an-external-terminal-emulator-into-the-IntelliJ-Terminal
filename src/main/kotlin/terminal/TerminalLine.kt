package terminal

class TerminalLine(
    val width: Int
) {
    private val cells: MutableList<Cell> = MutableList(width) { Cell() }

    fun getCell(col: Int): Cell {
        require(col in 0 until width) { "Column out of bounds: $col" }
        return cells[col]
    }

    fun setCell(col: Int, cell: Cell) {
        require(col in 0 until width) { "Column out of bounds: $col" }
        cells[col] = cell
    }

    fun overwriteAt(col: Int, cell: Cell) {
        setCell(col, cell)
    }

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

    fun fill(cell: Cell) {
        for (i in 0 until width) {
            cells[i] = cell
        }
    }

    fun clear() {
        fill(Cell())
    }

    fun asPlainString(): String {
        return buildString(width) {
            for (cell in cells) append(cell.displayChar())
        }
    }
}