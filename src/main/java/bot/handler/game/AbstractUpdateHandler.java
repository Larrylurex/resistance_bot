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
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public abstract class AbstractUpdateHandler implements UpdateHandler {

    @Autowired
    protected LockService lockService;
    @Autowired
    protected GameInfoService gameInfoService;
    @Autowired
    protected CommonMessageHolderService commonMessageHolder;
    @Autowired
    protected KeyboardHolderService keyboardHolderService;
    @Autowired
    protected MessageService messageService;
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
            result.addAll(getRemoveKeyBoardMessageIfNeeded(result, update));
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

    private List<? extends BotApiMethod<? extends Serializable>> getRemoveKeyBoardMessageIfNeeded(List<BotApiMethod<? extends Serializable>> messages, Update update) {
        boolean newKeyboardExists = messages.stream().filter(m -> m instanceof SendMessage)
                .map(SendMessage.class::cast)
                .map(SendMessage::getReplyMarkup)
                .anyMatch(Objects::nonNull);
        List<BotApiMethod<? extends Serializable>> result = new ArrayList<>();
        if (newKeyboardExists) {
            getMessageId(update)
                    .ifPresent(messageId -> result.add(commonMessageHolder.getRemoveKeyboardMessage(messageId, getChatId(update))));
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

    protected Player getSender(Update update, GameInfo gameInfo) {
        String senderName = update.getCallbackQuery().getFrom().getUserName();
        return gameInfoService.getPlayerByLoginOrThrowException(senderName, gameInfo);
    }

    protected Optional<Integer> getMessageId(Update update) {
        return Optional.ofNullable(update.getCallbackQuery())
                .map(CallbackQuery::getMessage)
                .map(Message::getMessageId);

    }

    protected abstract List<BotApiMethod<? extends Serializable>> processUpdate(Update update, GameInfo gameInfo);

    protected abstract GamePhase getPhase();

    protected abstract long getChatId(Update update);

}
