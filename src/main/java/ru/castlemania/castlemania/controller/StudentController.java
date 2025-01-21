package ru.castlemania.castlemania.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.castlemania.castlemania.data.ProgressData;
import ru.castlemania.castlemania.model.*;
import ru.castlemania.castlemania.repository.RoleRepository;
import ru.castlemania.castlemania.service.CourseService;
import ru.castlemania.castlemania.service.StudentService;
import ru.castlemania.castlemania.service.security.UserDetailsServiceImpl;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RequestMapping(value = "/student")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@AllArgsConstructor
@Slf4j
public class StudentController {

    RoleRepository roleRepository;
    CourseService courseService;
    StudentService service;
    UserDetailsServiceImpl userDetailsService;

    @GetMapping("/ping/guildId")
    public ResponseEntity<?> getUserGuildIfHas(){
        User user = userDetailsService.getCurrentUser();
        log.info("User: '"+user.getLogin()+"' executed /student/ping/guildId");
        Guild guild = user.getGuild();
        if (guild==null)
            return ResponseEntity.ok("null");
        return ResponseEntity.ok(user.getGuild().getId());
    }


    @GetMapping("/course")
    public ResponseEntity<List<ProgressData>> getNStudentCourses(@RequestParam int count, @RequestParam int offset) {
        User user = userDetailsService.getCurrentUser();
        log.info("User: '"+user.getLogin()+"' executed /student/course/?count="+count+"&offset="+offset);
        return ResponseEntity.ok(service.getNStudentCourses(offset,count, user));
    }

    @PostMapping(value = "/course", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Course> enrollInACourse(@RequestParam Long courseId){
        User user = userDetailsService.getCurrentUser();
        log.info("User: '"+user.getLogin()+"' executed /student/course/?courseId="+courseId);
        Course course;
        try{
          course = service.enrollInACourse(user,courseId);
        }catch (EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(course);
    }


    @GetMapping("/ping")
    public ResponseEntity<Object> ping(){
        User user = userDetailsService.getCurrentUser();
        log.info("User: '"+user.getLogin()+"' executed /student/ping/");
        service.updateOnline(user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/ping/leader")
    public Boolean pingHasLeader(){
        User user = userDetailsService.getCurrentUser();
        log.info("User: '"+user.getLogin()+"' executed /ping/leader/");
        Role leager = roleRepository.findByName(ERole.ROLE_GUILD_LEADER).orElseThrow(() -> new RuntimeException("Error: Role "+ERole.ROLE_USER+" in database not found."));
        Set<Role> userRoles = user.getRoles();
        return userRoles.contains(leager);
    }

}
