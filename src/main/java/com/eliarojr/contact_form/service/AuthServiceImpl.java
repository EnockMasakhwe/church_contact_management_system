package com.eliarojr.contact_form.service;

import com.eliarojr.contact_form.dto.AuthRequest;
import com.eliarojr.contact_form.dto.AuthResponse;
import com.eliarojr.contact_form.dto.RegisterRequest;
import com.eliarojr.contact_form.entity.Role;
import com.eliarojr.contact_form.entity.User;
import com.eliarojr.contact_form.repository.UserRepository;
import com.eliarojr.contact_form.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Override
    public String register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("User already exits!");
        }

        User user =  new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ROLE_USER);
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);

        return "User registered successfully!";
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail());
                //.orElseThrow(() -> new RuntimeException("User not found"));

        //Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        //Generate JWT
        String token = jwtService.generateToken(user.getEmail());

        //Build response
        AuthResponse response = new AuthResponse();
        response.setUsername(user.getUsername());
        response.setToken(token);
        response.setRole(user.getRole().name());

        return response;
    }
}
