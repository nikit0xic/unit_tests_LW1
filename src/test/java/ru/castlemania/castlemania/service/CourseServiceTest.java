package ru.castlemania.castlemania.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.castlemania.castlemania.data.CourseData;
import ru.castlemania.castlemania.model.*;
import ru.castlemania.castlemania.repository.CourseChapterRepository;
import ru.castlemania.castlemania.repository.CourseRepository;
import ru.castlemania.castlemania.repository.UserRepository;


import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CourseServiceTest {
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CourseChapterRepository courseChapterRepository;

    private CourseService courseService;

    private User testUser;
    private Course testCourse;
    private List<CourseChapter> testChapters;
    private Guild testGuild;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        courseService = spy(new CourseService(courseRepository, userRepository, courseChapterRepository));

        testUser = new User();
        testUser.setId(2L);
        testUser.setLogin("testuser");
        testUser.setEmail("testuser@example.com");
        testUser.setPassword("password123");

        testCourse = new Course();
        testCourse.setId(2L);
        testCourse.setName("Java Basics");
        testCourse.setDescription("Test Description");
        testCourse.setStartDate(new Date());
        testCourse.setEndDate(new Date());

        testChapters = Arrays.asList(
                new CourseChapter(),
                new CourseChapter()
        );

        testGuild = new Guild();
        testGuild.setId(1L);
    }

    @Test
    void shouldGetCourseDataWithGuild() {
        // Given
        testUser.setGuild(testGuild);
        when(courseChapterRepository.findCourseChaptersByCourseAndParentNullOrderByOrderNumber(any(Course.class)))
                .thenReturn(testChapters);
        doReturn(75.0).when(courseService).getStudentProgressByCourse(any(User.class), any(Course.class));
        doReturn(100).when(courseService).getStudentExperienceByCourse(any(User.class), any(Course.class));

        // When
        CourseData result = courseService.getCourseData(testCourse, testUser);

        // Then
        assertNotNull(result);
        assertEquals(testCourse.getId(), result.getId());
        assertEquals(testCourse.getName(), result.getName());
        assertEquals(testCourse.getDescription(), result.getDescription());
        assertEquals(testCourse.getStartDate(), result.getStartDate());
        assertEquals(testCourse.getEndDate(), result.getEndDate());
        assertEquals(testChapters, result.getChapters());
        assertEquals(75.0, result.getProgress());
        assertEquals(100, result.getExperience());
        assertEquals(testGuild.getId(), result.getGuildId());

        verify(courseChapterRepository).findCourseChaptersByCourseAndParentNullOrderByOrderNumber(any(Course.class));
    }

    @Test
    void shouldGetCourseDataWithoutGuild() {
        // Given
        testUser.setGuild(null);
        when(courseChapterRepository.findCourseChaptersByCourseAndParentNullOrderByOrderNumber(any(Course.class)))
                .thenReturn(testChapters);
        doReturn(0.0).when(courseService).getStudentProgressByCourse(any(User.class), any(Course.class));
        doReturn(0).when(courseService).getStudentExperienceByCourse(any(User.class), any(Course.class));

        // When
        CourseData result = courseService.getCourseData(testCourse, testUser);

        // Then
        assertNotNull(result);
        assertEquals(testCourse.getId(), result.getId());
        assertEquals(0L, result.getGuildId());
        assertEquals(0.0, result.getProgress());
        assertEquals(0, result.getExperience());

        verify(courseChapterRepository).findCourseChaptersByCourseAndParentNullOrderByOrderNumber(any(Course.class));
    }
}
