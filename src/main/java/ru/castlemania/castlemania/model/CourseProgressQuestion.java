package ru.castlemania.castlemania.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "course_progresses_question")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CourseProgressQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    User user;
    Boolean passed; //Был ли уже правильный ответ

    @JoinColumn(name = "question_id", nullable = false)
    @ManyToOne
    CourseQuestion question;

    Integer tryNumber; //Кол-во попыток (осталось) для ответа на этот вопрос
}
