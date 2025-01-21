package ru.castlemania.castlemania.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.castlemania.castlemania.model.Course;

import java.util.Date;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {


    Course findCourseById(Long id);
    List<Course> findCourseByNameLikeIgnoreCase(String name);
    List<Course> findCoursesByStartDateAfter(Date startDate);
    List<Course> findCoursesByEndDateBefore(Date end);
}
