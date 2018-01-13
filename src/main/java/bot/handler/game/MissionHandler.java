package bot.handler.game;

import bot.entities.GameInfo;
import bot.entities.Player;
import bot.enums.GamePhase;
import bot.enums.MissionCard;
import bot.exception.ValidationException;
import bot.handler.game.data.CallbackQueryData;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class MissionHandler extends AbstractUpdateHandler {


    public static final int NUMBER_OF_ROUNDS_TO_WIN = 3;

    @Override
    protected GamePhase getPhase() {
        return GamePhase.ROUND_PLAY;
    }

    @Override
    protected long getChatId(Update update) {
        return update.getCallbackQuery().getMessage().getChatId();
    }

    @Override
    protected void validateQuery(Update update, GameInfo gameInfo) {
        super.validateQuery(update, gameInfo);
        Player sender = getSender(update, gameInfo);
        if (!sender.isOnMission()) {
            String message = String.format("%s is not in this Mission", sender.getLogin());
            throw new ValidationException(message);
        }
    }

    @Override
    protected List<BotApiMethod<? extends Serializable>> processUpdate(Update update, GameInfo gameInfo) {
        String dataJson = update.getCallbackQuery().getData();
        CallbackQueryData queryData = CallbackQueryData.parseQueryData(dataJson);
        String data = queryData.getData();

        MissionCard card = MissionCard.getMissionCardByCode(Integer.parseInt(data));
        Player player = getSender(update, gameInfo);
        player.setCard(card);

        List<BotApiMethod<? extends Serializable>> result = new ArrayList<>();
        result.add(getPlayerPickedCardMessage(gameInfo.getChatId(), player));
        if (gameInfoService.areAllMissionersMadeAMove(gameInfo)) {
            int messageId = update.getCallbackQuery().getMessage().getMessageId();
            result.add(getRemoveKeyboardMessage(messageId, gameInfo.getChatId()));
            result.addAll(processMissionResults(gameInfo));
        }
        return result;
    }

    private SendMessage getPlayerPickedCardMessage(long chatId, Player player) {
        return getSimpleMessage(chatId, messageService.getPlayerMadeAMoveMessage(player));
    }

    private List<SendMessage> processMissionResults(GameInfo gameInfo) {
        List<SendMessage> result = new ArrayList<>();

        Map<MissionCard, Long> cardsCount = getCardsCount(gameInfo);
        setRoundResult(gameInfo, cardsCount);

        result.add(getCountMessage(gameInfo, cardsCount));
        result.add(getRoundResultMessage(gameInfo));

        Optional<Boolean> resistanceWon = isResistanceWinner(gameInfo);
        if (resistanceWon.isPresent()) {
            result.addAll(gameOver(gameInfo, messageService.getThreeRoundsWon(resistanceWon.get()), resistanceWon.get()));
        } else {
            gameInfo.newRound();
            result.add(getSimpleMessage(gameInfo.getChatId(), messageService.getRoundMessage(gameInfo.getRoundNumber())));
            result.add(startNewGameCycle(gameInfo));
        }
        return result;
    }

    private Optional<Boolean> isResistanceWinner(GameInfo gameInfo) {
        Optional<Boolean> winner = Optional.empty();
        if (gameInfo.getRoundNumber() >= NUMBER_OF_ROUNDS_TO_WIN) {
            Map<Boolean, Long> roundResults = Arrays.stream(gameInfo.getRoundResults())
                    .filter(Objects::nonNull)
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
            if (roundResults.get(true) >= NUMBER_OF_ROUNDS_TO_WIN) {
                winner = Optional.of(true);
            }
            if (roundResults.get(false) >= NUMBER_OF_ROUNDS_TO_WIN) {
                winner = Optional.of(false);
            }
        }
        return winner;
    }

    private void setRoundResult(GameInfo gameInfo, Map<MissionCard, Long> cardsCount) {
        int redCardsCountToWin = settingsService.getRedCardsCountToWin(gameInfo.getRound(), gameInfo.getPlayers().size());
        boolean spiesWon = cardsCount.getOrDefault(MissionCard.RED, 0L) >= redCardsCountToWin;
        gameInfo.setRoundResult(!spiesWon);
    }

    private SendMessage getRoundResultMessage(GameInfo gameInfo) {
        boolean resistanceWon = gameInfo.getRoundResult();
        return getSimpleMessage(gameInfo.getChatId(), messageService.getWonRoundMessage(resistanceWon, gameInfo.getRoundNumber()));
    }

    private SendMessage getCountMessage(GameInfo gameInfo, Map<MissionCard, Long> cardsCount) {
        String message = messageService.getCardsCountMessage(
                cardsCount.getOrDefault(MissionCard.BLUE, 0L),
                cardsCount.getOrDefault(MissionCard.RED, 0L));
        return getSimpleMessage(gameInfo.getChatId(), message);
    }

    private Map<MissionCard, Long> getCardsCount(GameInfo gameInfo) {
        return gameInfo.getPlayers().stream()
                .map(Player::getCard)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

}
