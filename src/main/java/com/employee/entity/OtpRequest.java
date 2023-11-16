package com.employee.entity;

import lombok.Data;

@Data
public class OtpRequest {
    private String email;
    private int otp;

    // Getters, setters, and other boilerplate code...
}
