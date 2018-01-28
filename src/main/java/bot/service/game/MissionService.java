package bot.service.game;

import bot.SettingsHolder;
import bot.entities.GameInfo;
import bot.entities.Player;
import bot.enums.GamePhase;
import bot.enums.MissionCard;
import bot.notification.NotificationService;
import bot.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MissionService {

    private static final int NUMBER_OF_ROUNDS_TO_WIN = 3;
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
    private GameOverService gameOverService;

    public List<BotApiMethod<? extends Serializable>> startMission(GameInfo gameInfo) {
        List<BotApiMethod<? extends Serializable>> result = new ArrayList<>();
        botService.goOnMission(gameInfo);
        if (areAllMissionersMadeAMove(gameInfo)) {
            result.addAll(processMissionResults(gameInfo));
        } else {
            gameInfo.setPhase(GamePhase.ROUND_PLAY);
            result.add(getStartMissionMessage(gameInfo));
            botService.goOnMission(gameInfo);
        }
        return result;
    }

    public boolean areAllMissionersMadeAMove(GameInfo gameInfo) {
        return gameInfo.getPlayers().stream()
                .filter(Player::isOnMission)
                .map(Player::getCard)
                .noneMatch(v -> v == MissionCard.NONE);
    }

    public List<BotApiMethod<? extends Serializable>> processMissionResults(GameInfo gameInfo) {
        List<BotApiMethod<? extends Serializable>> result = new ArrayList<>();
        notificationService.notifyMissionDone(gameInfo );
        Map<MissionCard, Long> cardsCount = getCardsCount(gameInfo);
        setRoundResult(gameInfo, cardsCount);

        result.add(getCountMessage(gameInfo, cardsCount));
        result.add(getRoundResultMessage(gameInfo));

        Optional<Boolean> resistanceWon = isResistanceWinner(gameInfo);
        if (resistanceWon.isPresent()) {
            result.addAll(gameOverService.gameOver(gameInfo, messageService.getThreeRoundsWon(resistanceWon.get()), resistanceWon.get()));
        } else {
            gameInfo.newRound();
            result.addAll(startGameService.startRound(gameInfo));
        }
        return result;
    }

    private Map<MissionCard, Long> getCardsCount(GameInfo gameInfo) {
        return gameInfo.getPlayers().stream()
                .map(Player::getCard)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    private SendMessage getRoundResultMessage(GameInfo gameInfo) {
        boolean resistanceWon = gameInfo.getRoundResult();
        return commonMessageHolder.getSimpleMessage(gameInfo.getChatId(),
                messageService.getWonRoundMessage(resistanceWon, gameInfo.getRoundNumber()));
    }

    private SendMessage getCountMessage(GameInfo gameInfo, Map<MissionCard, Long> cardsCount) {
        String message = messageService.getCardsCountMessage(
                cardsCount.getOrDefault(MissionCard.BLUE, 0L),
                cardsCount.getOrDefault(MissionCard.RED, 0L));
        return commonMessageHolder.getSimpleMessage(gameInfo.getChatId(), message);
    }


    private SendMessage getStartMissionMessage(GameInfo gameInfo) {
        return commonMessageHolder.getMessageWithKeyboard(gameInfo.getChatId(),
                messageService.getStartMissionMessage(gameInfoService.getPlayersInMission(gameInfo)),
                keyboardHolderService.getMissionKeyboard());
    }

    private void setRoundResult(GameInfo gameInfo, Map<MissionCard, Long> cardsCount) {
        int redCardsCountToWin = SettingsHolder.getRedCardsCountToWin(gameInfo.getRound(), gameInfo.getPlayers().size());
        boolean spiesWon = cardsCount.getOrDefault(MissionCard.RED, 0L) >= redCardsCountToWin;
        gameInfo.setRoundResult(!spiesWon);
    }

    private Optional<Boolean> isResistanceWinner(GameInfo gameInfo) {
        Optional<Boolean> winner = Optional.empty();
        if (gameInfo.getRoundNumber() >= NUMBER_OF_ROUNDS_TO_WIN) {
            Map<Boolean, Long> roundResults = Arrays.stream(gameInfo.getRoundResults())
                    .filter(Objects::nonNull)
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
            if (roundResults.getOrDefault(true, 0L) >= NUMBER_OF_ROUNDS_TO_WIN) {
                winner = Optional.of(true);
            }
            if (roundResults.getOrDefault(false, 0L) >= NUMBER_OF_ROUNDS_TO_WIN) {
                winner = Optional.of(false);
            }
        }
        return winner;
    }
}
