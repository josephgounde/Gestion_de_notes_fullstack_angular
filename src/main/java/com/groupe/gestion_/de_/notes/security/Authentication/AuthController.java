package com.groupe.gestion_.de_.notes.security.Authentication;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.groupe.gestion_.de_.notes.model.Student;
import com.groupe.gestion_.de_.notes.model.Teacher;
import com.groupe.gestion_.de_.notes.model.User;
import com.groupe.gestion_.de_.notes.repository.StudentRepository;
import com.groupe.gestion_.de_.notes.repository.TeacherRepository;
import com.groupe.gestion_.de_.notes.repository.UserRepository;
import com.groupe.gestion_.de_.notes.security.Jwt.JwtUtils;
import com.groupe.gestion_.de_.notes.services.ServiceImplementation.LoginRecordsServiceImpl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:4300", "http://localhost:4500"})
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final LoginRecordsServiceImpl loginRecordsServiceImpl;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;

    @PostMapping("/signin")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        // Fetch the User entity from the database using the username
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found after authentication."));

        // CRITICAL FIX: Fetch studentIdNum and teacherIdNum based on user ID
        String studentIdNum = null;
        String teacherIdNum = null;

        // Check if user is a student (Student extends User, so we use the user's ID)
        try {
            Optional<Student> studentOptional = studentRepository.findById(user.getId())
                    .map(u -> u instanceof Student ? (Student) u : null);
            if (studentOptional.isPresent() && studentOptional.get() != null) {
                studentIdNum = studentOptional.get().getStudentIdNum();
                System.out.println("✅ Student login - username: " + user.getUsername() + ", studentIdNum: " + studentIdNum);
            }
        } catch (Exception e) {
            // Not a student, continue
        }

        // Check if user is a teacher (Teacher extends User, so we use the user's ID)
        try {
            Optional<Teacher> teacherOptional = teacherRepository.findById(user.getId())
                    .map(u -> u instanceof Teacher ? (Teacher) u : null);
            if (teacherOptional.isPresent() && teacherOptional.get() != null) {
                teacherIdNum = teacherOptional.get().getTeacherIdNum();
                System.out.println("✅ Teacher login - username: " + user.getUsername() + ", teacherIdNum: " + teacherIdNum);
            }
        } catch (Exception e) {
            // Not a teacher, continue
        }

        // Check for an existing, un-logged-out record and log it out first
        loginRecordsServiceImpl.recordLogout(user.getUsername());

        // Record the successful login
        String ipAddress = request.getRemoteAddr();
        loginRecordsServiceImpl.recordLogin(user.getUsername(), ipAddress);

        System.out.println("📤 Returning JWT response - username: " + user.getUsername() + 
                           ", studentIdNum: " + studentIdNum + ", teacherIdNum: " + teacherIdNum);

        return ResponseEntity.ok(new JwtResponse(
                jwt,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roles,
                studentIdNum,
                teacherIdNum));
    }
}