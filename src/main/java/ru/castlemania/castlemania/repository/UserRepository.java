package ru.castlemania.castlemania.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.castlemania.castlemania.model.Guild;
import ru.castlemania.castlemania.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLogin(String login);

    List<User> findAllByOrderByExperienceDesc();

    Boolean existsByLogin(String login);

    Boolean existsByEmail(String email);

    User findFirstByGuildOrderByExperienceDesc(Guild guild);
}
