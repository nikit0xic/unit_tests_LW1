package ru.castlemania.castlemania.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users",
        uniqueConstraints ={
                @UniqueConstraint(columnNames = "login"),
                @UniqueConstraint(columnNames = "email")
        })
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotBlank
    @Size(max=20)
    @Column(unique = true)
    private String login;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guild_id")
    @JsonBackReference
    private Guild guild;

    @NotBlank
    @Size(max=50)
    @Email
    private String email;

    @NotBlank
    @Size(max = 120)
    private String password;

    @Column(name = "exp", columnDefinition = "integer default 0", nullable = false)
    private Integer experience;

    @Size(max = 100)
    @Column(name = "lvl", columnDefinition = "integer default 0", nullable = false)
    private Integer lvl;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(	name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY,      cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinTable(	name = "student_courses",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id"))
    @OrderBy(value = "name")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<Course> courses = new ArrayList<>();

    @Column
    private long lastOnline;

    @ManyToOne
    @JoinColumn(name = "location_chapter_id")
    private CourseChapter locationChapter;

    public User(Long id, String login, String email, String password, Set<Role> roles, List<Course> courses){
        this.id=id;
        this.login=login;
        this.email=email;
        this.password=password;
        this.roles=roles;
        this.courses=courses;
    }

    public void addCourse(Course course) {
        this.courses.add(course);
        course.getUsers().add(this);
    }

    public void setGuild(Guild guild){
        this.guild=guild;
    }

    public void removeCourse(long courseId) {
        Course course = this.courses.stream().filter(c -> c.getId() == courseId).findFirst().orElse(null);
        if (course != null) {
            this.courses.remove(course);
            course.getUsers().remove(this);
        }
    }

    @PrePersist
    public void setDefaultExperience() {
        if (experience == null) {
            experience = 0;
        }

        if (lvl == null) {
            lvl = 0;
        }
    }

}
