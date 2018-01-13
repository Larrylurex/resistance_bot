package bot.handler.game;

import bot.entities.GameInfo;
import bot.entities.Player;
import bot.enums.GamePhase;
import bot.exception.ProcessException;
import bot.exception.ValidationException;
import bot.handler.UpdateHandler;
import bot.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public abstract class AbstractUpdateHandler implements UpdateHandler {

    @Autowired
    protected LockService lockService;
    @Autowired
    protected GameInfoService gameInfoService;
    @Autowired
    protected KeyboardHolderService keyboardHolderService;
    @Autowired
    protected MessageService messageService;
    @Autowired
    protected SettingsService settingsService;
    @Autowired
    protected AIBotService botService;

    public List<BotApiMethod<? extends Serializable>> handleUpdate(Update update) {

        long chatId = getChatId(update);
        GameInfo gameInfo = gameInfoService.getGameInfoByChatId(chatId);

        ReentrantLock lock = lockService.getLock(chatId);
        lock.lock();

        List<BotApiMethod<? extends Serializable>> result = Collections.emptyList();
        try {
            validateQuery(update, gameInfo);
            result = processUpdate(update, gameInfo);
        } catch (ValidationException ex) {
            log.error("Wrong request", ex);
        } catch (ProcessException ex) {
            log.error("Error occurred while processing update", ex);
        } catch (Exception ex) {
            log.error("Unknown error occurred while processing update", ex);
        } finally {
            lock.unlock();
        }

        return result;
    }

    protected void validateQuery(Update update, GameInfo gameInfo) {
        long chatId = getChatId(update);
        if (gameInfo.getPhase() != getPhase()) {
            String message = String.format("Wrong game phase in chat %d. Current phase: %s; Requested phase: %s",
                    chatId,
                    gameInfo.getPhase(),
                    getPhase());
            throw new ValidationException(message);
        }
    }

    protected EditMessageReplyMarkup getRemoveKeyboardMessage(int messageId, Long chatId) {
        return new EditMessageReplyMarkup()
                .setChatId(chatId)
                .setMessageId(messageId);
    }

    protected SendMessage getSimpleMessage(long chatId, String text) {
        return new SendMessage()
                .setChatId(chatId)
                .setText(text);
    }

    protected SendMessage getMessageWithKeyboard(long chatId, String text, ReplyKeyboard keyboard) {
        return getSimpleMessage(chatId, text)
                .setReplyMarkup(keyboard);
    }


    protected SendMessage startNewGameCycle(GameInfo gameInfo) {
        gameInfoService.changeLeader(gameInfo);
        gameInfo.setPhase(GamePhase.ROUND_PICK_USER);
        return getYouAreLeaderMessage(gameInfo);
    }

    protected SendMessage getYouAreLeaderMessage(GameInfo gameInfo) {
        return getMessageWithKeyboard(gameInfo.getChatId(),
                messageService.getYouAreLeaderMessage(gameInfo),
                keyboardHolderService.getPlayersPickerKeyboard(gameInfo.getPlayers()));
    }

    protected List<SendMessage> gameOver(GameInfo gameInfo, String reason, boolean resistanceWon) {
        long chatId = gameInfo.getChatId();
        gameInfoService.clearChatData(chatId);
        lockService.removeLock(chatId);
        List<SendMessage> result = new ArrayList<>();
        result.add(getSimpleMessage(chatId, reason));
        String message = resistanceWon ? "RESISTANCE WON THE GAME" : "SPIES WON THE GAME";
        result.add(getMessageWithKeyboard(chatId, message, keyboardHolderService.getNewGameKeyboard()));
        return result;
    }

    protected Player getSender(Update update, GameInfo gameInfo) {
        String senderName = update.getCallbackQuery().getFrom().getUserName();
        return gameInfoService.getPlayerByLoginOrThrowException(senderName, gameInfo);
    }

    protected abstract List<BotApiMethod<? extends Serializable>> processUpdate(Update update, GameInfo gameInfo);

    protected abstract GamePhase getPhase();

    protected abstract long getChatId(Update update);

}
