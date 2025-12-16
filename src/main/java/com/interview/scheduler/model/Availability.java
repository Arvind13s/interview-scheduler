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
    
    private String dayOfWeek;
    
    private LocalTime startTime;
    
    private LocalTime endTime; 
}