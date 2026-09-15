package com.loginapp.controller;

import com.loginapp.entity.User;
import com.loginapp.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    // -----------------------------------------------------------------
    // Login
    // -----------------------------------------------------------------
    @Test
    void loginPage_rendersLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginForm"));
    }

    @Test
    void login_redirectsToWelcome_onValidCredentials() throws Exception {
        User user = new User("akash", "akash@example.com", "hashed");
        when(userService.authenticate("akash", "secret123")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/login")
                        .param("username", "akash")
                        .param("password", "secret123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/welcome"));
    }

    @Test
    void login_showsError_onInvalidCredentials() throws Exception {
        when(userService.authenticate(anyString(), anyString())).thenReturn(Optional.empty());

        mockMvc.perform(post("/login")
                        .param("username", "akash")
                        .param("password", "wrong"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginError"));
    }

    @Test
    void login_returnsFormWithErrors_whenFieldsBlank() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "")
                        .param("password", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeHasFieldErrors("loginForm", "username", "password"));
    }

    // -----------------------------------------------------------------
    // Register
    // -----------------------------------------------------------------
    @Test
    void registerPage_rendersRegisterView() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registerForm"));
    }

    @Test
    void register_redirectsToLogin_onSuccess() throws Exception {
        when(userService.usernameExists("akash")).thenReturn(false);
        when(userService.emailExists("akash@example.com")).thenReturn(false);

        mockMvc.perform(post("/register")
                        .param("username", "akash")
                        .param("email", "akash@example.com")
                        .param("password", "secret123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("registerSuccess"));
    }

    @Test
    void register_rejectsDuplicateUsername() throws Exception {
        when(userService.usernameExists("akash")).thenReturn(true);

        mockMvc.perform(post("/register")
                        .param("username", "akash")
                        .param("email", "new@example.com")
                        .param("password", "secret123"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("registerForm", "username"));

        verify(userService, never()).register(any());
    }

    @Test
    void register_rejectsInvalidEmail() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "akash")
                        .param("email", "not-an-email")
                        .param("password", "secret123"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("registerForm", "email"));
    }

    // -----------------------------------------------------------------
    // Welcome / logout — session guarding
    // -----------------------------------------------------------------
    @Test
    void welcome_rendersUsername_whenSessionPresent() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", "akash");

        mockMvc.perform(get("/welcome").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("welcome"))
                .andExpect(model().attribute("username", "akash"));
    }

    @Test
    void welcome_redirectsToLogin_whenNotSignedIn() throws Exception {
        mockMvc.perform(get("/welcome"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void logout_invalidatesSessionAndRedirects() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", "akash");

        mockMvc.perform(get("/logout").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void root_redirectsToLogin_whenNotSignedIn() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
