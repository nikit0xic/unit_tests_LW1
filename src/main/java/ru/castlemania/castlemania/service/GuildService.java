package ru.castlemania.castlemania.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.castlemania.castlemania.data.GuildResponse;
import ru.castlemania.castlemania.data.LocationData;
import ru.castlemania.castlemania.data.UserData;
import ru.castlemania.castlemania.model.*;
import ru.castlemania.castlemania.repository.GuildRepository;
import ru.castlemania.castlemania.repository.RoleRepository;
import ru.castlemania.castlemania.repository.UserRepository;

import javax.validation.ValidationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
@Slf4j
public class GuildService {

    UserRepository userRepository;
    GuildRepository guildRepository;
    RoleRepository roleRepository;

    public Guild createNewGuild(User user, String tagName) throws ValidationException {
        Guild guild = guildRepository.findGuildsByTagName(tagName);

        if (guild != null){
            log.warn("Иницатор: '"+user.getLogin()+ "'. Ошибка: " +"Гильдия с таким названием уже существует!");
            throw new ValidationException("Гильдия с таким названием уже существует!");
        }

        guild = guildRepository.findGuildByLeader(user);


        if (user.getGuild() != null ){
            log.warn("Иницатор: '"+user.getLogin()+ "'. Ошикбка: " +"Этот пользователь уже состоит в одной гильдией и не может стать главой еще одной!");
            throw new ValidationException("Этот пользователь уже состоит в одной гильдией и не может стать главой еще одной!");
        }

        guild = guildRepository.save(new Guild(user,tagName));
        user.setGuild(guild);
        setLeaderRole(user);
        userRepository.save(user);
        return guild;
    }

    public void setLeaderRole(User user){
        if (user==null)
            return;
        Set<Role> roles = user.getRoles();
        Role leaderRole = roleRepository.findByName(ERole.ROLE_GUILD_LEADER).orElseThrow(() -> new RuntimeException("Error: Role '"+ERole.ROLE_GUILD_LEADER+"' in database not found."));
        roles.add(leaderRole);

        user.setRoles(roles);
        userRepository.save(user);
    }

    public void unsetLeaderRole(User user){
        if (user==null)
            return;
        Set<Role> roles = user.getRoles();
        Role leaderRole = roleRepository.findByName(ERole.ROLE_GUILD_LEADER).orElseThrow(() -> new RuntimeException("Error: Role '"+ERole.ROLE_GUILD_LEADER+"' in database not found."));

        if (roles.contains(leaderRole)){
            roles.remove(leaderRole);
            user.setRoles(roles);
            userRepository.save(user);
        }

        log.info("User " + user.getLogin() + "hasn't got role " + ERole.ROLE_GUILD_LEADER);
    }

    public void subscribeToGuild(User user, Long id) throws ValidationException {
        if (user.getGuild() != null){
            log.error("Иницатор: '"+user.getLogin()+ "'. Ошибка: " +"Нельзя добавить человека в гильдию если он уже состоит в другой!");
            throw new ValidationException("Нельзя добавить человека в гильдию если он уже состоит в другой!");
        }

        Guild guild = guildRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        if (guild.getUsers().isEmpty()){
            setLeaderRole(user);
            guild.setLeader(user);
            guildRepository.save(guild);
        }
        user.setGuild(guild);
        userRepository.save(user);
    }


    /**
     *
     * @param user
     * @param id
     * @return 0 - чел отписался
     *          -1 - гильдия удалилась
     *          число больше нуля - лидер переназначен
     * @throws ValidationException
     */
    public long unsubscribeToGuild(User user, Long id) throws ValidationException {
        Guild guild = user.getGuild();
        if (guild==null || !guild.getId().equals(id)){
            log.error("Пользователь "+user.getLogin()+ " не состоит в гильдии с id="+id);
            throw new ValidationException("Пользователь "+user.getLogin()+ " не состоит в гильдии с id="+id);
        }


        user.setGuild(null);
        unsetLeaderRole(user);
        userRepository.save(user);

        if (guild.getUsers().isEmpty()) {
            guildRepository.delete(guild);
            return -1;
        }

        if (guild.getLeader().getId().equals(user.getId())){
            User newLeader = userRepository.findFirstByGuildOrderByExperienceDesc(guild);
            guild.setLeader(newLeader);
            setLeaderRole(newLeader);
            guildRepository.save(guild);
            return newLeader.getId();
        }

        return 0;

    }

   public List<GuildResponse> getGuildList(String orderBy){
        List<Guild> guilds;
        List<GuildResponse> guildsResponse = new ArrayList<>();
        switch (orderBy) {
           case "name" -> guilds = guildRepository.findGuildsByOrderByTagName();
           case "score" -> guilds =  guildRepository.findAllByOrderByGuildScoreDesc();
           default -> throw new IllegalArgumentException("Некорректное поле для сортировки");
       };

       for(Guild guild: guilds)
           guildsResponse.add(getGuild(guild));
       return guildsResponse;
    }

    public GuildResponse getGuild(Long id){
        Guild guild =  guildRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        return getGuild(guild);
    }

    private GuildResponse getGuild(Guild guild){
        User leader = guild.getLeader();
        List<UserData> usersData = new ArrayList<>();
        for(User user: guild.getUsers())
            usersData.add(getUserData(user));

        return new GuildResponse(guild.getId(),guild.getTagName(),getUserData(leader),guild.getGuildScore(),computeLvlByScore(guild.getGuildScore()),usersData,guild.getDescription());
    }

    private LocationData getLocationData(CourseChapter chapter){
        if (chapter==null)
            return null;
        return new LocationData(chapter.getId(), chapter.getTitle());
    }

    private UserData getUserData(User user){
        if (user==null)
            return null;
        Set<Role> roles = user.getRoles();
        List<String> rolesList = new ArrayList<>();
        if (roles!=null)
            rolesList.addAll(roles.stream().map(r->r.getName().toString()).toList());
        return new UserData(user.getId(),user.getLogin(),user.getEmail(),user.getExperience(),user.getLvl(),rolesList,user.getLastOnline(),getLocationData(user.getLocationChapter()));
    }

    void updateGuildScore(Guild guild, Integer score){
        guild.setGuildScore(guild.getGuildScore()+score);
        guildRepository.save(guild);
    }

    private Integer computeLvlByScore(Integer score){
        return Math.min(score/10,10);
    }

    public Guild editGuildDescription(User user, String description){
        Guild guild = guildRepository.findGuildByLeader(user);
        guild.setDescription(description);
        guildRepository.save(guild);
        return guild;
    }

}
