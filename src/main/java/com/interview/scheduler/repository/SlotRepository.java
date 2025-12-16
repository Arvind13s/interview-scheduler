package com.interview.scheduler.repository;

import com.interview.scheduler.model.InterviewSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SlotRepository extends JpaRepository<InterviewSlot, Long> {
    // Find all slots for a specific interviewer
    List<InterviewSlot> findByInterviewerId(Long interviewerId);
    
    // Find available slots (for candidate view)
    List<InterviewSlot> findByStatus(String status);
}