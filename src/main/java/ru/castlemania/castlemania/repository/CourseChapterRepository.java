package ru.castlemania.castlemania.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.castlemania.castlemania.model.Course;
import ru.castlemania.castlemania.model.CourseChapter;

import java.util.List;

@Repository
public interface CourseChapterRepository extends JpaRepository<CourseChapter, Long> {

    List<CourseChapter> findCourseChaptersByCourseOrderByOrderNumber(Course course);
    List<CourseChapter> findCourseChaptersByCourse(Course course);
    List<CourseChapter> findCourseChaptersByCourseAndParentNullOrderByOrderNumber(Course course);


}
