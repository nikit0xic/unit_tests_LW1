package ru.castlemania.castlemania.data;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.castlemania.castlemania.model.Role;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserData {
    private Long id;

    private String login;

    private String email;

    private Integer experience;
    private Integer lvl;

    private List<String> roles = new ArrayList<>();

    private long lastOnline;

    private LocationData location;

}
