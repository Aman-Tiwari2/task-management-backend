package com.example.taskmanager.dto;

import com.example.taskmanager.model.TaskPriority;
import com.example.taskmanager.model.TaskStatus;
import java.time.LocalDate;
import java.util.List;

public record TaskDTO(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate,
        Long assignedToId,
        List<String> documents
) {}
