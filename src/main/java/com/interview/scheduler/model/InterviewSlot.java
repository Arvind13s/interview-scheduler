package com.interview.scheduler.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "interview_slots")
public class InterviewSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    // "AVAILABLE" or "BOOKED"
    @Column(nullable = false)
    private String status; 

    private Long interviewerId; // ID of the person conducting
    private Long candidateId;   // ID of the person booking (can be null)

    // CRITICAL: Handles Race Conditions 
    @Version
    private Integer version;
}