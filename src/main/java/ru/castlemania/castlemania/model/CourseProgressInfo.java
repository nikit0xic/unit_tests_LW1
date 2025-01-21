package ru.castlemania.castlemania.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "course_progresses_info")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CourseProgressInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;


    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    User user;
    Boolean passed;

    @JoinColumn(name = "info_id", nullable = false)
    @ManyToOne
    CourseInfo info;
}
