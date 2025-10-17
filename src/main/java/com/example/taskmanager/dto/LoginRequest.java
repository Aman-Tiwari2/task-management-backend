package com.example.taskmanager.dto;

public record LoginRequest(
        String email,
        String password
) {}