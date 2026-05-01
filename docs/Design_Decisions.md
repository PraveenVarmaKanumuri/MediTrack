# Design Decisions — MediTrack

## 1. Domain Model Hierarchy

### Decision: Abstract base classes `MedicalEntity` → `Person` → `Doctor` / `Patient`

`MedicalEntity` provides the universal audit trail (`createdAt`, `updatedAt`, `id`) and `Serializable` support for all domain objects. `Person` adds the human-specific fields (name, DOB, email, phone) shared by both `Doctor` and `Patient`.

**Why:** Keeps audit logic in one place. Every entity automatically gets `getAuditInfo()` and `markUpdated()` without duplication. The `DateTimeProvider` abstraction injected into `MedicalEntity` allows tests to control the clock.

**Trade-off:** A deeper hierarchy means slightly more coupling between layers, but the shared behavior here is genuine and stable.

---

## 2. Static Factory Methods over Public Constructors

### Decision: Private constructors + `create()` / `createWithSchedule()` static factories

All entities (`Patient`, `Doctor`, `Appointment`, `Bill`) use private constructors exposed only through named static factory methods.

**Why:** Named factories (`Doctor.create()` vs `Doctor.createWithSchedule()`) communicate intent clearly. They also allow validation to live in the constructor without leaking complex overloads to callers, and they enable future caching or pooling without changing the API.

---

## 3. Strategy Pattern for Billing

### Decision: `BillingStrategy` interface with four implementations

`StandardBillingStrategy`, `SeniorCitizenBillingStrategy`, `InsuranceBillingStrategy`, and `EmergencyBillingStrategy` each implement `double calculate(Bill bill)`.

**Why:** Adding a new pricing model (e.g., corporate discount) requires only a new class — no changes to `Bill` or the billing workflow. The `BillFactory` selects the correct strategy at creation time based on `BillType`.

**Trade-off:** More classes. Justified because billing rules are genuinely volatile and the open/closed principle pays off here.

---

## 4. Template Method Pattern for Workflows

### Decision: `BillGenerationTemplate` and `AppointmentCompletionTemplate`

Abstract classes define the skeleton of multi-step workflows. Concrete subclasses override only the steps that vary.

**Why:** Prevents duplication of the orchestration sequence (validate → execute → notify → persist) while allowing customization of individual steps. The invariant order is guaranteed by the template.

---

## 5. Observer Pattern for Appointment Notifications

### Decision: `AppointmentObserver` interface + `CopyOnWriteArrayList<AppointmentObserver>`

`AppointmentServiceImpl` maintains a list of observers and calls `onAppointmentChanged()` on every status transition.

**Why:** Decouples notification logic from business logic. New notification channels (email, SMS) can be added by implementing the interface and registering — no changes to `AppointmentServiceImpl`.

**CopyOnWriteArrayList specifically:** Observer lists are iterated far more often than they are modified. `CopyOnWriteArrayList` provides thread-safe iteration without locking on reads, which is the hot path.

---

## 6. Singleton Variants — Eager vs Lazy

### Decision: Two different singleton implementations to demonstrate both patterns

| Class | Pattern | Mechanism |
|-------|---------|-----------|
| `IdGenerator` | Eager singleton | `static final` field initialized in a `static {}` block |
| `AppConfig` | Lazy singleton | `volatile` field + double-checked locking in `synchronized` block |

**Why:** `IdGenerator` is always needed and lightweight to initialize — eager is simpler and guaranteed thread-safe by the JVM class-loading contract. `AppConfig` may not always be needed and reads from system properties, making lazy initialization appropriate. The double-checked locking pattern (with `volatile`) avoids the synchronization cost on every subsequent `getInstance()` call.

---

## 7. AppointmentStatus State Machine

### Decision: `canTransitionTo()` method on each enum constant

```
PENDING → CONFIRMED → COMPLETED → PAID (terminal)
PENDING → CANCELLED (terminal)
CONFIRMED → CANCELLED (terminal)
```

**Why:** Encoding valid transitions in the enum itself rather than in service logic ensures the rules are enforced uniformly regardless of which code path triggers a status change. `PAID` as a terminal state prevents re-billing a paid appointment.

**Effect on bill generation:** `Main.generateBill()` filters out `PAID` and `CANCELLED` appointments before presenting the list, so users never see already-paid or cancelled appointments in the billing menu.

---

## 8. Immutable Lists with Defensive Copies

### Decision: `Collections.unmodifiableList()` on all public list getters; `final` list fields

All list fields (`medicalHistory`, `appointmentIds`, `notes`, `leaveDates`) are `final` and returned as unmodifiable views.

**Why:** Prevents callers from mutating internal state by bypassing the validated mutator methods (`addMedicalHistory()`, `addNote()`, etc.). This makes the entities easier to reason about and thread-safer.

**Deep clone implication:** Because list fields are `final`, `super.clone()` produces a shallow copy where both the original and the clone share the same `ArrayList`. `deepClone()` therefore uses the factory method to construct a fresh instance with new empty lists, then copies entries via `forEach`.

---

## 9. Exception Hierarchy

### Decision: `InvalidDataException` (unchecked) vs `DataPersistenceException` (checked)

| Exception | Type | When |
|-----------|------|------|
| `InvalidDataException` | `RuntimeException` | Programmer errors: null inputs, invalid transitions, bad data |
| `AppointmentNotFoundException` | `RuntimeException` | Lookup failures in service layer |
| `DataPersistenceException` | `Exception` (checked) | I/O failures during CSV or serialization operations |

**Why:** Validation failures are programming errors — callers should fix them, not catch them. Persistence failures are environmental (disk full, missing file) — callers must handle or propagate them. Exception chaining (`new DataPersistenceException(op, msg, cause)`) preserves the root cause for diagnostics.

---

## 10. In-Memory Store with Generic `InMemoryStore<T>`

### Decision: Generic `InMemoryStore<T>` backed by `ConcurrentHashMap`

All three services use `InMemoryStore<Doctor>`, `InMemoryStore<Patient>`, and `InMemoryStore<Appointment>` for O(1) ID-based lookup.

**Why:** `ConcurrentHashMap` provides thread-safe reads and segment-level locking for writes, avoiding the bottleneck of a fully synchronized map. The generic store eliminates boilerplate — the same code handles all entity types.

---

## 11. Logging Strategy

### Decision: SLF4J API with Logback implementation; dual appenders

All classes use `LoggerFactory.getLogger(ClassName.class)`. Logback is configured with:
- **Console appender** — concise format for development feedback
- **Rolling file appender** — `logs/meditrack.log`, daily rotation, 7-day retention

**Why:** SLF4J decouples application code from the logging framework. Switching from Logback to Log4j2 would require only a dependency swap, not code changes. The rolling file appender preserves a history of application runs for audit purposes.

---

## 12. Concurrency — `ScheduledExecutorService` for Reminders

### Decision: `AppointmentReminderScheduler` uses `ScheduledExecutorService` with `AtomicInteger`

**Why:** `ScheduledExecutorService` is the standard Java approach for periodic tasks. It manages thread lifecycle correctly and handles exceptions without silently killing the scheduler. `AtomicInteger` tracks scheduled count lock-free, avoiding contention on a hot counter.

---

## 13. CSV + Java Serialization as Dual Persistence

### Decision: CSV for human-readable doctor/patient data; Java serialization for appointments

| Data | Format | Rationale |
|------|--------|-----------|
| Doctors, Patients | CSV | Human-readable, easy to inspect/edit |
| Appointments | `.ser` binary | Preserves the full `AppointmentStatus` state machine state and `notes` list without custom parsing |

**Why:** Appointments have richer state (enum status, list of notes, audit timestamps) that is tedious to serialize/deserialize in CSV without a schema. Java serialization handles the full object graph automatically. Doctors and patients have flat, predictable fields well-suited to CSV.
