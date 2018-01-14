package bot.ai.mind;

import bot.entities.GameInfo;
import bot.enums.MissionCard;
import bot.enums.Vote;
import bot.handler.game.MissionHandler;

public class ResistanceMind extends AbstractMind{
    @Override
    public Vote vote() {
        return Vote.FOR;
    }

    @Override
    public MissionCard goOnMission() {
        return MissionCard.BLUE;
    }
}
