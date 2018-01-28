package bot.notification;

import bot.entities.GameInfo;

public interface NotificationService {
    void notifyIntroductionDone(GameInfo gameInfo);

    void notifyVoteDone(GameInfo gameInfo);

    void notifyMissionDone(GameInfo gameInfo);

    void notifyTeamChosenDone(GameInfo gameInfo);
}
