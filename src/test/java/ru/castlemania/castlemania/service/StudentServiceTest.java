package ru.castlemania.castlemania.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.castlemania.castlemania.data.CourseData;
import ru.castlemania.castlemania.data.ProgressData;
import ru.castlemania.castlemania.model.Course;
import ru.castlemania.castlemania.model.ERole;
import ru.castlemania.castlemania.model.Role;
import ru.castlemania.castlemania.model.User;
import ru.castlemania.castlemania.repository.CourseRepository;
import ru.castlemania.castlemania.repository.UserRepository;


import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StudentServiceTest {

    @Mock
    private CourseRepository courseRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CourseService courseService;
    @InjectMocks
    private StudentService studentService;

    private User testUser;
    private Course testCourse;
    private List<Course> courses;
    private User case3User;

    @BeforeEach
    void setUp(){
        testUser = new User();
        testUser.setId(2L);
        testUser.setLogin("testuser");
        testUser.setEmail("testuser@example.com");
        testUser.setPassword("password123");

        testCourse = new Course();
        testCourse.setId(2L);
        testCourse.setName("Java Basics");

        courses = Arrays.asList(new Course(3L, "Python basics", case3User), new Course(4L, "Kubernetes guide",case3User), new Course(5L, "Oracle Data Modeler",case3User), new Course(6L, "CKA",case3User));

        case3User = new User();
        case3User.setId(4L);
        case3User.setLogin("case3User");
        case3User.setEmail("case3User@example.com");
        case3User.setPassword("qwerty");
        case3User.setCourses(courses);
    }

    @Test
    void shouldAddCourseToUser() {
        when(courseRepository.findById(testCourse.getId())).thenReturn(Optional.of(testCourse));

        studentService.enrollInACourse(testUser, testCourse.getId());

        assertTrue(testCourse.getUsers().contains(testUser));
        verify(courseRepository, times(1)).save(testCourse);
        verify(userRepository, never()).save(any());
    }

    @Test
    public void testGetNStudentCoursesPagination() {
        when(courseService.getCourseData(any(Course.class), eq(case3User))).thenReturn(new CourseData());
        when(courseService.getStudentProgressByCourse(eq(case3User), any(Course.class))).thenReturn(0.0);
        when(courseService.getStudentExperienceByCourse(eq(case3User), any(Course.class))).thenReturn(0);

        List<ProgressData> result = studentService.getNStudentCourses(1, 3, case3User);

        assertEquals(3, result.size());

    }

    @Test
    public void testGetNStudentCoursesEmptyCourses() {
        case3User.setCourses(Arrays.asList());

        List<ProgressData> result = studentService.getNStudentCourses(0, 2, case3User);

        assertTrue(result.isEmpty());
    }
}
