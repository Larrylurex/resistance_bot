package bot.notification;

import bot.ai.AIPlayer;
import bot.ai.mind.Mind;
import bot.entities.GameInfo;
import bot.entities.Player;
import bot.enums.MissionCard;
import bot.enums.Vote;
import bot.notification.event.*;
import bot.service.GameInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    @Autowired
    private GameInfoService gameInfoService;

    public void notifyIntroductionDone(GameInfo gameInfo) {
        notifyBots(gameInfo, this::getIntroductionEvent, Mind::processEvent);
    }

    private IntroductionEvent getIntroductionEvent(GameInfo gameInfo) {
        List<String> players = gameInfo.getPlayers()
                .stream()
                .map(Player::getLogin)
                .collect(Collectors.toList());
        return new IntroductionEvent(players);
    }

    public void notifyVoteDone(GameInfo gameInfo) {
        notifyBots(gameInfo, this::getVoteEvent, Mind::processEvent);
    }

    private VoteEvent getVoteEvent(GameInfo gameInfo) {
        VoteEvent event = new VoteEvent();
        Map<String, Vote> result = gameInfo.getPlayers()
                .stream()
                .collect(Collectors.toMap(Player::getLogin, Player::getVote));
        event.setVotes(result);
        return event;
    }

    public void notifyMissionDone(GameInfo gameInfo) {
        notifyBots(gameInfo, this::getMissionEvent, Mind::processEvent);
    }

    private MissionEvent getMissionEvent(GameInfo gameInfo) {

        MissionEvent event = new MissionEvent();
        List<String> team = getTeam(gameInfo);
        event.setTeam(team);
        int redCardsCount = (int) gameInfoService.getPlayersInMission(gameInfo)
                .stream()
                .map(Player::getCard)
                .filter(card -> MissionCard.RED == card)
                .count();
        event.setRedCardsCount(redCardsCount);
        return event;
    }

    public void notifyTeamChosenDone(GameInfo gameInfo) {
        notifyBots(gameInfo, this::getTeamChoosingEvent, Mind::processEvent);
    }

    private TeamChoosingEvent getTeamChoosingEvent(GameInfo gameInfo) {
        TeamChoosingEvent event = new TeamChoosingEvent();
        event.setLeader(gameInfoService.getLeaderOrThrowException(gameInfo).getLogin());
        event.setTeam(getTeam(gameInfo));
        return event;
    }

    private <T extends GameEvent> void notifyBots(GameInfo gameInfo, Function<GameInfo, T> getEvent, BiConsumer<Mind, T> processor) {
        T event = getEvent.apply(gameInfo);
        List<AIPlayer> bots = gameInfoService.getBots(gameInfo);
        bots.forEach(bot -> processor.accept(bot.getMind(), event));
    }

    private List<String> getTeam(GameInfo gameInfo) {
        return gameInfoService.getPlayersInMission(gameInfo)
                .stream()
                .map(Player::getLogin)
                .collect(Collectors.toList());
    }


}
