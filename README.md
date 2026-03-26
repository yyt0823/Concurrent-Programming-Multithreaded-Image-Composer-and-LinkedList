# COMP409 — Assignment 1: Concurrent Programming in Java

Two concurrency exercises exploring multi-threading, synchronization, and lock design in Java.

---

## Q1 — Parallel Icon Compositor (`q1.java`)

Randomly places **n icons** onto a 2048×2048 canvas using multiple threads, ensuring no two icons overlap.

### Approach
- The output image is divided into a **16×16 grid of tiles**, each protected by its own lock
- Each worker thread repeatedly: picks a random icon → picks a random tile → validates the position (boundary check) → draws the icon — all within the tile's lock
- A shared counter (protected by `COUNT_LOCK`) tracks remaining placements; threads exit when it reaches 0
- Uses `ThreadLocalRandom` for thread-safe, high-performance random number generation

### Key Design Decisions
- **Tile-based locking** over a single global lock: threads working on different tiles run fully in parallel
- Position validation and drawing are **atomic within the tile lock** to prevent overlapping icons from two threads passing validation simultaneously
- Icons are constrained to stay within their assigned tile, trading a small amount of placement randomness for a large speedup

### Usage
```bash
javac q1.java
java q1 -w 2048 -h 2048 -t 8 -n 100
```
| Flag | Description | Default |
|------|-------------|---------|
| `-w` | Output image width | 2048 |
| `-h` | Output image height | 2048 |
| `-t` | Number of threads | 8 |
| `-n` | Number of icons to place | 100 |

---

## Q2 — Concurrent Circular Linked List (`q2.java`)

Three threads operate simultaneously on a **circular linked list** seeded with nodes A → B → C for 5 seconds.

| Thread | Role | Behavior |
|--------|------|----------|
| `worker0` | Reader | Traverses the list, printing each character every 100ms |
| `worker1` | Deleter | 10% chance per step to remove the current node (never removes A/B/C) |
| `worker2` | Inserter | 10% chance per step to insert a random non-ABC character after current node |

### Key Design Decisions
- `Node.next` is declared **`volatile`** so that insertions/deletions by one thread are immediately visible to all others without requiring full synchronization
- A/B/C nodes are marked `protectedABC = true` and are never deleted, guaranteeing the list always has at least 3 nodes
- The `running` flag is also `volatile` to ensure threads see the main thread's stop signal promptly after 5 seconds

### Known Race Conditions (by design)
The assignment intentionally leaves some races unguarded to illustrate their effects:
- **Lost insert**: deleter unlinks a node at the same moment an inserter links into it
- **Lost delete**: inserter sets `current.next` after deleter already bypassed it

---

## Files

| File | Description |
|------|-------------|
| `q1.java` | Multi-threaded icon compositor |
| `q2.java` | Concurrent circular linked list |
| `icon1-8.png` | Input icons for Q1 |
| `outputimage.png` | Sample output from Q1 |
| `assig1.pdf` | Assignment specification |
| `declaration.txt` | Academic integrity declaration |
