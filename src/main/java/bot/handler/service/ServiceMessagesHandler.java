package bot.handler.service;

import bot.dao.GameInfoDao;
import bot.handler.UpdateHandler;
import bot.service.SettingsService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;

import java.io.Serializable;
import java.util.*;

@Component
public class ServiceMessagesHandler implements UpdateHandler {

    @Autowired
    private GameInfoDao gameInfoDao;

    @Override
    public List<BotApiMethod<? extends Serializable>> handleUpdate(Update update) {
        SERVICE_MESSAGE serviceMessage = getServiceMessage(update);
        List<BotApiMethod<? extends Serializable>> messages = new ArrayList<>();
        switch (serviceMessage) {
            case HELP:
                messages.add(getHelpMessage(update));
                break;
            case START:
                messages.add(getStartMessage(update));
                break;
            case QUIT:
                messages.add(getQuitMessage(update));
                break;
            case ADDED:
                messages.add(getAddedMessage(update));
                break;
            case UNKNOWN:
                messages.add(getUnknownMessage(update));
                break;
        }
        return messages;
    }

    private BotApiMethod<? extends Serializable> getAddedMessage(Update update) {
        SendMessage message = new SendMessage();
        message.setText("Hello! Let's /play the resistance game!");
        message.setChatId(update.getMessage().getChatId());
        return message;    }

    private BotApiMethod<? extends Serializable> getQuitMessage(Update update) {
        gameInfoDao.clearChatData(update.getMessage().getChatId());
        SendMessage message = new SendMessage();
        message.setText("Well, if you want so, I quit");
        message.setChatId(update.getMessage().getChatId());
        return message;
    }

    private SendMessage getUnknownMessage(Update update) {
        SendMessage message = new SendMessage();
        message.setText("I'm sorry, I don't understand this command yet. \nTo find out the available commands send /help");
        message.setChatId(update.getMessage().getChatId());
        return message;
    }

    private SendMessage getStartMessage(Update update) {
        SendMessage message = new SendMessage();
        message.setText("Hi, I'm the Resistance Game Bot, and I'm here to play with you The Resistance game\n" +
                "Full manual you can read <a href=\"https://en.wikipedia.org/wiki/The_Resistance_(game)\">here</a>\n" +
                "But if you're too lazy, just add me to your group, send /play and follow the instructions ;)\n" +
                "Don't forget to <b>add me</b> to your contacts, I will have to send you private message during the game");
        message.setChatId(update.getMessage().getChatId());
        message.setParseMode("HTML");
        return message;
    }

    private SendMessage getHelpMessage(Update update) {
        SendMessage message = new SendMessage();
        message.setText("Here's the available commands: \n/start - to find out what I'm for\n" +
                "/help - to see this message again\n" +
                "/play - to start new game\n" +
                "/quit - to immediately end current game");
        message.setChatId(update.getMessage().getChatId());
        message.setParseMode("HTML");
        return message;
    }

    private SERVICE_MESSAGE getServiceMessage(Update update) {
        SERVICE_MESSAGE message = Optional.ofNullable(update.getMessage())
                .map(Message::getText)
                .map(SERVICE_MESSAGE::getServiceMessage)
                .orElse(SERVICE_MESSAGE.UNKNOWN);

        if(message == SERVICE_MESSAGE.UNKNOWN && checkIfImAdded(update)){
            message = SERVICE_MESSAGE.ADDED;
        }
        return message;
    }

    private boolean checkIfImAdded(Update update) {
        return Optional.ofNullable(update.getMessage())
                .map(Message::getNewChatMembers)
                .orElse(Collections.emptyList())
                .stream()
                .anyMatch(user -> user.getUserName().equals(SettingsService.BOT_NAME));
    }

    enum SERVICE_MESSAGE {
        UNKNOWN("/unknown"),
        START("/start"),
        HELP("/help"),
        QUIT("/quit"),
        ADDED("/added");

        @Getter
        private String message;

        SERVICE_MESSAGE(String message) {
            this.message = message;
        }

        public static SERVICE_MESSAGE getServiceMessage(String message) {
            return Arrays.stream(SERVICE_MESSAGE.values())
                    .filter(m -> message.startsWith(m.getMessage()))
                    .findAny()
                    .orElse(SERVICE_MESSAGE.UNKNOWN);
        }
    }
}
