## Overview

This project implements a simplified terminal buffer model in Kotlin, including screen rendering, scrollback history, cursor management, and text editing operations.

The implementation focuses on correct behavior, predictable state transitions, and testability, rather than UI or ANSI parsing.

---

## Design Goals

* Represent terminal behavior in a deterministic and testable way
* Keep the model simple and explicit
* Avoid unnecessary abstractions while preserving extensibility
* Operate strictly through public API (no hidden side effects)

---

## Core Model

The buffer is modeled using three main layers:

### 1. Cell

Represents a single position in the terminal:

* character (Char?)
* styling (TextAttributes)

### 2. TerminalLine

Represents a fixed-width line:

* stores Cell[]
* supports overwrite and insert operations

### 3. TerminalBuffer

Top-level abstraction:

* manages visible screen and scrollback history
* maintains cursor state
* applies editing operations

---

## Screen and Scrollback Model

The buffer is split into:

* screen — visible lines (size = height)
* history — scrollback (bounded by maxScrollback)

### Rationale

Instead of storing everything in a single list with offsets, the model separates:

* mutable screen state
* append-only history

This simplifies:

* scrolling logic
* boundary conditions
* reasoning about visible vs historical content

---

## Cursor Semantics

* Cursor is always constrained to the visible screen
* All movements are clamped to valid bounds
* Cursor position is updated deterministically after each operation

---

## Editing Operations

### writeText

* Overwrites existing content
* Moves cursor forward
* Wraps at line end
* Scrolls when reaching bottom

### insertText

* Inserts characters at cursor
* Shifts content to the right
* Propagates overflow across lines
* May trigger scrolling

### fillCurrentLine

* Replaces entire line with a single character
* Applies current attributes

---

## State Transitions

All operations:

* mutate only the buffer state
* update cursor explicitly
* do not depend on external state

This ensures:

* reproducibility
* ease of testing
* predictable behavior

---

## Data Access

The buffer exposes read-only access via:

* getCharAt
* getAttributesAt
* getLineAsString
* getScreenAsString
* getAllContentAsString
* totalLineCount

All access is index-based and validated.

---

## Testing Strategy

Tests are written using kotlin.test and validate:

* public API behavior only
* cursor positioning
* wrapping and scrolling
* insert vs overwrite semantics
* attribute propagation
* edge cases and boundary conditions

No internal implementation details are tested.

---

## Design Trade-offs

### Single Responsibility

TerminalBuffer aggregates multiple concerns:

* screen management
* scrollback
* editing logic

This is intentional to:

* reduce complexity
* keep the model cohesive for the assignment

Further decomposition is possible if the system grows.

---

### Performance

* Insert operations are O(n)
* Line storage is array-based

This favors:

* simplicity
* correctness

over micro-optimizations.

---

### Extensibility

The model can be extended with:

* wide character support
* screen resizing
* ANSI escape handling
* alternative editing strategies

---

## Limitations

* No support for wide characters (e.g. emojis, CJK)
* No dynamic resizing
* No ANSI escape sequence parsing
* No concurrency support

---

## Running Tests

gradlew test
---


## Repository Structure

```text
IntelliJ_Terminal/
├── src/
│   ├── main/
│   │   └── kotlin/
│   │       └── terminal/
│   │           ├── Cell.kt
│   │           ├── Cursor.kt
│   │           ├── TerminalBuffer.kt
│   │           ├── TerminalColor.kt
│   │           ├── TerminalLine.kt
│   │           └── TextAttributes.kt
│   └── test/
│       └── kotlin/
│           └── terminal/
│               └── TerminalBufferTest.kt

```
---

## Conclusion

The implementation provides a clear and testable model of a terminal buffer, focusing on correctness and simplicity while maintaining a structure that can be extended if needed.
