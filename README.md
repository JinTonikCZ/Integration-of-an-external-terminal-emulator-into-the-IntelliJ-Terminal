# IntelliJ Terminal Text Buffer — Task 1

This project implements a simplified terminal text buffer for the IntelliJ Terminal task.  
The solution models the terminal as a layered module with clear responsibility separation between cursor control, screen lines, individual cells, text attributes, and the main buffer logic.

---

## Overview

The implementation is designed as a small terminal engine that manages:

- cursor position
- visible screen content
- scrollback history
- text insertion and overwrite operations
- line shifting
- screen clearing
- per-cell text attributes

The architecture follows a layered and modular approach, where each class has a clearly defined role.

---

## Architecture

The solution is implemented as a layered model:

### 1. Data layer
This layer stores the smallest terminal entities.

- **Cell** — represents one terminal cell
- **TextAttributes** — stores visual formatting of a cell
- **TerminalColor** — defines available terminal colors
- **Cursor** — stores current cursor position

### 2. Line layer
- **TerminalLine** — represents one line of the terminal screen and provides operations over cells inside that line

### 3. Buffer layer
- **TerminalBuffer** — the main class that coordinates screen state, cursor movement, scrollback, writing, insertion, and clearing operations

This separation makes the implementation easier to understand, test, and extend.

---

## Main design idea

The terminal is not treated as one flat string.  
Instead, it is modeled as:

- a list of visible screen lines
- a history of lines that have already scrolled out of view
- a cursor that points to the current writable position
- cells that combine both text content and formatting

This approach is closer to how a real terminal works.

---

# Classes and Responsibilities

## `Cell`

`Cell` represents the smallest visible unit of the terminal.

A cell stores:

- the displayed character
- text attributes associated with that character

This means that content and formatting are connected directly inside the same object.

For example, a cell may contain:

- character: `'A'`
- foreground color: green
- background color: black
- bold: true
- italic: false
- underline: false

This design makes it possible to retrieve both the symbol and its appearance from the same position in the buffer.

### Responsibility
- store one character and its formatting
- provide the visible character representation for rendering

---

## `TextAttributes`

`TextAttributes` stores the visual style of text for each cell.

It contains:

- `foreground` — foreground text color
- `background` — background color
- `bold` — bold flag
- `italic` — italic flag
- `underline` — underline flag

### Why this class is needed
Terminal content is not only about characters.  
A terminal also needs to preserve formatting.  
That is why text attributes are separated into their own data structure and attached to each `Cell`.

### Responsibility
- store all formatting metadata for a character
- allow the buffer to write styled text
- preserve appearance information per cell

---

## `TerminalColor`

`TerminalColor` defines the available terminal colors.

It acts as a fixed set of supported color values used by `TextAttributes`.

### Responsibility
- provide a controlled set of terminal colors
- avoid raw string-based color handling
- improve type safety and readability

---

## `Cursor`

`Cursor` stores the current write position in the terminal.

It contains:

- `row`
- `col`

The cursor is used by the terminal buffer to know where the next write or insert operation should happen.

### Responsibility
- represent the active writing position
- support movement through the buffer
- keep coordinate state separate from buffer logic

---

## `TerminalLine`

`TerminalLine` represents one horizontal line of the screen.

Internally, it stores a fixed-width sequence of `Cell` objects.

### Supported responsibilities
- return a cell at a given column
- replace a cell
- overwrite a cell
- insert a cell and shift the rest of the line
- fill the whole line with the same cell
- convert the line into plain text

### Why this class matters
Instead of letting `TerminalBuffer` manipulate raw arrays directly everywhere, line-level operations are encapsulated inside `TerminalLine`.

This keeps buffer code cleaner and makes the logic easier to test.

---

## `TerminalBuffer`

`TerminalBuffer` is the central class of the whole implementation.

It manages:

- visible screen lines
- scrollback history
- cursor position
- current text attributes
- writing and insertion behavior
- clearing logic
- line scrolling
- access to characters and attributes

### Responsibility
This class acts as the terminal controller.  
It coordinates all operations and is responsible for maintaining consistent terminal state.

In other words:

- `TerminalBuffer` manages the cursor
- `TerminalBuffer` manages screen content
- `TerminalBuffer` manages scrollback
- `TerminalBuffer` performs all main terminal operations

---

# Internal Model

The terminal buffer is divided into two logical parts:

## 1. Visible screen
The visible screen contains exactly `height` lines.  
These lines represent what is currently shown in the terminal window.

## 2. Scrollback history
When the screen scrolls upward, lines leaving the visible area are moved into history.  
This allows the system to preserve previous terminal output.

### Benefit
This model makes it possible to distinguish between:

- what is currently visible
- what was previously shown and scrolled away

---

# Core Operations

## Writing text

The buffer supports writing text at the current cursor position.

During writing:

- characters are placed into cells
- current text attributes are attached to the written cells
- the cursor advances
- when needed, writing continues to the next line
- if the screen is full, old lines are moved into scrollback history

### Important detail
Text content and attributes are written together.  
This ensures that every character keeps its own style.

---

## Inserting text

Insertion differs from overwrite mode.

When text is inserted:

- the new character is placed at the current cursor position
- existing content on that line is shifted to the right
- if the line overflows, the overflow may propagate further depending on the implementation logic

### Purpose
This simulates terminal-like insertion behavior rather than simple replacement.

---

## Cursor movement

The cursor determines where writing happens.

The buffer supports explicit cursor positioning and implicit movement during write operations.

### Cursor behavior includes
- row and column tracking
- moving to the next line
- resetting when needed
- staying within valid terminal bounds

---

## Screen clearing

The implementation provides screen clearing operations.

### `clearScreen()`
This clears only the visible screen.

Effect:
- visible lines become blank
- cursor is reset
- scrollback history remains preserved

### `clearScreenAndScrollback()`
This clears the visible screen and also removes scrollback history.

Effect:
- visible screen becomes blank
- history is erased
- cursor is reset

### Why two methods are useful
A terminal often needs both behaviors:

- clear what is currently visible
- or fully reset the terminal state including old output

---

## Filling the current line

The buffer can fill the active line with a chosen symbol or blank content.

This is useful for:
- line reset behavior
- terminal redraw scenarios
- replacing current line content

The fill operation also uses the current text attributes when appropriate.

---

## Scrolling

When output reaches the bottom of the visible screen, the terminal scrolls upward.

This means:
- the top visible line is moved to scrollback history
- all remaining visible lines shift upward
- a new blank line appears at the bottom

This matches the expected terminal-style behavior.

---

# Relationship Between Content and Attributes

One of the core design decisions in this project is that terminal content is stored together with visual state.

This is achieved through the relationship:

- `TerminalBuffer` writes into
- `TerminalLine`, which stores
- `Cell`, and each `Cell` contains
- `TextAttributes`

So each screen position preserves both:

- **what character is shown**
- **how that character should be displayed**

### Why this matters
Without this connection, it would be impossible to correctly restore or inspect styled terminal output.

For example, if the user writes green bold text and later queries the same position, both the character and its style must still be available.

---

# Why the solution is modular

The implementation is intentionally split into separate classes instead of one large file.

### Advantages of this modular design

- better readability
- easier debugging
- easier testing
- clearer responsibilities
- simpler future extension

For example:

- formatting logic belongs to `TextAttributes`
- cell-level content belongs to `Cell`
- line-level operations belong to `TerminalLine`
- global terminal behavior belongs to `TerminalBuffer`

This makes the project more maintainable and closer to real software engineering practice.

---

# Testing

The project includes unit tests for the main terminal behavior.

The tests verify:

- text writing
- screen clearing
- scrollback preservation
- insertion behavior
- attribute storage per cell
- correct handling of empty cells

### Why tests are important
Because terminal buffers are stateful structures, even small logic changes can break scrolling, line shifting, or cursor movement.  
Unit tests help confirm that the implementation behaves consistently.

---

# Summary

This implementation provides a structured and modular terminal text buffer model.

The solution includes:

- a clear layered architecture
- per-cell content and formatting storage
- cursor-controlled write and insert operations
- visible screen and scrollback separation
- screen clearing logic
- terminal-style line scrolling
- unit test validation

Overall, the project is implemented as a modular layered solution where `TerminalBuffer` acts as the central controller, while lower-level classes encapsulate content, formatting, and line-level operations.
