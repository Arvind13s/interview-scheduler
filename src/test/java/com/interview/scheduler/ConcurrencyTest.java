package com.interview.scheduler;

import com.interview.scheduler.model.InterviewSlot;
import com.interview.scheduler.repository.SlotRepository;
import com.interview.scheduler.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ConcurrencyTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private SlotRepository slotRepository;

    @Test
    public void testRaceCondition() throws InterruptedException {
        // 1. SETUP: Create a fresh slot in the DB
        InterviewSlot slot = new InterviewSlot();
        slot.setStartTime(LocalDateTime.now().plusDays(1));
        slot.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        slot.setStatus("AVAILABLE");
        slot.setInterviewerId(1L);
        slot.setVersion(0);
        slot = slotRepository.save(slot);
        
        final Long slotId = slot.getId();

        // 2. SIMULATE: Two threads (users) running at the same time
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2); // Wait for both threads to be ready
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // User A Task
        Runnable userA = () -> {
            try {
                bookingService.bookSlot(slotId, 101L);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        };

        // User B Task
        Runnable userB = () -> {
            try {
                bookingService.bookSlot(slotId, 102L);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        };

        // 3. EXECUTE: Fire both at once
        executor.execute(userA);
        executor.execute(userB);

        // Wait for both to finish
        latch.await(); 

        // 4. ASSERT: Only ONE should succeed, the other MUST fail
        System.out.println("Successes: " + successCount.get());
        System.out.println("Failures: " + failCount.get());

        assertEquals(1, successCount.get(), "Only one user should succeed!");
        assertEquals(1, failCount.get(), "The other user should fail with concurrency error!");
    }
}