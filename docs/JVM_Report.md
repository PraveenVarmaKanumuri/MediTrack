# JVM Report — MediTrack

## 1. Java Virtual Machine Overview

The Java Virtual Machine (JVM) is an abstract computing machine that enables the "Write Once, Run Anywhere" promise of Java. It provides a runtime environment in which Java bytecode is executed, abstracting the application from the underlying hardware and operating system.

### Write Once, Run Anywhere

Java source files (`.java`) are compiled by `javac` into platform-independent bytecode (`.class` files). The JVM on any supported OS then interprets or compiles this bytecode at runtime. MediTrack was developed on Windows 11 and can run unmodified on Linux or macOS as long as a compatible JDK is installed.

---

## 2. Class Loading Subsystem

The class loader subsystem is responsible for loading, linking, and initializing class files.

### 2.1 Three-Phase Process

| Phase | What Happens |
|-------|-------------|
| **Loading** | Reads the `.class` file bytes from the classpath and creates a `Class` object in the Method Area |
| **Linking** | Verifies bytecode integrity, allocates static field memory, and resolves symbolic references |
| **Initialization** | Executes `static` initializers and assigns static field values |

### 2.2 Class Loader Hierarchy

```
Bootstrap ClassLoader   (rt.jar / java.* classes)
        │
Extension ClassLoader   (ext/ directory)
        │
Application ClassLoader (project classpath — MediTrack classes)
```

The **delegation model** means each loader first asks its parent before loading a class itself, preventing duplicate or conflicting class definitions.

### 2.3 MediTrack Example — Static Initializers

`IdGenerator` uses an **eager singleton** with an explicit static block, demonstrating class initialization:

```java
// IdGenerator.java
private static final IdGenerator INSTANCE;
static {
    INSTANCE = new IdGenerator();
    logger.info("Singleton initialized (eager static block)");
}
```

This static block runs exactly once when the JVM's Application ClassLoader first loads `IdGenerator`. The JVM guarantees this initialization is thread-safe.

`Constants.java` also uses a static block to log that application constants have been loaded, triggered on first class access.

---

## 3. Runtime Data Areas

The JVM partitions memory into several distinct runtime data areas.

### 3.1 Method Area (Metaspace in Java 8+)

Stores per-class metadata: class structure, method bytecode, constant pool, and `static` fields/blocks.

**MediTrack examples:**
- `IdGenerator.INSTANCE` — a `static final` reference lives here
- `AppConfig.instance` — `static volatile` field lives here
- `Constants.DATA_DIR`, `MAX_APPOINTMENTS_PER_DAY`, etc. — all `static final` constants
- Bytecode for every method in every class

### 3.2 Heap

The heap is shared across all threads and holds all object instances and arrays. It is divided into:

```
┌──────────────────────────────────────────────────────────┐
│  Young Generation                                        │
│   ├── Eden Space  (new objects allocated here)          │
│   └── Survivor 0 / Survivor 1  (survived minor GC)     │
├──────────────────────────────────────────────────────────│
│  Old (Tenured) Generation  (long-lived objects)         │
└──────────────────────────────────────────────────────────┘
```

**MediTrack examples:**
- Every `Patient`, `Doctor`, `Appointment`, `Bill` instance lives on the heap
- The `CopyOnWriteArrayList<AppointmentObserver>` in `AppointmentServiceImpl`
- `HashMap`-backed `InMemoryStore` instances for each service
- `ConcurrentHashMap` in stores ensure thread-safe heap access

### 3.3 JVM Stack (per thread)

Each thread has its own stack of **frames**. A new frame is pushed for every method call and popped when it returns. Each frame holds:
- **Local variable array** — method parameters and local variables
- **Operand stack** — working area for bytecode instructions
- **Reference to runtime constant pool**

**MediTrack example:** When `AppointmentServiceImpl.bookAppointment()` is called, a frame is pushed containing local variables like `patientId`, `doctorId`, and the newly created `Appointment` reference. Nested calls to `Validator.requireNonBlank()` push additional frames.

### 3.4 PC Register (Program Counter)

Each thread has its own PC register that holds the address of the currently executing JVM instruction. For native methods the PC is undefined.

### 3.5 Native Method Stack

Supports native (C/C++) method calls via JNI. Used internally by the JDK but not directly by MediTrack application code.

---

## 4. Execution Engine

### 4.1 Interpreter

When a class is first loaded, the JVM interprets bytecode instructions one at a time. Interpretation has low startup cost but is slower for hot (frequently executed) code.

### 4.2 JIT Compiler (Just-In-Time)

The JIT compiler monitors execution and identifies "hot spots" — methods called frequently. It compiles those methods to native machine code at runtime, eliminating interpretation overhead for subsequent calls.

**HotSpot JVM tiers (Java 21):**

| Tier | Compiler | When |
|------|----------|------|
| 0 | Interpreter | Cold code |
| 1–3 | C1 (client compiler) | Moderately hot — fast compile, basic optimizations |
| 4 | C2 (server compiler) | Hottest methods — aggressive optimizations (inlining, loop unrolling, escape analysis) |

**MediTrack hot paths likely JIT-compiled:**
- `InMemoryStore.findById()` / `findAll()` — called by every service method
- `Validator.requireNonBlank()` / `requireNonNull()` — called on nearly every operation
- `AppointmentServiceImpl.notifyObservers()` — dispatches on every status change

### 4.3 Garbage Collector

Java manages memory automatically. The GC reclaims heap objects that are no longer reachable. Java 21 defaults to the **G1 (Garbage-First) GC**, which divides the heap into equal-sized regions and prioritizes collecting regions with the most garbage first.

**Minor GC** — collects the Young Generation. Most short-lived `Appointment` or `Bill` objects created during a single menu interaction are collected here.

**Major/Full GC** — collects the Old Generation. Long-lived singletons (`IdGenerator`, `AppConfig`) and service instances are promoted here and rarely collected.

---

## 5. Concurrency in MediTrack and the JVM

### 5.1 Thread Safety Mechanisms Used

| Mechanism | Location | Purpose |
|-----------|----------|---------|
| `volatile` | `AppConfig.instance` | Ensures visibility of the singleton reference across threads |
| `synchronized` | `AppConfig.getInstance()` | Prevents two threads from creating the instance simultaneously |
| `AtomicInteger` | `AppointmentReminderScheduler.scheduledCount`, `IdGenerator._counter` | Lock-free thread-safe counters |
| `AtomicReference` | `DateUtil.dateTimeProvider` | Lock-free thread-safe provider swap |
| `CopyOnWriteArrayList` | `AppointmentServiceImpl.observers` | Safe observer list iteration during concurrent notifications |
| `ScheduledExecutorService` | `AppointmentReminderScheduler` | Managed thread pool for periodic reminder tasks |

### 5.2 JVM Memory Model Relevance

The Java Memory Model (JMM) guarantees that:
- A `volatile` write **happens-before** any subsequent `volatile` read of the same variable — ensuring the `AppConfig` singleton is fully constructed before other threads see the reference.
- Actions within a `synchronized` block are visible to any thread that subsequently acquires the same lock.

---

## 6. Serialization and the JVM

`MedicalEntity implements Serializable` enables Java's built-in object graph serialization. The JVM's `ObjectOutputStream` uses reflection to traverse all non-`transient` fields and write them to a binary stream. On deserialization, `ObjectInputStream` reconstructs the object graph without calling constructors.

**MediTrack usage:**
- `SerializationUtil.serialize()` / `.deserialize()` persist `List<Appointment>` to `appointments.ser`
- `serialVersionUID = 1L` on every serializable class prevents accidental version-mismatch errors during deserialization

---

## 7. Summary Table

| JVM Concept | MediTrack Implementation |
|-------------|--------------------------|
| Class loading | `IdGenerator` static block (eager init on first load) |
| Method Area | All `static final` constants in `Constants.java` |
| Heap | `Patient`, `Doctor`, `Appointment`, `Bill` instances; service stores |
| JVM Stack | Per-call frames in every service method |
| JIT Compilation | Hot paths: `findById`, `Validator` checks, observer dispatch |
| Garbage Collection | G1 GC manages short-lived menu-operation objects + long-lived singletons |
| Volatile + Synchronized | `AppConfig` lazy singleton double-checked locking |
| AtomicInteger / AtomicReference | Lock-free counters and provider swapping in `IdGenerator`, `DateUtil` |
| Serialization | `SerializationUtil` + `appointments.ser` persistence |
