package com.example.taskmanager.dto;

import com.example.taskmanager.model.Role;

public record UserDTO(Long id, String name ,String email, Role role) {}
