package ru.castlemania.castlemania.data;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProgressChapter {
    long chapterId;

    double capacity; //Percent of progress of a week

    double passedInfos; //Percent of passed infos

    double passedQuestions; //Percent of passed questions

}
