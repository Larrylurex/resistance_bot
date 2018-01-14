package bot.ai.mind;

import bot.SettingsHolder;
import bot.entities.GameInfo;
import bot.entities.Player;
import bot.util.ResistanceUtils;

import java.util.List;

public abstract class AbstractMind implements Mind{

    @Override
    public void chooseTeam(GameInfo gameInfo) {
        int missionersCount = SettingsHolder.getMissionersCount(gameInfo.getRound(), gameInfo.getPlayers().size());
        List<Player> missioners = ResistanceUtils.getRandomPlayers(missionersCount, gameInfo.getPlayers());
        missioners.forEach(player -> player.setOnMission(true));
    }
}
