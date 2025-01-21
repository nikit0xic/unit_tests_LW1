package ru.castlemania.castlemania.service;

import jakarta.persistence.EntityExistsException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;
import ru.castlemania.castlemania.model.Achievement;
import ru.castlemania.castlemania.model.AchievementProgress;
import ru.castlemania.castlemania.model.User;
import ru.castlemania.castlemania.repository.AchievementProgressRepository;
import ru.castlemania.castlemania.repository.AchievementRepository;

import java.util.Date;
import java.util.List;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class AchievementService {

    AchievementRepository achievementRepository;
    AchievementProgressRepository achievementProgressRepository;


    public void updateAchievementOfUser(User user, Long achievementId) {
        Achievement achievement = achievementRepository.findById(achievementId).orElseThrow(EntityExistsException::new);
        AchievementProgress progress = achievementProgressRepository.findAchievementProgressByAchievementAndUser(achievement,user);

        if (progress.getGainedCount()>=achievement.getTaskAmount()){
            if (!progress.getAchieved()) {
                progress.setAchieved(true);

                progress.setAchievedDate(new Date());
                achievementProgressRepository.save(progress);
            }
            return;
        }
        progress.setGainedCount(progress.getGainedCount()+1);
        if (progress.getGainedCount().equals(achievement.getTaskAmount())) {
            progress.setAchieved(true);
            progress.setAchievedDate(new Date());
        }

        achievementProgressRepository.save(progress);
    }


    public List<Achievement> getAchievementsGainedByUser(User user){
        List<AchievementProgress> progresses = achievementProgressRepository.findAchievementProgressesByUserAndAchievedTrue(user);
        return progresses.stream().map(AchievementProgress::getAchievement).toList();

    }

    public List<Achievement> getAchievementProgressesByUser(User user){
        List<AchievementProgress> progresses = achievementProgressRepository.findAchievementProgressesByUser(user);

        return progresses.stream().map(AchievementProgress::getAchievement).toList();

    }
}
