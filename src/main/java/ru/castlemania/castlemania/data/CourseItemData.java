package ru.castlemania.castlemania.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.castlemania.castlemania.model.CourseQuestionVariant;
import ru.castlemania.castlemania.model.ECourseItemType;
import ru.castlemania.castlemania.model.EQuestionType;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseItemData {
    Long id;
    Integer orderNumber;

    ECourseItemType type;
    EQuestionType typeQuestion;
    String content;
    List<CourseQuestionVariantData> variants;
    String videoLink;
    Integer tryNumber;
    Boolean passed;
}
