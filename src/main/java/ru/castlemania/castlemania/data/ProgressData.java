package ru.castlemania.castlemania.data;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.castlemania.castlemania.model.Course;

//Получаем на фронт в резальтате ответа нв вопросы
@Data
@AllArgsConstructor
@JsonPropertyOrder({ "progress", "experience", "course"})
public class ProgressData {
    CourseData course;
    Double progress;
    Integer experience;
}
