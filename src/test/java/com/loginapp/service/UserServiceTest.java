package com.loginapp.service;

import com.loginapp.dto.RegisterForm;
import com.loginapp.entity.User;
import com.loginapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User storedUser;

    @BeforeEach
    void setUp() {
        storedUser = new User("akash", "akash@example.com", "hashed-password");
        storedUser.setId(1);
    }

    @Test
    void register_hashesPasswordBeforeSaving() {
        RegisterForm form = new RegisterForm();
        form.setUsername("akash");
        form.setEmail("akash@example.com");
        form.setPassword("plaintext123");

        when(passwordEncoder.encode("plaintext123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.register(form);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        // The raw password must never reach the database.
        assertThat(captor.getValue().getPassword()).isEqualTo("hashed-password");
        assertThat(captor.getValue().getPassword()).isNotEqualTo("plaintext123");
    }

    @Test
    void authenticate_returnsUser_whenCredentialsAreValid() {
        when(userRepository.findByUsername("akash")).thenReturn(Optional.of(storedUser));
        when(passwordEncoder.matches("plaintext123", "hashed-password")).thenReturn(true);

        Optional<User> result = userService.authenticate("akash", "plaintext123");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("akash");
    }

    @Test
    void authenticate_returnsEmpty_whenPasswordIsWrong() {
        when(userRepository.findByUsername("akash")).thenReturn(Optional.of(storedUser));
        when(passwordEncoder.matches("wrongpassword", "hashed-password")).thenReturn(false);

        Optional<User> result = userService.authenticate("akash", "wrongpassword");

        assertThat(result).isEmpty();
    }

    @Test
    void authenticate_returnsEmpty_whenUserDoesNotExist() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        Optional<User> result = userService.authenticate("ghost", "anything");

        assertThat(result).isEmpty();
        // Should short-circuit without ever attempting a password comparison.
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void usernameExists_delegatesToRepository() {
        when(userRepository.existsByUsername("akash")).thenReturn(true);

        assertThat(userService.usernameExists("akash")).isTrue();
        verify(userRepository).existsByUsername("akash");
    }

    @Test
    void emailExists_delegatesToRepository() {
        when(userRepository.existsByEmail("akash@example.com")).thenReturn(true);

        assertThat(userService.emailExists("akash@example.com")).isTrue();
        verify(userRepository).existsByEmail("akash@example.com");
    }
}
