package ru.castlemania.castlemania.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import ru.castlemania.castlemania.model.Course;
import ru.castlemania.castlemania.model.User;
import ru.castlemania.castlemania.repository.CourseRepository;
import ru.castlemania.castlemania.repository.UserRepository;
import ru.castlemania.castlemania.service.security.UserDetailsServiceImpl;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CourseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setUp() {
        // Добавляем тестового пользователя
        User testUser = new User();
        testUser.setLogin("testuser");
        testUser.setEmail("testuser@example.com");
        testUser.setPassword("password123");
        userRepository.save(testUser);

        // Добавляем тестовые курсы
        for (int i = 1; i <= 5; i++) {
            Course course = new Course();
            course.setName("Course " + i);
            course.setDescription("Description for Course " + i);
            courseRepository.save(course);
        }
    }

    @Test
    void shouldReturnNCoursesWithPagination() throws Exception {
        // Устанавливаем тестового пользователя в контекст
        User testUser = userRepository.findByLogin("testuser").orElseThrow();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(testUser, null));

        // Выполняем GET-запрос к эндпоинту
        mockMvc.perform(get("/course")
                        .param("count", "3")
                        .param("offset", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Проверяем, что статус 200
                .andExpect(jsonPath("$.length()").value(3)) // Проверяем, что вернулось 3 курса
                .andExpect(jsonPath("$[0].name").value("Course 1")) // Проверяем, что данные корректны
                .andExpect(jsonPath("$[1].name").value("Course 2"))
                .andExpect(jsonPath("$[2].name").value("Course 3"));
    }

    @Test
    void shouldHandlePaginationOutOfBounds() throws Exception {
        // Выполняем GET-запрос с
        mockMvc.perform(get("/course")
                        .param("count", "3")
                        .param("offset", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Статус 200
                .andExpect(jsonPath("$.length()").value(0)); // Пустой массив
    }
}
