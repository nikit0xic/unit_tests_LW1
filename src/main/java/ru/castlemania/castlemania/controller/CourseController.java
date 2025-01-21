package ru.castlemania.castlemania.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.castlemania.castlemania.data.*;
import ru.castlemania.castlemania.model.Course;
import ru.castlemania.castlemania.model.User;
import ru.castlemania.castlemania.service.CourseService;
import ru.castlemania.castlemania.service.StudentService;
import ru.castlemania.castlemania.service.security.UserDetailsServiceImpl;

import java.util.List;

@RequestMapping(value = "/course")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@AllArgsConstructor
@Slf4j
public class CourseController {

    private CourseService courseService;

    private StudentService studentService;
    private UserDetailsServiceImpl userDetailsService;

    @GetMapping
    public ResponseEntity<List<Course>> getNCourses(@RequestParam int count, @RequestParam int offset) {
        User user = userDetailsService.getCurrentUser();
        log.info("User: '"+user.getLogin()+"' executed /course/?count="+count+"&offset="+offset);
        return ResponseEntity.ok(courseService.getNCourses(count,offset));
    }

    @PostMapping
    public ResponseEntity<Course> saveCourse(@RequestBody Course course){
        User user = userDetailsService.getCurrentUser();
        log.info("User: '"+user.getLogin()+"' executed /course/");
        return ResponseEntity.ok(courseService.createNewCourse(course));
    }


    @GetMapping("{courseId}/chapter/{chapterId}")
    public ResponseEntity<List<CourseItemData>> getChapterContent(@PathVariable String chapterId, @PathVariable String courseId) {
        User user = userDetailsService.getCurrentUser();
        log.info("User: '"+user.getLogin()+"' executed /course/"+chapterId+"/chapter/"+courseId);
        return ResponseEntity.ok(courseService.getChapterContent(Long.parseLong(chapterId), user));
    }

    @GetMapping("/info/{infoId}")
    public ResponseEntity<UpdateProgressData> updateProgress(@PathVariable Long infoId){
        UpdateProgressData updateProgressData = null;
        try {
            User user = userDetailsService.getCurrentUser();
            log.info("User: '"+user.getLogin()+"' executed /course/info/"+infoId);
            updateProgressData = studentService.updateInfoProgress(user,infoId);
        }catch (EntityNotFoundException e){
            log.error(e.toString());
          return  ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updateProgressData);
    }

    @PostMapping(value = "/question")
    public ResponseEntity<UpdateProgressData> setAnswer(@RequestBody QuestionAnswerData questionAnswerData){
        UpdateProgressData updateProgressData = null;
        User user;
        try {
            user = userDetailsService.getCurrentUser();
            log.info("User: '"+user.getLogin()+"' executed /course/question");
            updateProgressData = studentService.setQuestionAnswer(user, questionAnswerData);
        }catch (EntityNotFoundException e){
            log.error("Unknown user executed /course/question and caused "+e.toString());
          return  ResponseEntity.notFound().build();
        }catch (IllegalStateException e){
            log.error("Unknown user executed /course/question and caused "+e.toString());
            return ResponseEntity.internalServerError().build();
        }
      return ResponseEntity.ok(updateProgressData);
    }

    @GetMapping("/week-progress/{courseId}")
    public ResponseEntity<List<ProgressChapter>> getProgressOfChaptersInCourse(@PathVariable Long courseId){
        try {
            User user = userDetailsService.getCurrentUser();
            log.info("User: '"+user.getLogin()+"' executed /course/week-progress/"+courseId);
            return ResponseEntity.ok(courseService.getCourseChapterProgress(courseId, user));
        }catch (EntityNotFoundException e){
            log.error("Unknown user executed /course/week-progress/"+courseId+" and caused "+e.toString());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<CourseData> getCourse(@PathVariable Long courseId){
        try {
            User user = userDetailsService.getCurrentUser();
            log.info("User: '"+user.getLogin()+"' executed /course/"+courseId);
            return ResponseEntity.ok(studentService.getStudentCourseData(courseId, user));
        }catch (EntityNotFoundException e){
            log.error("Unknown user executed /course/"+courseId+" and caused"+e.toString());
            return ResponseEntity.notFound().build();
        }
    }

}