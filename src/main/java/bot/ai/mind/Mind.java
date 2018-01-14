package bot.ai.mind;

import bot.entities.GameInfo;
import bot.enums.MissionCard;
import bot.enums.Vote;

public interface Mind {

    Vote vote();

    void chooseTeam(GameInfo gameInfo);

    MissionCard goOnMission();
}
