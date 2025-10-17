package com.example.taskmanager.service;

import com.example.taskmanager.dto.CreateTaskRequest;
import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.dto.UpdateTaskRequest;
import com.example.taskmanager.model.*;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ✅ List all tasks (supports filters)
    public Page<TaskDTO> list(Authentication auth, TaskStatus status, TaskPriority priority, Pageable pageable) {
        User currentUser = getCurrentUser(auth);

        Page<Task> tasksPage;
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (isAdmin) {
            if (status != null) tasksPage = taskRepository.findByStatus(status, pageable);
            else if (priority != null) tasksPage = taskRepository.findByPriority(priority, pageable);
            else tasksPage = taskRepository.findAll(pageable);
        } else {
            if (status != null)
                tasksPage = taskRepository.findByAssignedToAndStatus(currentUser, status, pageable);
            else if (priority != null)
                tasksPage = taskRepository.findByAssignedToAndPriority(currentUser, priority, pageable);
            else
                tasksPage = taskRepository.findByAssignedTo(currentUser, pageable);
        }

        return tasksPage.map(this::toDTO);
    }

    // ✅ Create task
    public TaskDTO create(Authentication auth, CreateTaskRequest req) {
        User user = getCurrentUser(auth);

        String safeTitle = (req.title() == null || req.title().isBlank()) ? "Untitled Task" : req.title().trim();

        Task task = new Task();
        task.setTitle(safeTitle);
        task.setDescription(req.description());
        task.setStatus(req.status() == null ? TaskStatus.TODO : req.status());
        task.setPriority(req.priority() == null ? TaskPriority.MEDIUM : req.priority());
        task.setDueDate(req.dueDate() == null ? LocalDate.now().plusDays(1) : req.dueDate());
        task.setAssignedTo(user);

        return toDTO(taskRepository.save(task));
    }

    // ✅ Get task entity
    public Task get(Authentication auth, Long id) {
        User currentUser = getCurrentUser(auth);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (currentUser.getRole() != Role.ADMIN) {
            if (task.getAssignedTo() == null ||
                    !Objects.equals(task.getAssignedTo().getId(), currentUser.getId())) {
                throw new RuntimeException("Unauthorized access");
            }
        }
        return task;
    }

    // ✅ Get task DTO by ID (Controller calls this)
    public TaskDTO getById(Authentication auth, Long id) {
        return toDTO(get(auth, id));
    }

    // ✅ Update task
    public TaskDTO update(Authentication auth, Long id, UpdateTaskRequest req) {
        Task task = get(auth, id);

        if (req.title() != null && !req.title().isBlank()) task.setTitle(req.title().trim());
        if (req.description() != null) task.setDescription(req.description());
        if (req.status() != null) task.setStatus(req.status());
        if (req.priority() != null) task.setPriority(req.priority());
        if (req.dueDate() != null) task.setDueDate(req.dueDate());

        if (req.assignedToId() != null) {
            User assigned = userRepository.findById(req.assignedToId())
                    .orElseThrow(() -> new RuntimeException("Assigned user not found"));
            task.setAssignedTo(assigned);
        }

        if (task.getTitle() == null || task.getTitle().isBlank())
            task.setTitle("Untitled Task");

        return toDTO(taskRepository.save(task));
    }

    // ✅ Delete task
    public void delete(Authentication auth, Long id) {
        Task task = get(auth, id);
        taskRepository.delete(task);
    }

    // ✅ Upload PDFs
//    public TaskDTO upload(Authentication auth, Long id, MultipartFile[] files) throws IOException {
//        Task task = get(auth, id);
//
//        if (files.length > 3) throw new RuntimeException("Max 3 files allowed");
//
//        Path dir = Paths.get("uploads");
//        if (!Files.exists(dir)) Files.createDirectories(dir);
//
//        List<String> names = new ArrayList<>();
//        for (MultipartFile f : files) {
//            String original = f.getOriginalFilename() == null ? "file.pdf" : f.getOriginalFilename();
//            if (!original.toLowerCase().endsWith(".pdf"))
//                throw new RuntimeException("Only PDFs allowed");
//            String newName = System.currentTimeMillis() + "_" + original;
//            Files.copy(f.getInputStream(), dir.resolve(newName), StandardCopyOption.REPLACE_EXISTING);
//            names.add(newName);
//        }
//
//        task.setDocuments(names);
//        return toDTO(taskRepository.save(task));
//    }

//    public TaskDTO upload(Authentication auth, Long id, MultipartFile[] files) throws IOException {
//        boolean isAdmin = auth.getAuthorities().stream()
//                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
//        if (!isAdmin) {
//            throw new AccessDeniedException("Only admins can upload files");
//        }
//
//        Task task = taskRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Task not found"));
//
//        if (files.length > 3) throw new RuntimeException("Max 3 files allowed");
//
//        Path dir = Paths.get("uploads");
//        if (!Files.exists(dir)) Files.createDirectories(dir);
//
//        List<String> names = new ArrayList<>();
//        for (MultipartFile f : files) {
//            String original = f.getOriginalFilename() == null ? "file.pdf" : f.getOriginalFilename();
//            if (!original.toLowerCase().endsWith(".pdf"))
//                throw new RuntimeException("Only PDFs allowed");
//            String newName = System.currentTimeMillis() + "_" + original;
//            Files.copy(f.getInputStream(), dir.resolve(newName), StandardCopyOption.REPLACE_EXISTING);
//            names.add(newName);
//        }
//
//        task.setDocuments(names);
//        return toDTO(taskRepository.save(task));
//    }


//    public TaskDTO upload(Authentication auth, Long id, MultipartFile[] files) throws IOException {
//        // ✅ Identify current user
//        User currentUser = userRepository.findByEmail(auth.getName())
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        // ✅ Find task
//        Task task = taskRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Task not found"));
//
//        // ✅ Check permissions — allow Admin or assigned user
//        boolean isAdmin = auth.getAuthorities().stream()
//                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
//        boolean isAssignedUser = task.getAssignedTo() != null &&
//                task.getAssignedTo().getId().equals(currentUser.getId());
//
//        if (!isAdmin && !isAssignedUser) {
//            throw new AccessDeniedException("You are not authorized to upload files for this task");
//        }
//
//        // ✅ Validate files
//        if (files.length > 3)
//            throw new RuntimeException("You can upload a maximum of 3 files only");
//
//        Path dir = Paths.get("uploads");
//        if (!Files.exists(dir)) Files.createDirectories(dir);
//
//        List<String> names = new ArrayList<>();
//        for (MultipartFile file : files) {
//            String original = file.getOriginalFilename() == null ? "file.pdf" : file.getOriginalFilename();
//            if (!original.toLowerCase().endsWith(".pdf"))
//                throw new RuntimeException("Only PDF files are allowed");
//
//            String newName = System.currentTimeMillis() + "_" + original;
//            Files.copy(file.getInputStream(), dir.resolve(newName), StandardCopyOption.REPLACE_EXISTING);
//            names.add(newName);
//        }
//
//        task.setDocuments(names);
//        return toDTO(taskRepository.save(task));
//    }


    public TaskDTO upload(Authentication auth, Long id, MultipartFile[] files) throws IOException {
        Task task = get(auth, id); // keeps your existing auth/ownership checks

        if (files == null || files.length == 0) {
            throw new RuntimeException("No files provided");
        }
        if (files.length > 3) {
            throw new RuntimeException("Max 3 files allowed per upload");
        }

        Path dir = Paths.get("uploads");
        if (!Files.exists(dir)) Files.createDirectories(dir);

        // Load existing docs (may be null)
        List<String> existing = task.getDocuments() != null
                ? new ArrayList<>(task.getDocuments())
                : new ArrayList<>();

        // How many more can we accept overall?
        int remainingSlots = Math.max(0, 3 - existing.size());
        if (remainingSlots == 0) {
            throw new RuntimeException("This task already has 3 files attached");
        }

        // Save at most 'remainingSlots' new files and collect their names
        List<String> newNames = new ArrayList<>();
        for (MultipartFile f : files) {
            if (newNames.size() >= remainingSlots) break;

            String original = (f.getOriginalFilename() == null) ? "file.pdf" : f.getOriginalFilename();
            if (!original.toLowerCase().endsWith(".pdf")) {
                throw new RuntimeException("Only PDFs allowed");
            }

            String savedName = System.currentTimeMillis() + "_" + original;
            Files.copy(f.getInputStream(), dir.resolve(savedName), StandardCopyOption.REPLACE_EXISTING);
            newNames.add(savedName);
        }

        // Merge (preserve existing, append new)
        existing.addAll(newNames);
        task.setDocuments(existing);

        return toDTO(taskRepository.save(task));
    }


    // ✅ Mapper
    private TaskDTO toDTO(Task t) {
        return new TaskDTO(
                t.getId(),
                t.getTitle(),
                t.getDescription(),
                t.getStatus(),
                t.getPriority(),
                t.getDueDate(),
                t.getAssignedTo() != null ? t.getAssignedTo().getId() : null,
                t.getDocuments()
        );
    }
}


