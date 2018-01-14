package bot.service;

import bot.ai.AIPlayer;
import bot.entities.GameInfo;
import bot.entities.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AIBotService {

    @Autowired
    private GameInfoService gameInfoService;

    public void vote(GameInfo gameInfo) {
        gameInfoService.getBots(gameInfo).stream()
                .filter(bot -> !bot.isLeader())
                .forEach(AIPlayer::vote);
    }

    public void chooseTeam(GameInfo gameInfo) {
        Player leader = gameInfoService.getLeaderOrThrowException(gameInfo);
        if (leader.isBot()) {
            ((AIPlayer) leader).chooseTeam(gameInfo);
        }
    }

    public void goOnMission(GameInfo gameInfo) {
        gameInfoService.getBots(gameInfo).stream()
                .filter(Player::isOnMission)
                .forEach(AIPlayer::goOnMission);
    }
}
