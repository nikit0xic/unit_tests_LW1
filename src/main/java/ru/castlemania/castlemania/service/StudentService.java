package ru.castlemania.castlemania.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.castlemania.castlemania.data.CourseData;
import ru.castlemania.castlemania.data.ProgressData;
import ru.castlemania.castlemania.data.QuestionAnswerData;
import ru.castlemania.castlemania.data.UpdateProgressData;
import ru.castlemania.castlemania.model.*;
import ru.castlemania.castlemania.repository.*;

import java.util.List;

@Service
@AllArgsConstructor
public class StudentService {

    private final CourseQuestionRepository courseQuestionRepository;
    private CourseInfoRepository courseInfoRepository;
    private final CourseRepository courseRepository;
    private CourseProgressInfoRepository courseProgressInfoRepository;
    private CourseService courseService;
    private CourseProgressQuestionRepository courseProgressQuestionRepository;
    private CourseQuestionVariantRepository courseQuestionVariantRepository;
    private GuildService guildService;
    private GuildRepository guildRepository;
    private UserRepository userRepository;


    public Integer getStudentExperience(User user){
        List<CourseProgressQuestion> progressList = courseProgressQuestionRepository.findCourseProgressQuestionByUserAndPassedTrue(user);
        List<CourseQuestion> questions = progressList.stream().map(CourseProgressQuestion::getQuestion).toList();
        return questions.stream().map(question->question.getType().getExperience()).reduce(0, Integer::sum);
    }

    public List<ProgressData> getNStudentCourses(int offset, int limit, User user){
        List<Course> courses = user.getCourses();

        return courses.stream()
                .map(course -> new ProgressData(courseService.getCourseData(course,user),courseService.getStudentProgressByCourse(user, course),courseService.getStudentExperienceByCourse(user,course)))
                .skip(offset).limit(limit).toList();
    }

    public CourseData getStudentCourseData(Long courseId, User user){
        Course course = courseRepository.findById(courseId).orElseThrow(EntityNotFoundException::new);
        return courseService.getCourseData(course,user);
    }

    public UpdateProgressData setQuestionAnswer(User user, QuestionAnswerData questionAnswerData) {

        CourseQuestion question = courseQuestionRepository.findById(questionAnswerData.getQuestionId()).orElseThrow(EntityNotFoundException::new);
        CourseProgressQuestion progress = courseProgressQuestionRepository.findCourseProgressQuestionsByUserAndQuestion(user, question);

        if (progress == null)
            progress = new CourseProgressQuestion(0L, user, false, question, question.getTryNumber());
        else if (progress.getTryNumber() == 0)
            throw new IllegalStateException("Попытки закончились");

        progress.setTryNumber(progress.getTryNumber() - 1);
        CourseQuestionType type = question.getType();
        Guild guild = user.getGuild();
        switch (type.getType()) {
            case SINGLE -> {
                if (questionAnswerData.getVariantIds().size() != 1) {
                    courseProgressQuestionRepository.save(progress);
                } else {
                    CourseQuestionVariant variant = courseQuestionVariantRepository.findById(questionAnswerData.getVariantIds().get(0)).orElseThrow(EntityNotFoundException::new);
                    if (variant.getIsTrue()) {
                        progress.setPassed(true);
                        if (guild!=null)
                            guildService.updateGuildScore(guild, type.getExperience());
                    }
                    courseProgressQuestionRepository.save(progress);
                }
            }
            case PLURAL -> {
                if (questionAnswerData.getVariantIds().isEmpty()){
                    courseProgressQuestionRepository.save(progress);
                } else {
                    List<CourseQuestionVariant> questionVariants = question.getVariants();
                    if (questionVariants.stream().filter(v ->
                            !(v.getIsTrue() && questionAnswerData.getVariantIds().contains(v.getId())
                                    || !v.getIsTrue() && !questionAnswerData.getVariantIds().contains(v.getId()))
                    ).toList().isEmpty()
                    ) {
                        progress.setPassed(true);
                        if (guild!=null)
                            guildService.updateGuildScore(guild, type.getExperience());
                    }
                    courseProgressQuestionRepository.save(progress);
                }
            }
            case OPEN -> {
                List<CourseQuestionVariant> variants = question.getVariants();
                if (!variants.isEmpty() && variants.get(0).getContent().equals(questionAnswerData.getOpenAnswer())) {
                    progress.setPassed(true);
                    if (guild!=null)
                        guildService.updateGuildScore(guild, type.getExperience());
                }
                courseProgressQuestionRepository.save(progress);
            }
            default -> throw new IllegalStateException("Некорректный тип вопроса");
        }
        Course course = question.getChapter().getCourse();

        Integer exp = getStudentExperience(user);
        user.setExperience(exp);
        user.setLvl(computeLvlByScore(exp));
        userRepository.save(user);
        if (guild!=null) {
            guildService.unsetLeaderRole(guild.getLeader());
            User first = userRepository.findFirstByGuildOrderByExperienceDesc(guild);
            guild.setLeader(first);
            guildService.setLeaderRole(first);
            guildRepository.save(guild);
        }
        return new UpdateProgressData(progress.getPassed(), progress.getTryNumber(), courseService.getStudentProgressByCourse(user,course), courseService.getStudentExperienceByCourse(user,course));
    }

    public UpdateProgressData updateInfoProgress(User user, Long courseInfoId){
        CourseInfo info = courseInfoRepository.findById(courseInfoId).orElseThrow(EntityNotFoundException::new);
        CourseProgressInfo cpi = courseProgressInfoRepository.findCourseProgressInfoByUserAndInfo(user,info);
        if (cpi==null)
            cpi = courseProgressInfoRepository.save(new CourseProgressInfo(0L,user,true,info));
        else {
            cpi.setPassed(true);
            courseProgressInfoRepository.save(cpi);
        }
        Course course = info.getChapter().getCourse();
        Integer exp = getStudentExperience(user); // Не уверен что здесь это все нужно вообще
        user.setExperience(exp);
        user.setLvl(computeLvlByScore(exp));
        userRepository.save(user);
        return new UpdateProgressData(cpi.getPassed(), 0, courseService.getStudentProgressByCourse(user,course), courseService.getStudentExperienceByCourse(user,course));

    }

    private Integer computeLvlByScore(Integer score){
        return Math.min(score/10,10);
    }

    public Course enrollInACourse(User user, Long courseId){
        Course course =  courseRepository.findById(courseId).orElseThrow(EntityNotFoundException::new);
        user.addCourse(course);
        course = courseRepository.save(course);
        return course;
    }

    public void updateOnline(User user){
        user.setLastOnline(System.currentTimeMillis());
        userRepository.save(user);
    }

}
