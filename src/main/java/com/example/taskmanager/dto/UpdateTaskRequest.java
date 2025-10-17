//package com.example.taskmanager.dto;
//
//import com.example.taskmanager.model.TaskPriority;
//import com.example.taskmanager.model.TaskStatus;
//import java.time.LocalDate;
//
//public record UpdateTaskRequest(
//        String title,
//        String description,
//        TaskStatus status,
//        TaskPriority priority,
//        LocalDate dueDate,
//        Long assignedToId
//) {}
package com.example.taskmanager.dto;

import com.example.taskmanager.model.TaskPriority;
import com.example.taskmanager.model.TaskStatus;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record UpdateTaskRequest(
        @Size(max = 120, message = "Title must not exceed 120 characters")
        String title,

        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        TaskStatus status,

        TaskPriority priority,

        @FutureOrPresent(message = "Due date cannot be in the past")
        LocalDate dueDate,

        Long assignedToId
) {}
