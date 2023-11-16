package com.employee.entity;

import java.time.LocalDate;

import lombok.Data;

@Data
public class LeaveRequestDTO {
	private Long id;
    private String empCode;
    private String type;
    private int days;
    private String reason;
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveStatus status;

}