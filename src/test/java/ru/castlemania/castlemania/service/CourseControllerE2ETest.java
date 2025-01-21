package ru.castlemania.castlemania.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import ru.castlemania.castlemania.model.Course;
import ru.castlemania.castlemania.model.User;
import ru.castlemania.castlemania.repository.CourseRepository;
import ru.castlemania.castlemania.repository.UserRepository;

import static java.lang.reflect.Array.get;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class CourseControllerE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @BeforeEach
    void setUp() {
        // Создаем тестового пользователя
        User testUser = new User();
        testUser.setLogin("john_doe");
        testUser.setEmail("john_doe@example.com");
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
    void shouldReturnPaginatedCourses() throws Exception {
        mockMvc.perform(get("/course")
                        .param("count", "3")
                        .param("offset", "0")
                        .header("Authorization", "Bearer test-token")) // JWT токен для аутентификации
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("Course 1"))
                .andExpect(jsonPath("$[1].name").value("Course 2"))
                .andExpect(jsonPath("$[2].name").value("Course 3"));
    }

    @Test
    void shouldReturnStudentProgressForCourses() throws Exception {
        // Создаем курсы и связываем с пользователем
        User testUser = userRepository.findByLogin("john_doe").orElseThrow();
        for (int i = 1; i <= 3; i++) {
            Course course = courseRepository.findByName("Course " + i);
            testUser.addCourse(course);
        }
        userRepository.save(testUser);

        mockMvc.perform(get("/student/course")
                        .param("count", "2")
                        .param("offset", "0")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2)) // Проверяем, что вернулись только 2 курса
                .andExpect(jsonPath("$[0].progress").value(0.0)) // Прогресс по первому курсу
                .andExpect(jsonPath("$[1].progress").value(0.0)); // Прогресс по второму курсу
    }

    @Test
    void shouldReturnBadRequestForInvalidParams() throws Exception {
        mockMvc.perform(get("/course")
                        .param("count", "-1")
                        .param("offset", "0")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isBadRequest());
    }

}