package com.interview.scheduler.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalTime;

@Entity
@Data
public class Availability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long interviewerId;
    
    // e.g., "MONDAY", "TUESDAY"
    private String dayOfWeek; 
    
    // e.g., 09:00:00
    private LocalTime startTime; 
    
    // e.g., 12:00:00
    private LocalTime endTime; 
}