package bot.service.game;

import bot.entities.GameInfo;
import bot.entities.Player;
import bot.enums.GamePhase;
import bot.enums.Vote;
import bot.notification.NotificationService;
import bot.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class VoteService {

    @Autowired
    private GameInfoService gameInfoService;
    @Autowired
    private AIBotService botService;
    @Autowired
    private CommonMessageHolderService commonMessageHolder;
    @Autowired
    private MessageService messageService;
    @Autowired
    private KeyboardHolderService keyboardHolderService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private StartGameService startGameService;
    @Autowired
    private MissionService missionService;
    @Autowired
    private GameOverService gameOverService;

    public List<BotApiMethod<? extends Serializable>> startVote(GameInfo gameInfo) {
        List<BotApiMethod<? extends Serializable>> result = new ArrayList<>();
        notificationService.notifyTeamChosenDone(gameInfo);
        botService.vote(gameInfo);
        if (needToVote(gameInfo)) {
            gameInfo.setPhase(GamePhase.ROUND_VOTE);
            result.add(getLetsVoteMessage(gameInfo));
        } else {
            result.addAll(processVote(gameInfo));
        }
        return result;
    }

    public SendMessage getLetsVoteMessage(GameInfo gameInfo) {
        String message = messageService.getLeaderChoiceMessage(gameInfoService.getLeaderOrThrowException(gameInfo),
                gameInfoService.getPlayersInMission(gameInfo));
        InlineKeyboardMarkup keyboard = keyboardHolderService.getVotingKeyboard();
        return commonMessageHolder.getMessageWithKeyboard(gameInfo.getChatId(), message, keyboard);
    }

    public List<BotApiMethod<? extends Serializable>> processVote(GameInfo gameInfo) {
        List<BotApiMethod<? extends Serializable>> result = new ArrayList<>();
        result.add(getVotesMessage(gameInfo));
        notificationService.notifyVoteDone(gameInfo);
        if (isTeamConfirmed(gameInfo)) {
            result.addAll(missionService.startMission(gameInfo));
        } else {
            gameInfo.newStep();
            if (checkStepsOver(gameInfo)) {
                result.addAll(gameOverService.gameOver(gameInfo, messageService.getRunOutOfStepsMessage(), false));
            } else {
                result.addAll(startGameService.startGameCycle(gameInfo));
            }
        }
        return result;
    }

    private boolean isTeamConfirmed(GameInfo gameInfo) {
        Map<Vote, Long> votes = gameInfo.getPlayers().stream()
                .map(Player::getVote)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return votes.getOrDefault(Vote.FOR, 0L) >
                votes.getOrDefault(Vote.AGAINST, 0L);
    }

    private boolean checkStepsOver(GameInfo gameInfo) {
        return gameInfo.getStep() >= 5;
    }


    private SendMessage getVotesMessage(GameInfo gameInfo) {
        return commonMessageHolder.getSimpleMessage(gameInfo.getChatId(),
                messageService.getVoteResultMessage(gameInfo.getPlayers())).setParseMode("HTML");
    }

    public boolean needToVote(GameInfo gameInfo) {
        return gameInfo.getPlayers().stream()
                .map(Player::getVote)
                .anyMatch(v -> v == Vote.NONE);
    }
}
