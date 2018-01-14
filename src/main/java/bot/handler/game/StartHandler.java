package bot.handler.game;

import bot.entities.GameInfo;
import bot.enums.GamePhase;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class StartHandler extends AbstractUpdateHandler {

    @Override
    protected GamePhase getPhase() {
        return GamePhase.START;
    }

    @Override
    protected long getChatId(Update update) {
        return Optional.ofNullable(update.getMessage())
                .orElseGet(() -> update.getCallbackQuery().getMessage())
                .getChatId();
    }

    @Override
    protected List<BotApiMethod<? extends Serializable>> processUpdate(Update update, GameInfo gameInfo) {
        Long chatId = getChatId(update);
        BotApiMethod<Message> message = commonMessageHolder.getMessageWithKeyboard(chatId,
                messageService.getRegisterMessage(),
                keyboardHolderService.getRegistrationKeyboard());
        saveGameInfo(gameInfo);
        return Collections.singletonList(message);
    }

    private void saveGameInfo(GameInfo gameInfo) {
        gameInfo.setPhase(GamePhase.REGISTRATION);
        gameInfoService.save(gameInfo);
    }
}
