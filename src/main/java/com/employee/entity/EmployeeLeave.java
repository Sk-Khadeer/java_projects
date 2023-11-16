package com.employee.entity;


import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Entity
@Data
@Table(name="employee_leave")
public class EmployeeLeave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long leaveId;

    private String empCode;
    private int totalSickLeave;
    private int totalCasualLeave;
    private int remainingSickLeaves;
    private int remainingCasualLeaves;
    private int usedSickLeaves;
    private int usedCasualLeaves;
    @Transient
    private LocalDate startDate;
    @Transient
    private LocalDate endDate;
    @Transient
    @Enumerated(EnumType.STRING)
    private LeaveStatus status;


}