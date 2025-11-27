package com.groupe.gestion_.de_.notes.security.Authentication;

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

import com.groupe.gestion_.de_.notes.model.User;
import com.groupe.gestion_.de_.notes.repository.UserRepository;
import com.groupe.gestion_.de_.notes.security.Jwt.JwtUtils;
import com.groupe.gestion_.de_.notes.services.ServiceImplementation.LoginRecordsServiceImpl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:4300", "http://localhost:4500"}) // Allow CORS for your Angular app
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final LoginRecordsServiceImpl loginRecordsServiceImpl;
    private final UserRepository userRepository;

    @PostMapping("/signin")
    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        // getting the UserDetails object
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        //fetching the User entity from the database using the username
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(()-> new RuntimeException("User not found after authentication."));

        // Checking for an existing, un-logged-out record and log it out first
        loginRecordsServiceImpl.recordLogout(user.getUsername());

        // calling the service to record the successful login
        String ipAddress = request.getRemoteAddr();
        loginRecordsServiceImpl.recordLogin(user.getUsername(), ipAddress);

        // In a real app, it's a good practice to return user ID, roles, etc
        // Contructing the response using the fetched User entity
        return ResponseEntity.ok(new JwtResponse(jwt,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                userDetails.getAuthorities().stream().map(item -> item.getAuthority()).toList()));
    }
}