package com.example.taskmanager.controller;

import com.example.taskmanager.dto.CreateTaskRequest;
import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.dto.UpdateTaskRequest;
import com.example.taskmanager.dto.UploadRequest;
import com.example.taskmanager.model.TaskPriority;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "task-controller", description = "Task management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // ✅ Get all tasks
    @Operation(summary = "Get all tasks", description = "Supports filtering by status and priority")
    @GetMapping
    public ResponseEntity<Page<TaskDTO>> list(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            Pageable pageable,
            Authentication auth
    ) {
        return ResponseEntity.ok(taskService.list(auth, status, priority, pageable));
    }

    // ✅ Get a specific task
    @Operation(summary = "Get a specific task by ID")
    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getById(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(taskService.getById(auth, id));
    }

    // ✅ Create a new task
    @Operation(summary = "Create a new task")
    @PostMapping
    public ResponseEntity<TaskDTO> create(@Valid @RequestBody CreateTaskRequest req, Authentication auth) {
        return ResponseEntity.ok(taskService.create(auth, req));
    }

    // ✅ Update an existing task
    @Operation(summary = "Update an existing task")
    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> update(@PathVariable Long id, @Valid @RequestBody UpdateTaskRequest req, Authentication auth) {
        return ResponseEntity.ok(taskService.update(auth, id, req));
    }



    // ✅ Delete a task
    @Operation(summary = "Delete a task")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        taskService.delete(auth, id);
        return ResponseEntity.noContent().build();
    }

    // ✅ FILE UPLOAD ENDPOINT (Fixed for Swagger 2.2.21)
//    @Operation(
//            summary = "Upload PDF files for a task",
//            description = "Allows uploading one or more PDF files for a specific task"
//    )
//    @PostMapping(value = "/{id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<TaskDTO> uploadFiles(
//            @PathVariable Long id,
//            @Parameter(description = "One or more PDF files (PDF only)", required = true)
//            @RequestPart("files") MultipartFile[] files,
//            Authentication auth
//    ) throws IOException {
//        return ResponseEntity.ok(taskService.upload(auth, id, files));
//    }

    @Operation(
            summary = "Upload PDF files for a task (max 3 files)",
            description = "Allows uploading up to 3 PDF documents for a specific task"
    )
    @PostMapping(value = "/{id}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFiles(
            @PathVariable Long id,
            @Parameter(description = "One or more PDF files (PDF only)", required = true)
            @RequestParam("files") MultipartFile[] files,
            Authentication auth
    ) throws IOException {

        // ✅ Enforce max 3 file limit
        if (files.length == 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "At least one file must be uploaded"));
        }

        if (files.length > 3) {
            return ResponseEntity.badRequest().body(Map.of("error", "You can upload a maximum of 3 files only"));
        }

        // ✅ Optional: Validate PDF format
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Empty file not allowed"));
            }
            if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Only PDF files are allowed"));
            }
        }

        // ✅ Proceed with normal upload
        TaskDTO updatedTask = taskService.upload(auth, id, files);
        return ResponseEntity.ok(updatedTask);
    }

    // ✅ FILE DOWNLOAD ENDPOINT (Optional)
    @Operation(summary = "Get uploaded file by name", description = "Serves a specific uploaded PDF file")
    @GetMapping(value = "/file/{fileName}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getFile(@PathVariable("fileName") String fileName) throws IOException {
        Path path = Paths.get("uploads").resolve(fileName);
        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }

        byte[] data = Files.readAllBytes(path);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + fileName)
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }
}



