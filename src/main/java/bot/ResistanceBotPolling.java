package bot;

import bot.handler.UpdateHandler;
import bot.handler.factory.HandlerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.Serializable;
import java.util.List;

@Component
@Slf4j
public class ResistanceBotPolling extends TelegramLongPollingBot {

    @Autowired
    private HandlerFactory factory;

    @Override
    public void onUpdateReceived(Update update) {
        UpdateHandler handler = factory.getHandler(update);
        List<BotApiMethod<? extends Serializable>> methods = handler.handleUpdate(update);
        for (BotApiMethod<? extends Serializable> method : methods) {
            executeExceptional(method);
        }
    }

    private void executeExceptional(BotApiMethod<? extends Serializable> method) {
        try {
            execute(method);
        } catch (TelegramApiException ex) {
            log.error("Couldn't execute method", ex);
        }
    }

    @Override
    public String getBotUsername() {
        return SettingsHolder.BOT_NAME;
    }

    @Override
    public String getBotToken() {
        return SettingsHolder.BOT_TOKEN;
    }
}
