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
import org.springframework.transaction.annotation.Transactional;
import ru.castlemania.castlemania.model.Course;
import ru.castlemania.castlemania.model.User;
import ru.castlemania.castlemania.service.CourseService;
import ru.castlemania.castlemania.repository.CourseRepository;
import ru.castlemania.castlemania.repository.UserRepository;
import ru.castlemania.castlemania.service.security.UserDetailsServiceImpl;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class StudentCourseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private CourseService courseService;

    @BeforeEach
    void setUp() {
        // Создаем тестового пользователя
        User testUser = new User();
        testUser.setLogin("testuser");
        testUser.setEmail("testuser@example.com");
        testUser.setPassword("password123");

        // Добавляем тестовые курсы
        for (int i = 1; i <= 5; i++) {
            Course course = new Course();
            course.setName("Course " + i);
            course.setDescription("Description for Course " + i);
            courseRepository.save(course);
            testUser.addCourse(course); // Связываем пользователя с курсами
        }

        userRepository.save(testUser);
    }

    @Test
    void shouldReturnNStudentCoursesWithPagination() throws Exception {
        // Устанавливаем тестового пользователя в контекст безопасности
        User testUser = userRepository.findByLogin("testuser").orElseThrow();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(testUser, null));

        // Выполняем GET-запрос
        mockMvc.perform(get("/course")
                        .param("count", "2")
                        .param("offset", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Проверяем, что статус 200
                .andExpect(jsonPath("$.length()").value(2)) // Проверяем, что вернулось 2 курса
                .andExpect(jsonPath("$[0].courseData.name").value("Course 2")) // Проверяем корректность данных
                .andExpect(jsonPath("$[1].courseData.name").value("Course 3"));
    }

    @Test
    void shouldReturnEmptyListForOutOfBoundsPagination() throws Exception {
        // Устанавливаем тестового пользователя в контекст безопасности
        User testUser = userRepository.findByLogin("testuser").orElseThrow();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(testUser, null));

        // Выполняем GET-запрос с оффсетом, выходящим за пределы
        mockMvc.perform(get("/course")
                        .param("count", "2")
                        .param("offset", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Проверяем, что статус 200
                .andExpect(jsonPath("$.length()").value(0)); // Пустой массив
    }

    @Test
    void shouldHandleZeroCoursesForUser() throws Exception {
        // Создаем пользователя без курсов
        User newUser = new User();
        newUser.setLogin("emptyuser");
        newUser.setEmail("emptyuser@example.com");
        newUser.setPassword("password123");
        userRepository.save(newUser);

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(newUser, null));

        // Выполняем GET-запрос
        mockMvc.perform(get("/course")
                        .param("count", "2")
                        .param("offset", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Проверяем, что статус 200
                .andExpect(jsonPath("$.length()").value(0)); // Пустой массив
    }
}
