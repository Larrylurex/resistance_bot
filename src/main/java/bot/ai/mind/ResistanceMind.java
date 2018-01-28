package bot.ai.mind;

import bot.SettingsHolder;
import bot.ai.mind.data.PlayerInfo;
import bot.entities.GameInfo;
import bot.entities.Player;
import bot.enums.MissionCard;
import bot.enums.Vote;
import bot.notification.event.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ResistanceMind extends AbstractMind {

    private Map<String, PlayerInfo> playersInfo = new HashMap<>();

    public ResistanceMind(Player me) {
        super(me);
    }

    @Override
    public void chooseTeam(GameInfo gameInfo) {
        int missionersCount = SettingsHolder.getMissionersCount(gameInfo.getRound(), gameInfo.getPlayers().size());
        List<Player> missioners = getLessProbableSpies(gameInfo, missionersCount);

        missioners.forEach(player -> player.setOnMission(true));
    }

    @Override
    protected void think() {
        playersInfo.values().forEach(PlayerInfo::recalculateProbability);
    }

    @Override
    public Vote vote(GameInfo gameInfo) {
        boolean lastTry = gameInfo.getStep() >= 4;
        boolean probableSpyInTeam = gameInfo.getPlayers().stream().filter(Player::isOnMission)
                .map(Player::getLogin)
                .map(playersInfo::get)
                .map(PlayerInfo::getSpyProbability)
                .anyMatch(probability -> probability > 75);
        return probableSpyInTeam && !lastTry? Vote.AGAINST : Vote.FOR;
    }

    @Override
    public MissionCard goOnMission(GameInfo gameInfo) {
        return MissionCard.BLUE;
    }

    @Override
    protected void processIntroductionEvent(IntroductionEvent event) {
        playersInfo = event.getPlayers()
                .stream()
                .map(PlayerInfo::new)
                .collect(Collectors.toMap(PlayerInfo::getLogin, Function.identity()));
        playersInfo.get(me.getLogin()).setSpyProbability(0);
    }

    @Override
    public void processMissionEvent(MissionEvent event) {
        playersInfo.values()
                .forEach(info -> info.addMissionResult(event.getTeam(), event.getRedCardsCount()));
    }

    @Override
    public void processTeamChoosingEvent(TeamChoosingEvent event) {
        playersInfo.get(event.getLeader()).addLeaderChoiceResult(event.getTeam());
    }

    @Override
    public void processVoteEvent(VoteEvent event) {
        playersInfo.values()
        .forEach(info -> info.addVoteResultResult(event.getTeam(),
                event.getVotes().get(info.getLogin())));
    }

    private List<Player> getLessProbableSpies(GameInfo gameInfo, int missionersCount){
        Comparator<Player> playerComparator = Comparator.comparingInt(p -> playersInfo.get(p.getLogin()).getSpyProbability());
        return gameInfo.getPlayers().stream()
                .sorted(playerComparator.reversed())
                .limit(missionersCount)
                .collect(Collectors.toList());
    }
}
