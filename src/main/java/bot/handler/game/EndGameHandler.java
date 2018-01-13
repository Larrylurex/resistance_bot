package bot.handler.game;

import bot.entities.GameInfo;
import bot.enums.GamePhase;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.objects.Update;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Component
public class EndGameHandler extends AbstractUpdateHandler {

    @Override
    protected GamePhase getPhase() {
        return GamePhase.END;
    }

    @Override
    protected long getChatId(Update update) {
        return update.getCallbackQuery().getMessage().getChatId();
    }

    @Override
    protected void validateQuery(Update update, GameInfo gameInfo) {
        //Do nothing
    }

    @Override
    protected List<BotApiMethod<? extends Serializable>> processUpdate(Update update, GameInfo gameInfo) {
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        return Collections.singletonList(getRemoveKeyboardMessage(messageId, getChatId(update)));
    }
}
