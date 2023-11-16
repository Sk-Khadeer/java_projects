package com.employee.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Response {

    private String status;
    private String message;

    // Constructors, getters, setters
}