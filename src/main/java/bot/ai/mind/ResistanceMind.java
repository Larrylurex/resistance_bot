package bot.ai.mind;

import bot.SettingsHolder;
import bot.ai.mind.data.PlayerInfo;
import bot.entities.GameInfo;
import bot.entities.Player;
import bot.enums.MissionCard;
import bot.enums.Vote;
import bot.notification.event.IntroductionEvent;
import bot.notification.event.MissionEvent;
import bot.notification.event.TeamChoosingEvent;
import bot.notification.event.VoteEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

import static bot.ai.mind.data.PlayerInfo.MAX_PERCENTAGE;

@Slf4j
public class ResistanceMind extends AbstractMind {

    private Map<String, PlayerInfo> playersInfo = new HashMap<>();

    public ResistanceMind(Player me) {
        super(me);
    }

    @Override
    public void chooseTeam(GameInfo gameInfo) {
        int missionersCount = SettingsHolder.getMissionersCount(gameInfo.getRound(), gameInfo.getPlayers().size());
        List<String> missioners = getLessProbableSpies(missionersCount);
        gameInfo.getPlayers().stream()
                .filter(p -> missioners.contains(p.getLogin()))
                .forEach(player -> player.setOnMission(true));
    }

    @Override
    public Vote vote(GameInfo gameInfo) {
        boolean lastTry = gameInfo.getStep() >= 4;
        boolean probableSpyInTeam = gameInfo.getPlayers().stream().filter(Player::isOnMission)
                .map(Player::getLogin)
                .map(playersInfo::get)
                .map(PlayerInfo::getSpyProbability)
                .anyMatch(probability -> probability > 80);
        return probableSpyInTeam && !lastTry ? Vote.AGAINST : Vote.FOR;
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

    @Override
    protected void recalculateProbability(int round) {
        if (isThereAnythingToThinkAbout()) {
            processData(playerInfo -> processMissionResults(playerInfo, round));
            processData(playerInfo -> processVoteResults(playerInfo, round));
            processData(playerInfo -> processLeaderChoiceResults(playerInfo, round));
            log.info(getProbabilityLog());
        } else {
            log.info("There's nothing to think about yet");

        }
    }

    private void processData(Consumer<PlayerInfo> processor) {
        playersInfo.values().stream()
                .filter(playerInfo -> !playerInfo.getLogin().equals(me.getLogin()))
                .forEach(processor);
    }

    private boolean isThereAnythingToThinkAbout() {
        return playersInfo.values().stream()
                .map(PlayerInfo::getMissionResults)
                .flatMap(List::stream)
                .map(PlayerInfo.MissionResult::getRedCardsCount)
                .anyMatch(redCards -> redCards > 0);
    }

    private String getProbabilityLog() {
        return playersInfo.values().stream()
                .map(p -> p.getLogin() + ": " + p.getSpyProbability())
                .collect(Collectors.joining(", ", me.getLogin() + " thinks: [", "]"));
    }

    private void processMissionResults(PlayerInfo playerInfo, int round) {
        int spyProbability = playerInfo.getSpyProbability();
        for (PlayerInfo.MissionResult result : playerInfo.getMissionResults()) {
            int rate = MAX_PERCENTAGE - spyProbability;
            if (result.getTeam().contains(playerInfo.getLogin())) {
                if (result.getRedCardsCount() > 0) {
                    spyProbability += (rate * (long) (result.getRedCardsCount()) / result.getTeam().size());
                } else {
                    spyProbability -= getProbabilityFunction(round).applyAsInt(rate);
                }
            }
        }
        playerInfo.setSpyProbability(spyProbability);
    }

    private void processVoteResults(PlayerInfo playerInfo, int round) {
        int spiesCount = SettingsHolder.getSpiesCount(playersInfo.size());
        List<String> mostProbableSpies = getMostProbableSpies(spiesCount);
        int spyProbability = playerInfo.getSpyProbability();
        for (PlayerInfo.VoteResult result : playerInfo.getVoteResults()) {
            int rate = MAX_PERCENTAGE - spyProbability;
            int probAdds = getProbabilityFunction(round).applyAsInt(rate);
            int voteRate = result.getVote() == Vote.FOR ? 1 : -1;
            int spiesInTeamRate = Collections.disjoint(result.getTeam(), mostProbableSpies) ? -1 : 1;
            spyProbability += voteRate * spiesInTeamRate * probAdds;
        }
        playerInfo.setSpyProbability(spyProbability);
    }

    private void processLeaderChoiceResults(PlayerInfo playerInfo, int round) {
        int spiesCount = SettingsHolder.getSpiesCount(playersInfo.size());
        List<String> mostProbableSpies = getMostProbableSpies(spiesCount);
        int spyProbability = playerInfo.getSpyProbability();
        for (List<String> team : playerInfo.getLeaderChoice()) {
            int rate = MAX_PERCENTAGE - spyProbability;
            int probAdds = getProbabilityFunction(round).applyAsInt(rate);
            int spiesInTeamRate = Collections.disjoint(team, mostProbableSpies) ? -1 : 1;
            spyProbability += spiesInTeamRate * probAdds;
        }
        playerInfo.setSpyProbability(spyProbability);
    }

    private IntUnaryOperator getProbabilityFunction(int maxRate) {
        double a = -maxRate / Math.pow(50, 2);
        double b = 2 * maxRate / 50;
        return x -> (int)(a * Math.pow(x, 2) + b * x);
    }

    private List<String> getLessProbableSpies(int count) {
        Comparator<PlayerInfo> playerComparator = Comparator.comparingInt(PlayerInfo::getSpyProbability);
        return getTopPlayers(count, playerComparator);
    }

    private List<String> getMostProbableSpies(int count) {
        Comparator<PlayerInfo> playerComparator = Comparator.comparingInt(PlayerInfo::getSpyProbability);
        return getTopPlayers(count, playerComparator.reversed());
    }

    private List<String> getTopPlayers(int count, Comparator<PlayerInfo> playerComparator) {
        return playersInfo.values().stream()
                .sorted(playerComparator)
                .limit(count)
                .map(PlayerInfo::getLogin)
                .collect(Collectors.toList());
    }
}
