package com.loginapp.service;

import com.loginapp.dto.RegisterForm;
import com.loginapp.entity.User;
import com.loginapp.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public User register(RegisterForm form) {
        User user = new User(
                form.getUsername(),
                form.getEmail(),
                passwordEncoder.encode(form.getPassword())
        );
        return userRepository.save(user);
    }

    /**
     * Returns the matching user if the credentials are valid, or an empty
     * Optional otherwise. Deliberately does not distinguish "no such user"
     * from "wrong password" so the caller can't leak which usernames exist.
     */
    public Optional<User> authenticate(String username, String rawPassword) {
        Optional<User> found = userRepository.findByUsername(username);
        if (found.isEmpty()) {
            return Optional.empty();
        }
        User user = found.get();
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            return Optional.empty();
        }
        return Optional.of(user);
    }
}
