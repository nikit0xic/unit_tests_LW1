package ru.castlemania.castlemania.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.castlemania.castlemania.model.Course;
import ru.castlemania.castlemania.model.CourseChapter;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseData {

    private Long id;
    private String name;
    private Double progress;
    private Integer experience;
    private String description;
    private Date startDate;
    private Date endDate;
    private List<CourseChapter> chapters;
    private Long guildId;


    public CourseData(Course course, List<CourseChapter> chapters){
        this.id = course.getId();
        this.name = course.getName();
        this.description = course.getDescription();
        this.startDate  = course.getStartDate();
        this.endDate = course.getEndDate();
        this.chapters = chapters;
    }


}
