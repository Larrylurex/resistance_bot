package bot.ai.mind;

import bot.entities.GameInfo;
import bot.enums.MissionCard;
import bot.enums.Vote;

public class SpyMind extends AbstractMind {

    @Override
    public Vote vote() {
        return Vote.AGAINST;
    }

    @Override
    public MissionCard goOnMission() {
        return MissionCard.RED;
    }
}
