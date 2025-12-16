# Automatic Interview Scheduling System

## 1. System Overview
This is a backend REST API developed in **Java Spring Boot** that allows interviewers to set availability and candidates to book, manage, and reschedule interview slots. The system is designed to handle high concurrency, ensuring that no two candidates can book the same slot simultaneously.

## 2. Architecture
The system follows **Clean Architecture** principles to separate concerns:
* **Controller Layer:** Handles HTTP requests and response formatting.
* **Service Layer:** Contains business logic (Booking rules, Rescheduling atomicity).
* **Repository Layer:** Abstracted database interactions using Spring Data JPA.
* **Domain Layer:** Core entities (`InterviewSlot`, `Availability`) representing the database schema.

## 3. Key Features & Implementation
### A. Slot Generation
* **Logic:** Interviewers define weekly availability (e.g., "Mondays 9-12"). The system projects this 14 days into the future, creating individual 1-hour `InterviewSlot` records.
* **Scalability:** Slots are pre-generated, making the "Get Available Slots" API extremely fast (O(1) lookup vs. O(N) calculation on the fly).

### B. Concurrency Handling (Race Conditions)
* **Problem:** Multiple candidates clicking "Book" on the same slot at the same millisecond.
* **Solution:** **Optimistic Locking** via JPA `@Version`.
* **Trade-off Discussion:** We chose Optimistic Locking over Pessimistic Locking because interview bookings are "low-contention" (rarely do 100+ people fight for one slot). Optimistic locking provides higher throughput and doesn't block database rows unnecessarily.

### C. Atomic Rescheduling
* **Implementation:** The `rescheduleSlot` method uses the `@Transactional` annotation.
* **Behavior:** It performs two actions: (1) Releasing the old slot and (2) Booking the new slot. If either fails, the entire transaction rolls back, preventing data corruption.

## 4. API Endpoints
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/slots/generate/{id}` | Generates slots based on availability rules. |
| `GET` | `/api/slots/available` | Lists all open slots for the next 14 days. |
| `POST` | `/api/slots/{id}/book` | Books a specific slot (Safe against race conditions). |
| `POST` | `/api/slots/reschedule` | Atomically swaps an existing booking for a new one. |

## 5. Database Schema
* **Users:** Stores Interviewers and Candidates.
* **Availability:** Stores recurring weekly schedules.
* **Interview_Slots:** Stores actual actionable slots. Includes `version` column for locking.

## 6. Testing
* **Integration Testing:** Added `ConcurrencyTest.java` using `ExecutorService` to simulate multi-threaded user attacks.
* **Result:** Verified that when 2 threads attempt to book the same slot, exactly 1 succeeds and 1 fails.