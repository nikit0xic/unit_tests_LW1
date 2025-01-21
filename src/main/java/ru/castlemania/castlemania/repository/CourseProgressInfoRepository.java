package ru.castlemania.castlemania.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.castlemania.castlemania.model.CourseInfo;
import ru.castlemania.castlemania.model.CourseProgressInfo;
import ru.castlemania.castlemania.model.User;

import java.util.List;

@Repository
public interface CourseProgressInfoRepository extends JpaRepository<CourseProgressInfo, Long> {
    List<CourseProgressInfo> findCourseProgressInfosByUserAndInfoIn(User user, List<CourseInfo> infos);

    List<CourseProgressInfo> findCourseProgressInfosByUserAndInfoInAndPassedTrue(User user,  List<CourseInfo> infos);

    CourseProgressInfo findCourseProgressInfoByUserAndInfo(User user, CourseInfo info);
}
