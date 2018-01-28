package bot.ai.mind;

import bot.entities.GameInfo;
import bot.enums.MissionCard;
import bot.enums.Vote;
import bot.notification.event.GameEvent;
import bot.notification.listener.GameEventListener;

public interface Mind<T extends GameEvent> extends GameEventListener<T> {

    Vote vote(GameInfo gameInfo);

    void chooseTeam(GameInfo gameInfo);

    MissionCard goOnMission(GameInfo gameInfo);
}
