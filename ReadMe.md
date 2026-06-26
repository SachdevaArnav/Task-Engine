# Task Engine — Execution Subsystem

Task Engine is a state-aware desktop automation system that executes structured, goal-conditioned workflows over real Windows environments.

This repository implements the execution subsystem of the broader Task Engine architecture — a deterministic runtime responsible for interpreting workflow definitions (Molecules), executing desktop interactions, and validating outcomes against observed system state.

The system is built around a core engineering principle:

>Execution must be *explicit, observable, and verifiable* against real environment state, not assumed.

---

# System Context

At a higher level, Task Engine is designed to connect:

* structured intent (workflows)
* runtime system state (desktop observation)
* controlled execution (UI + OS interaction)

The broader architecture (outside this repository) focuses on planning and goal reasoning.
This repository focuses only on execution correctness and environment interaction.

---

# What This Repository Actually Does

This is a deterministic desktop workflow execution engine.

It:

* loads structured workflow definitions (.mol files)
* executes them step-by-step
* interacts with Windows applications and UI
* observes screen/UI state during execution
* validates expected outcomes before continuing

The runtime ensures that each step is grounded in real system feedback, not blind execution.

---

# Execution Model (Unified Surface)

## 1. Visual Interaction

Screen-based interaction using OCR + coordinates to click, locate, and verify UI elements.

Example:

```
clickText("Upload")
```

---

## 2. Application & Target Resolution

Open applications, URLs, and folders.

Example:

```
openTarget("Chrome")
openTarget("github.com")
```

---

## 3. Semantic UI Control Layer

Interacts with UI Automation tree and resolves controls semantically (not pixel-based).

Example:

```
activateControl("Save")
activateControl("OK")
```

---

## 4. State Observation Layer

Reads UI state and system state and extracts runtime values from the interface.

Examples:

* control state (checked / selected / active)
* dialog/window presence
* UI text values

---

## 5. File Retrieval Layer

Semantic file search and ranked resolution over filesystem.

Details about this layer is present here: https://github.com/SachdevaArnav/intent-file-search

Examples:

```
getFile("resume pdf from last month")
getFile("hostel document i downloaded yesterday")
```

---

# Execution Primitives (Runtime Surface)

These are the actual executable operations exposed by the system.

They map directly into OS + UI actions.

---

## Target Execution

```
openTarget()
```

→ application / URL / folder resolution

→ implemented via `eFriend.openTarget()`

---

## File Search

```
getFile()
```

→ semantic file lookup

→ implemented via `sending_query.file_search()`

---

## UI Interaction (Hybrid OCR + Coordinates)

```
click()
```

→ screen-based interaction using OCR + coordinate mapping

→ executed via IPC layer

---

## UI Automation Control

```
activateControl()
```

→ UIA-based semantic control invocation

→ executed via `ipc.activateControl()`

---

## Validation Layer

```
controlExists()
textExists()
```

→ ensures expected UI state is achieved after execution

---

## Timing Control

```
delay()
```

→ deterministic pacing between actions

---

# Core Architecture

## Workflow Orchestration (Java Layer)

Responsible for execution flow:

* `Main.java` → entry point / demo runner
* `TaskEngine.java` → execution loop coordinator
* `MoleculeReader.java` → workflow parser
* `Molecule.java` → workflow representation

TaskEngine is intentionally thin — it coordinates execution rather than making decisions.

Capability implementations include:

* `eFriend.java` (openTarget)
* `search2.java` / `sending_query.java` (file retrieval)
* `EngineIPC.java` (bridge execution)

---

## Automation Backend (Python Layer)

Handles direct OS interaction:

* window control
* mouse/keyboard automation
* OCR-based perception
* UI tree inspection

Entry point:

```
src/main/py/Main.py
```

---

## Java ↔ Python Bridge

Communication layer:

* structured command passing
* JSON-based execution results
* IPC-based runtime coordination

Core file:

```
EngineIPC.java
```

---

# Workflow Representation (Molecules)

Molecules are structured workflow definitions stored in `.mol` files.

Each Molecule defines:

* PRE conditions (required state before execution)
* action sequence
* POST conditions (expected state after execution)

This enables workflows that are:

* repeatable
* state-aware
* verifiable

---

# Perception & State Model

Execution is grounded in observed reality, not assumptions.

The system combines:

* UI Automation tree inspection
* OCR-based screen reading
* fuzzy matching over UI text
* window/control discovery

This enables execution in dynamic desktop environments.

Here’s a **clean, GitHub-ready Setup section** matching your Task Engine tone (minimal fluff, engineer-style, not AI-ish):

---

# Setup

## Prerequisites

* Java 21+
* Python 3.10.11
* Maven 3.8+
* Windows OS (UI Automation + OCR dependency)

---

## Python Environment

```
python -m venv pyenv
pyenv\Scripts\activate
pip install -r requirements.txt
```

---

## Java Build

```
mvn clean install
```
Execution starts from `Main.java` (console entry point).

# Engineering Characteristics

* deterministic execution flow
* state-aware validation loop
* hybrid UI perception (UIA + OCR)
* modular Java orchestration + Python execution substrate
* extensible workflow-based design

---

# Summary

Task Engine is a deterministic desktop execution runtime that:

* interprets structured workflows (Molecules)
* executes OS and UI actions via a hybrid Java–Python system
* observes real-time desktop state using OCR and UI Automation
* validates outcomes using explicit postconditions
* operates as a closed-loop execution engine over Windows environments

# Extension Points (System Evolution)

* Higher-level planning layer for intent decomposition and goal reasoning
* Expanded execution primitives for richer OS/UI interactions
* Improved perception combining UIA, OCR, and semantic matching
* Structured recovery and re-planning loops for failure handling
* Multi-environment execution support beyond current Windows scope

All extensions preserve explicit, observable, state-grounded execution.


>To get more details regarding the broader Task Engine system (Design & Architecture),
 view this: https://tinyurl.com/5xmb8xh7
