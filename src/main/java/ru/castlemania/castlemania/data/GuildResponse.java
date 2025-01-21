package ru.castlemania.castlemania.data;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GuildResponse {

    private Long id;
    private String tagName;
    private UserData leader;
    private Integer guildScore;
    private Integer lvl;
    private List<UserData> users;
    private String description;

}
