package ru.castlemania.castlemania.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.castlemania.castlemania.data.*;
import ru.castlemania.castlemania.model.*;
import ru.castlemania.castlemania.repository.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CourseService {

    private CourseRepository courseRepository;
    private CourseChapterRepository courseChapterRepository;

    private CourseInfoRepository courseInfoRepository;

    private CourseQuestionRepository courseQuestionRepository;

    private UserRepository userRepository;
    private CourseProgressInfoRepository courseProgressInfoRepository;

    private CourseProgressQuestionRepository courseProgressQuestionRepository;

    public CourseService(CourseRepository courseRepository, UserRepository userRepository, CourseChapterRepository courseChapterRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.courseChapterRepository = courseChapterRepository;
    }


    public Course createNewCourse(Course course){
        return courseRepository.save(course);
    }

    public List<Course> getAll(){
        return courseRepository.findAll();
    }

    public List<Course> getNCourses(int limit, int offset){
        Page<Course> courses = courseRepository.findAll(PageRequest.of(offset,limit));
        return courses.stream().limit(limit).collect(Collectors.toList());
    }

    public List<Course> getCoursesByName(String template){
        List<Course> courses = courseRepository.findCourseByNameLikeIgnoreCase(template);
        return courses.stream().toList();
    }


    public double getStudentProgressByCourse(User user, Course course){
        List<CourseChapter> courseChapters = courseChapterRepository.findCourseChaptersByCourse(course);
        List<CourseInfo> courseInfos = courseInfoRepository.findCourseInfosByChapterIn(courseChapters);
        List<CourseQuestion> courseQuestions = courseQuestionRepository.findCourseQuestionsByChapterIn(courseChapters);
        List<CourseProgressInfo> progressList = courseProgressInfoRepository.findCourseProgressInfosByUserAndInfoInAndPassedTrue(user,courseInfos);
        List<CourseProgressQuestion> courseProgressQuestions = courseProgressQuestionRepository.findCourseProgressQuestionsByUserAndQuestionInAndPassedTrue(user,courseQuestions);
        return ((((double) (progressList.size() + courseProgressQuestions.size()) / (courseQuestions.size()+ courseInfos.size())) * 100 ) );
    }

    public int getStudentExperienceByCourse(User user, Course course){
        List<CourseChapter> courseChapters = courseChapterRepository.findCourseChaptersByCourse(course);
        List<CourseQuestion> courseQuestions = courseQuestionRepository.findCourseQuestionsByChapterIn(courseChapters);
        List<CourseProgressQuestion> courseProgressQuestions = courseProgressQuestionRepository.findCourseProgressQuestionsByUserAndQuestionInAndPassedTrue(user,courseQuestions);
        return  courseProgressQuestions.stream().map(cpq->cpq.getQuestion().getType().getExperience()).reduce(0, Integer::sum);
    }

    public Course getCoursesById(Long id){
        return courseRepository.findCourseById(id);
    }

    public List<Course> getCoursesAfterDate(Date start){
        return courseRepository.findCoursesByStartDateAfter(start);
    }
    public List<Course> getCoursesBeforeDate(Date end){
        return courseRepository.findCoursesByEndDateBefore(end);
    }

    List<CourseChapter> getCourseStructure(Long courseId) {
        Course course = courseRepository.findCourseById(courseId);
        return courseChapterRepository.findCourseChaptersByCourseAndParentNullOrderByOrderNumber(course);
    }

    public List<ProgressChapter> getCourseChapterProgress(Long courseId, User user) {

        Course course = courseRepository.findById(courseId).orElseThrow(EntityNotFoundException::new);
        List<CourseChapter> chapters = courseChapterRepository.findCourseChaptersByCourseAndParentNullOrderByOrderNumber(course);
        List<ProgressChapter> data = new ArrayList<>();
        AtomicInteger accumInfos = new AtomicInteger();
        AtomicInteger accumQuestions = new AtomicInteger();

        AtomicInteger accumPassedInfos = new AtomicInteger();
        AtomicInteger accumPassedQuestions = new AtomicInteger();

        chapters.forEach(courseChapter -> {

            courseChapter.getChildren().forEach(child -> accumInfos.addAndGet(child.getInfos().size()));
            courseChapter.getChildren().forEach(child -> accumQuestions.addAndGet(child.getQuestions().size()));

            courseChapter.getChildren().forEach(child -> accumPassedInfos.addAndGet(
                    courseProgressInfoRepository.findCourseProgressInfosByUserAndInfoInAndPassedTrue(
                            user,
                            child.getInfos()
                    ).size()
            ));

            courseChapter.getChildren().forEach(child -> accumPassedQuestions.addAndGet(
                    courseProgressQuestionRepository.findCourseProgressQuestionsByUserAndQuestionInAndPassedTrue(
                            user,
                            child.getQuestions()
                    ).size()
            ));

            data.add(new ProgressChapter(
                    courseChapter.getId(),
                    ((accumInfos.get() + accumQuestions.get()) != 0) ? ( (int) ((double) (accumPassedInfos.get() + accumPassedQuestions.get()) / (accumInfos.get() + accumQuestions.get()) * 100) ) : 0,
                    (accumInfos.get() != 0) ? ( (int) ((double) accumPassedInfos.get() / accumInfos.get() * 100) ) : 0,
                    (accumQuestions.get() != 0) ? ( (int) ((double) accumPassedQuestions.get() / accumQuestions.get() * 100) ) : 0
            ));
            accumInfos.set(0);
            accumQuestions.set(0);
            accumPassedInfos.set(0);
            accumPassedQuestions.set(0);
        });
        return data;
    }

    public List<CourseItemData> getChapterContent(Long chapterId, User user){
        CourseChapter chapter = courseChapterRepository.findById(chapterId).orElseThrow(EntityNotFoundException::new);
        List<CourseInfo> courseInfos = courseInfoRepository.findCourseInfosByChapterOrderByOrderNumber(chapter);
        List<CourseQuestion> courseQuestions = courseQuestionRepository.findCourseQuestionsByChapterOrderByOrderNumber(chapter);

        List<CourseItemData> data = new ArrayList<>();
        courseInfos.forEach(ci-> {
            CourseProgressInfo cpi = courseProgressInfoRepository.findCourseProgressInfoByUserAndInfo(user,ci);
            if (cpi==null)
                cpi = courseProgressInfoRepository.save(new CourseProgressInfo(0L,user,false,ci));
            data.add(new CourseItemData(ci.getId(), ci.getOrderNumber(), ECourseItemType.INFO, null, ci.getContent(), null, ci.getVideoLink(), null, cpi.getPassed()));
        });

        courseQuestions.forEach( cq ->{
            CourseProgressQuestion cpq = courseProgressQuestionRepository.findCourseProgressQuestionsByUserAndQuestion(user,cq);
            if (cpq==null)
                cpq = courseProgressQuestionRepository.save(new CourseProgressQuestion(0L,user,false,cq, cq.getTryNumber()));
            List<CourseQuestionVariant> variants = new ArrayList<>();
            if (cq.getType().getType()!=EQuestionType.OPEN)
                variants.addAll(cq.getVariants());
            data.add(new CourseItemData(cq.getId(), cq.getOrderNumber(), ECourseItemType.QUESTION, cq.getType().getType(), cq.getContent(), variants.stream().map(cqv -> new CourseQuestionVariantData(cqv.getId(),cqv.getContent())).toList(), null,cpq.getTryNumber(), cpq.getPassed()));
        });
        user.setLocationChapter(chapter);
        userRepository.save(user);
        return data.stream().sorted(Comparator.comparingInt(CourseItemData::getOrderNumber)).toList();
    }

    public CourseData getCourseData(Course course, User user) {
        List<CourseChapter> chapters = courseChapterRepository.findCourseChaptersByCourseAndParentNullOrderByOrderNumber(course);
        CourseData courseData = new CourseData(course, chapters);
        courseData.setProgress(getStudentProgressByCourse(user, course));
        courseData.setExperience(getStudentExperienceByCourse(user, course));
        Guild guild = user.getGuild();
        if (guild == null)
            courseData.setGuildId(0L);
        else courseData.setGuildId(guild.getId());
        return courseData;
    }

}
