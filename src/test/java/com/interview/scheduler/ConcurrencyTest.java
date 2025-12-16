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
        InterviewSlot slot = new InterviewSlot();
        slot.setStartTime(LocalDateTime.now().plusDays(1));
        slot.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));
        slot.setStatus("AVAILABLE");
        slot.setInterviewerId(1L);
        slot.setVersion(0);
        slot = slotRepository.save(slot);
        
        final Long slotId = slot.getId();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

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

        executor.execute(userA);
        executor.execute(userB);

        latch.await(); 

        System.out.println("Successes: " + successCount.get());
        System.out.println("Failures: " + failCount.get());

        assertEquals(1, successCount.get(), "Only one user should succeed!");
        assertEquals(1, failCount.get(), "The other user should fail with concurrency error!");
    }
}