package ru.castlemania.castlemania.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.castlemania.castlemania.model.CourseQuestionType;
import ru.castlemania.castlemania.model.EQuestionType;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionAnswerData {
    List<Long> variantIds; //Если несполько вариантов
    Long questionId; //Отвечаем на какой
    String openAnswer; //Открытый вопрос
}
