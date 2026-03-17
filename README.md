# IntelliJ Terminal Text Buffer - Task 1

This repository contains the implementation of a terminal text buffer, including the core requirements and **Bonus features** (Wide characters support and Screen resizing).

## 🏗 Architecture & Data Structures Decisions

1. **Screen Grid (`Array<Array<TerminalCell>>`)**:
   I chose a 2D array to represent the active screen buffer. While a 1D flattened array can sometimes offer slightly better CPU cache locality, a 2D array in Kotlin maps perfectly to the logical `(x, y)` coordinate system of a terminal. It makes operations like cursor movement and line wrapping highly readable and intuitive.

2. **Scrollback History (`ArrayDeque<Array<TerminalCell>>`)**:
   **[Ref: Kotlin Standard Library Documentation]**
   The scrollback is implemented using an `ArrayDeque`. Since the scrollback acts as a Double-Ended Queue (we push new lines to the back, and pop the oldest lines from the front when `maxScrollback` is reached), `ArrayDeque` is the most optimal data structure. It operates in $O(1)$ time for both ends and avoids the memory overhead of node allocation found in a `LinkedList`.

3. **Immutability of Attributes (`CellAttributes`)**:
   **[Ref: Effective Java, Item 17: Minimize mutability]**
   `CellAttributes` is implemented as an immutable data class. This prevents unintended side effects when multiple `TerminalCell` instances share the same default attributes, ensuring thread safety and predictable state.

4. **Memory Management during Scrolling**:
   When the screen scrolls up (`insertEmptyLineAtBottom`), the array references are shifted instead of doing a cell-by-cell copy. However, when a line is pushed into the scrollback history, a deep `copy()` is explicitly created. This prevents reference mutation bugs if the original screen line is later overwritten.

## 🚀 Bonus Features Implemented

* **Wide Characters (CJK / Emoji)**: Handled using a `isWidePlaceholder` flag. The buffer correctly allocates two physical cells for wide characters and wraps them safely if they don't fit at the end of a line.
* **Screen Resizing**: Implemented a dynamic `resize(newWidth, newHeight)` method. If the height grows, it intelligently pulls lines back from the scrollback buffer (standard terminal emulator behavior).

## 💡 Trade-offs & Future Improvements

If I had more time for this experiment, I would explore the following improvements:

1. **Circular Buffer for the Screen**:
   Currently, scrolling the screen shifts array references, which takes $O(H)$ time. Implementing the screen as a Circular Buffer (where a pointer tracks the "top" row) would make scrolling strictly $O(1)$. The trade-off is a slightly more complex logical-to-physical coordinate mapping in the `getCellAt` function.
2. **Text Reflowing on Resize**:
   While the current resize logic handles pulling from scrollback, true text reflowing (re-wrapping long lines when the window shrinks and un-wrapping when it expands) is a highly complex feature that requires tracking "soft" vs "hard" line breaks.
3. **Static Factory Methods**:
   **[Ref: Effective Java, Item 1: Consider static factory methods instead of constructors]**
   Instead of a public constructor, I would introduce static factories like `TerminalBuffer.createStandard(80, 24)` to provide clear intention and flexibility for future configurations.