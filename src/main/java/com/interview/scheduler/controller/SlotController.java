package com.interview.scheduler.controller;

import com.interview.scheduler.model.InterviewSlot;
import com.interview.scheduler.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.List;

@RestController
@RequestMapping("/api/slots")
public class SlotController {

    @Autowired
    private BookingService bookingService;

    @PostMapping("/generate/{interviewerId}")
    public ResponseEntity<String> generateSlots(@PathVariable Long interviewerId) {
        bookingService.generateSlotsForInterviewer(interviewerId);
        return ResponseEntity.ok("Slots generated for the next 14 days!");
    }

    @PostMapping("/{slotId}/book")
    public ResponseEntity<?> bookSlot(@PathVariable Long slotId, @RequestParam Long candidateId) {
        try {
            InterviewSlot bookedSlot = bookingService.bookSlot(slotId, candidateId);
            return ResponseEntity.ok(bookedSlot);
        } catch (ObjectOptimisticLockingFailureException e) {
            return ResponseEntity.status(409).body("Error: This slot was just taken by someone else.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/reschedule")
    public ResponseEntity<?> reschedule(@RequestParam Long oldSlotId, 
                                        @RequestParam Long newSlotId, 
                                        @RequestParam Long candidateId) {
        try {
            InterviewSlot newBooking = bookingService.rescheduleSlot(oldSlotId, newSlotId, candidateId);
            return ResponseEntity.ok(newBooking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/available")
    public List<InterviewSlot> getAvailableSlots(@RequestParam(defaultValue = "0") Long cursor,
                                                 @RequestParam(defaultValue = "10") int limit) {
        return bookingService.getAvailableSlotsWithCursor(cursor, limit);
    }

    @GetMapping("/booked")
    public List<InterviewSlot> getBookedSlots(@RequestParam(required = false) Long candidateId) {
        if (candidateId != null) {
            return bookingService.getBookedSlotsByCandidate(candidateId);
        }
        return bookingService.getAllBookedSlots();
    }
}