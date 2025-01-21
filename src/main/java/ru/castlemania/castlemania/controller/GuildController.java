package ru.castlemania.castlemania.controller;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.castlemania.castlemania.data.GuildResponse;
import ru.castlemania.castlemania.data.UnsubscribeGuildResponse;
import ru.castlemania.castlemania.model.Guild;
import ru.castlemania.castlemania.model.User;
import ru.castlemania.castlemania.service.GuildService;
import ru.castlemania.castlemania.service.security.UserDetailsServiceImpl;

import javax.validation.ValidationException;
import java.util.List;

@RequestMapping(value = "/guild")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@AllArgsConstructor
@Slf4j
public class GuildController {

        private GuildService guildService;
        private UserDetailsServiceImpl userDetailsService;

        @PostMapping
        public ResponseEntity<?> createGuild(@RequestBody String tagName) throws ValidationException {
                User user;
                try {
                        user = userDetailsService.getCurrentUser();
                        guildService.createNewGuild(user, tagName);
                } catch (EntityNotFoundException e) {
                        log.error("Unknown user executed /guild/"+tagName+" and caused "+e.toString());
                        return ResponseEntity.notFound().build();
                }
                log.info("Гильдия '" + tagName + "' успешно создана пользователем '"+user.getLogin()+"'");
                return ResponseEntity.ok().body("Гильдия " + tagName + " успешно создана!");
        }

        @PutMapping("/subscription/{id}")
        public ResponseEntity<?> subscribeToGuild(@PathVariable Long id) throws ValidationException {
                User user;
                try {
                        user = userDetailsService.getCurrentUser();
                        guildService.subscribeToGuild(user, id);
                } catch (EntityNotFoundException e) {
                        log.error("Unknown user executed /guild/"+id+" and caused "+e.toString());
                        return ResponseEntity.notFound().build();
                }

                log.info("Пользователь "+ user.getLogin() + " успешно добавлен в гильдию c id="+id);
                return ResponseEntity.ok().body("Пользователь "+ user.getLogin() + " успешно добавлен в гильдию!");
        }



        @PutMapping("/unsubscribe/{id}")
        public  ResponseEntity<?> unsubscribeGuild(@PathVariable Long id) throws ValidationException{

                User user;
                try {
                        user = userDetailsService.getCurrentUser();
                        log.info("User: '"+user.getLogin()+"' executed /guild/unsubscribe/"+id);
                        long code = guildService.unsubscribeToGuild(user, id);

                        switch ((int) code) {
                                case 0 -> {
                                        return ResponseEntity.ok().build();
                                }
                                case -1 -> {
                                        return ResponseEntity.ok().body(new UnsubscribeGuildResponse(id, -1));
                                }
                                default -> {
                                        return ResponseEntity.ok().body(new UnsubscribeGuildResponse(-1, code));
                                }
                        }
                } catch (EntityNotFoundException e) {
                        log.error("Unknown user executed /guild/unsubscribe/"+id+" and caused "+e.toString());
                        return ResponseEntity.notFound().build();
                }
        }

        @GetMapping
        ResponseEntity<List<GuildResponse>> getGuildList(@RequestParam String orderBy){
                User user = userDetailsService.getCurrentUser();
                log.info("User: '"+user.getLogin()+"' executed /guild/?orderBy="+orderBy);
                List<GuildResponse> guilds = guildService.getGuildList(orderBy);
                return ResponseEntity.ok(guilds);
        }

        @GetMapping("/{id}")
        ResponseEntity<GuildResponse> getGuild(@PathVariable Long id){
                User user = userDetailsService.getCurrentUser();
                log.info("User: '"+user.getLogin()+"' executed /guild/"+id);
                return ResponseEntity.ok(guildService.getGuild(id));
        }

        @PutMapping("/leader/edit")
        @PreAuthorize("hasRole('GUILD_LEADER')")
        public ResponseEntity<String> changeGuildDescription(@RequestBody String description){
                User user = userDetailsService.getCurrentUser();
                Guild guild = guildService.editGuildDescription(user, description);
                log.info("Guild '" + guild.getTagName() + "' description changed by '"+user.getLogin() +"'.");
                return ResponseEntity.ok("Guild '" + guild.getTagName() + "' description changed by '"+user.getLogin() +"'.");
        }

}
