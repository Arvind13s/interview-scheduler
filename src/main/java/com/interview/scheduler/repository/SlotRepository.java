package com.interview.scheduler.repository;
import com.interview.scheduler.model.InterviewSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface SlotRepository extends JpaRepository<InterviewSlot, Long> {
    List<InterviewSlot> findByInterviewerId(Long interviewerId);
    
    List<InterviewSlot> findByStatus(String status);

    List<InterviewSlot> findByIdGreaterThanAndStatus(Long cursorId, String status, Pageable pageable);
    
    List<InterviewSlot> findByCandidateIdAndStatus(Long candidateId, String status);
}