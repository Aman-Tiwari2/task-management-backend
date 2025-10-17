package com.example.taskmanager.controller;

import com.example.taskmanager.dto.UserDTO;
import com.example.taskmanager.model.Role;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@Tag(name = "user-controller", description = "Admin control for user management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ✅ Get all users (Admin only)
    @Operation(summary = "Get all registered users (Admin only)")
    @GetMapping
    public ResponseEntity<?> getAllUsers(Authentication auth) {
        User currentUser = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!currentUser.getRole().equals(Role.ADMIN)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied: Admins only"));
        }

        List<UserDTO> users = userRepository.findAll().stream()
                .map(u -> new UserDTO(u.getId(), u.getName(), u.getEmail(), u.getRole()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }

    // ✅ Update a user's role (Admin only)
    @Operation(
            summary = "Update user role (Admin only)",
            description = "Change a user's role. Allowed roles: ADMIN, USER"
    )
    @PutMapping("/{id}/role")
    public ResponseEntity<?> updateUserRole(
            @PathVariable Long id,
            @RequestParam("role") String newRole,
            Authentication auth
    ) {
        // ✅ Check if current user is ADMIN
        User currentUser = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!currentUser.getRole().equals(Role.ADMIN)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied: Admins only"));
        }

        // ✅ Find target user
        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        // ✅ Validate new role
        Role roleEnum;
        try {
            roleEnum = Role.valueOf(newRole.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid role. Allowed values: ADMIN, USER"));
        }

        // ✅ Prevent self-demotion (safety)
        if (targetUser.getId().equals(currentUser.getId()) && roleEnum == Role.USER) {
            return ResponseEntity.badRequest().body(Map.of("error", "Admins cannot demote themselves"));
        }

        // ✅ Update role
        targetUser.setRole(roleEnum);
        userRepository.save(targetUser);

        return ResponseEntity.ok(Map.of(
                "message", "User role updated successfully",
                "userId", targetUser.getId(),
                "newRole", targetUser.getRole().name()
        ));
    }
}
//package com.example.taskmanager.controller;
//
//import com.example.taskmanager.dto.TaskDTO;
//import com.example.taskmanager.dto.UserDTO;
//import com.example.taskmanager.model.Role;
//import com.example.taskmanager.model.Task;
//import com.example.taskmanager.model.User;
//import com.example.taskmanager.repository.TaskRepository;
//import com.example.taskmanager.repository.UserRepository;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.security.SecurityRequirement;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@RestController
//@RequestMapping("/api/users")
//@Tag(name = "user-controller", description = "Admin control for user management")
//@SecurityRequirement(name = "bearerAuth")
//public class UserController {
//
//    private final UserRepository userRepository;
//    private final TaskRepository taskRepository;
//
//    public UserController(UserRepository userRepository, TaskRepository taskRepository) {
//        this.userRepository = userRepository;
//        this.taskRepository = taskRepository;
//    }
//
//    // ✅ Get all users (Admin only)
//    @Operation(summary = "Get all registered users (Admin only)")
//    @GetMapping
//    public ResponseEntity<?> getAllUsers(Authentication auth) {
//        User currentUser = userRepository.findByEmail(auth.getName())
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        if (!currentUser.getRole().equals(Role.ADMIN)) {
//            return ResponseEntity.status(403).body(Map.of("error", "Access denied: Admins only"));
//        }
//
//        List<UserDTO> users = userRepository.findAll().stream()
//                .map(u -> new UserDTO(u.getId(), u.getName(), u.getEmail(), u.getRole()))
//                .collect(Collectors.toList());
//
//        return ResponseEntity.ok(users);
//    }
//
//    // ✅ Update a user's role (Admin only)
//    @Operation(
//            summary = "Update user role (Admin only)",
//            description = "Change a user's role. Allowed roles: ADMIN, USER"
//    )
//    @PutMapping("/{id}/role")
//    public ResponseEntity<?> updateUserRole(
//            @PathVariable Long id,
//            @RequestParam("role") String newRole,
//            Authentication auth
//    ) {
//        User currentUser = userRepository.findByEmail(auth.getName())
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        if (!currentUser.getRole().equals(Role.ADMIN)) {
//            return ResponseEntity.status(403).body(Map.of("error", "Access denied: Admins only"));
//        }
//
//        User targetUser = userRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Target user not found"));
//
//        Role roleEnum;
//        try {
//            roleEnum = Role.valueOf(newRole.toUpperCase());
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.badRequest().body(Map.of("error", "Invalid role. Allowed values: ADMIN, USER"));
//        }
//
//        if (targetUser.getId().equals(currentUser.getId()) && roleEnum == Role.USER) {
//            return ResponseEntity.badRequest().body(Map.of("error", "Admins cannot demote themselves"));
//        }
//
//        targetUser.setRole(roleEnum);
//        userRepository.save(targetUser);
//
//        return ResponseEntity.ok(Map.of(
//                "message", "User role updated successfully",
//                "userId", targetUser.getId(),
//                "newRole", targetUser.getRole().name()
//        ));
//    }
//
//    // ✅ Get all tasks assigned to a specific user (Admin only)
//    @Operation(
//            summary = "Get all tasks assigned to a user (Admin only)",
//            description = "Returns all tasks assigned to a specific user ID. Admin-only endpoint."
//    )
//    @GetMapping("/{id}/tasks")
//    public ResponseEntity<?> getUserTasks(@PathVariable Long id, Authentication auth) {
//        User currentUser = userRepository.findByEmail(auth.getName())
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        if (!currentUser.getRole().equals(Role.ADMIN)) {
//            return ResponseEntity.status(403).body(Map.of("error", "Access denied: Admins only"));
//        }
//
//        User targetUser = userRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Target user not found"));
//
//        List<Task> tasks = taskRepository.findByAssignedTo(targetUser);
//
//        List<TaskDTO> taskDTOs = tasks.stream()
//                .map(task -> new TaskDTO(
//                        task.getId(),
//                        task.getTitle(),
//                        task.getDescription(),
//                        task.getStatus(),
//                        task.getPriority(),
//                        task.getDueDate(),
//                        task.getAssignedTo() != null ? task.getAssignedTo().getId() : null,
//                        task.getDocuments()
//                ))
//                .collect(Collectors.toList());
//
//        return ResponseEntity.ok(taskDTOs);
//    }
//}
