package bot.service;

import bot.ai.AIPlayer;
import bot.entities.GameInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AIBotService {

    @Autowired
    private GameInfoService gameInfoService;

    public void vote(GameInfo gameInfo){
        gameInfoService.getBots(gameInfo)
                .forEach(AIPlayer::vote);
    }
}
