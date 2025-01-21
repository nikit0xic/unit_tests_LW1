package ru.castlemania.castlemania.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.castlemania.castlemania.model.CourseChapter;
import ru.castlemania.castlemania.model.CourseInfo;

import java.util.List;

public interface CourseInfoRepository extends JpaRepository<CourseInfo, Long> {

    List<CourseInfo> findCourseInfosByChapterOrderByOrderNumber(CourseChapter courseChapter);

    List<CourseInfo> findCourseInfosByChapterIn(List<CourseChapter> courseChapters);

}
