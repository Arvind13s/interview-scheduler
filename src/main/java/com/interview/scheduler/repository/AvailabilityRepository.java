package com.interview.scheduler.repository;

import com.interview.scheduler.model.Availability;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    List<Availability> findByInterviewerId(Long interviewerId);
}