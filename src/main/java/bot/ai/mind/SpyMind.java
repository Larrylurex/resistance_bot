package bot.ai.mind;

import bot.SettingsHolder;
import bot.entities.GameInfo;
import bot.entities.Player;
import bot.enums.MissionCard;
import bot.enums.Vote;
import bot.notification.event.IntroductionEvent;
import bot.notification.event.MissionEvent;
import bot.notification.event.TeamChoosingEvent;
import bot.notification.event.VoteEvent;
import bot.util.ResistanceUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class SpyMind extends AbstractMind {

    public SpyMind(Player me) {
        super(me);
    }

    @Override
    public Vote vote(GameInfo gameInfo) {
        boolean spyOnMission = gameInfo.getPlayers().stream()
                .filter(Player::isOnMission)
                .anyMatch(Player::isSpy);

        Random rand = new Random(System.currentTimeMillis());
        boolean goAgainst = !spyOnMission || (gameInfo.getRound() + rand.nextInt(5)) >= 4;
        return goAgainst ? Vote.AGAINST : Vote.FOR;
    }

    @Override
    public MissionCard goOnMission(GameInfo gameInfo) {
        Random rand = new Random(System.currentTimeMillis());
        boolean goRed = (gameInfo.getRound() + rand.nextInt(5)) >= 5;

        return goRed ? MissionCard.RED: MissionCard.BLUE;
    }

    @Override
    public void chooseTeam(GameInfo gameInfo) {
        int missionersCount = SettingsHolder.getMissionersCount(gameInfo.getRound(), gameInfo.getPlayers().size());
        List<Player> missioners = new ArrayList<>();
        missioners.add(me);

        List<Player> others = gameInfo.getPlayers().stream()
                .filter(p -> !p.equals(me))
                .collect(Collectors.toList());
        missioners.addAll(ResistanceUtils.getRandomPlayers(missionersCount-1, others));

        missioners.forEach(player -> player.setOnMission(true));
    }
}
