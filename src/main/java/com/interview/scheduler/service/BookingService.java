package com.interview.scheduler.service;
import com.interview.scheduler.model.InterviewSlot;
import com.interview.scheduler.repository.SlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.interview.scheduler.model.Availability;
import com.interview.scheduler.repository.AvailabilityRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@Service
public class BookingService {

    @Autowired
    private SlotRepository slotRepository;

    @Autowired
    private AvailabilityRepository availabilityRepository;

    // Requirement: Candidates can book a slot [cite: 9]
    @Transactional
    public InterviewSlot bookSlot(Long slotId, Long candidateId) {
        InterviewSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        if ("BOOKED".equals(slot.getStatus())) {
            throw new RuntimeException("Slot is already booked!");
        }

        slot.setStatus("BOOKED");
        slot.setCandidateId(candidateId);

        // If two people reach here at the same time, @Version will cause
        // one of them to fail with ObjectOptimisticLockingFailureException 
        return slotRepository.save(slot);
    }
    @Transactional
    public InterviewSlot rescheduleSlot(Long oldSlotId, Long newSlotId, Long candidateId) {
        
        // 1. Validate the NEW Slot (must be free)
        InterviewSlot newSlot = slotRepository.findById(newSlotId)
                .orElseThrow(() -> new RuntimeException("New slot not found"));
        
        if (!"AVAILABLE".equals(newSlot.getStatus())) {
            throw new RuntimeException("The new slot is already booked! Please pick another.");
        }

        // 2. Validate the OLD Slot (must be yours)
        InterviewSlot oldSlot = slotRepository.findById(oldSlotId)
                .orElseThrow(() -> new RuntimeException("Old slot not found"));

        if (!candidateId.equals(oldSlot.getCandidateId())) {
             throw new RuntimeException("You can only reschedule your own slots.");
        }

        // 3. RELEASE the Old Slot (Make it available again)
        oldSlot.setStatus("AVAILABLE");
        oldSlot.setCandidateId(null);
        slotRepository.save(oldSlot);

        // 4. BOOK the New Slot
        newSlot.setStatus("BOOKED");
        newSlot.setCandidateId(candidateId);
        return slotRepository.save(newSlot);
    }

    // Requirement: Generate slots (simplified for assessment) [cite: 5]
    public void createSlots(List<InterviewSlot> slots) {
        slotRepository.saveAll(slots);
    }
    
    public List<InterviewSlot> getAllAvailableSlots() {
        return slotRepository.findByStatus("AVAILABLE");
    }

    public void generateSlotsForInterviewer(Long interviewerId) {
        List<Availability> availabilities = availabilityRepository.findByInterviewerId(interviewerId);
        
        // Generate for next 14 days
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 14; i++) {
            LocalDate currentDate = today.plusDays(i);
            String currentDayName = currentDate.getDayOfWeek().name(); // e.g. "MONDAY"
            
            for (Availability av : availabilities) {
                if (av.getDayOfWeek().equalsIgnoreCase(currentDayName)) {
                    createHourlySlots(interviewerId, currentDate, av.getStartTime(), av.getEndTime());
                }
            }
        }
    }

    private void createHourlySlots(Long interviewerId, LocalDate date, LocalTime start, LocalTime end) {
        // Loop from start time to end time in 1-hour increments
        LocalTime current = start;
        while (current.isBefore(end)) {
            LocalDateTime slotStart = date.atTime(current);
            LocalDateTime slotEnd = date.atTime(current.plusHours(1));
            
            // SAVE THE SLOT
            InterviewSlot slot = new InterviewSlot();
            slot.setInterviewerId(interviewerId);
            slot.setStartTime(slotStart);
            slot.setEndTime(slotEnd);
            slot.setStatus("AVAILABLE");
            slot.setVersion(0);
            
            slotRepository.save(slot);
            
            current = current.plusHours(1);
        }
    }
    public List<InterviewSlot> getAvailableSlotsWithCursor(Long cursorId, int limit) {
        // Fetch 'limit' number of slots that come AFTER the cursorId
        return slotRepository.findByIdGreaterThanAndStatus(cursorId, "AVAILABLE", PageRequest.of(0, limit));
    }
}