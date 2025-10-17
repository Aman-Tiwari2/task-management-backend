//package com.example.taskmanager.service;
//
//import com.example.taskmanager.dto.UserDTO;
//import com.example.taskmanager.model.Role;
//import com.example.taskmanager.model.User;
//import com.example.taskmanager.repository.UserRepository;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//public class UserService {
//
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
//        this.userRepository = userRepository;
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    public List<UserDTO> list() {
//        return userRepository.findAll().stream()
//                .map(u -> new UserDTO(u.getId(), u.getName(), u.getEmail(), u.getRole()))
//                .toList();
//    }
//
//    public UserDTO create(String email, String password, Role role) {
//        User u = new User();
//        u.setEmail(email);
//        u.setPassword(passwordEncoder.encode(password));
//        u.setRole(role == null ? Role.USER : role);
//        u = userRepository.save(u);
//        return new UserDTO(u.getId(), u.getName(), u.getEmail(), u.getRole());
//    }
//}
package com.example.taskmanager.service;

import com.example.taskmanager.model.Role;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Promote an existing user to ADMIN role.
     * Only admins can perform this action.
     */
    public User makeAdmin(Authentication auth, Long userId) {
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            throw new AccessDeniedException("Only admins can promote users");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setRole(Role.ADMIN);
        return userRepository.save(user);
    }
}
