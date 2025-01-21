package ru.castlemania.castlemania.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.castlemania.castlemania.model.User;
import ru.castlemania.castlemania.payload.request.LoginRequest;
import ru.castlemania.castlemania.payload.request.SignupRequest;
import ru.castlemania.castlemania.repository.CourseRepository;
import ru.castlemania.castlemania.repository.UserRepository;

import java.util.Optional;

import static java.lang.reflect.Array.get;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.post;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class AuthControllerE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Test
    void shouldFailAuthenticationWithInvalidCredentials() throws Exception {
        // Arrange: создаем тестового пользователя
        User testUser = new User();
        testUser.setLogin("test_user");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setEmail("test_user@example.com");
        userRepository.save(testUser);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setLogin("test_user");
        loginRequest.setPassword("wrong_password");

        // Act & Assert: выполняем запрос с неверными данными
        mockMvc.perform(post("/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setLogin("new_user");
        signupRequest.setPassword("securepassword");
        signupRequest.setEmail("new_user@example.com");

        mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully!"));

        // Проверяем, что пользователь добавлен в базу
        Optional<User> createdUser = userRepository.findByLogin("new_user");
        assertTrue(createdUser.isPresent());
        assertEquals("new_user@example.com", createdUser.get().getEmail());
    }

}
