package bot.service;

import bot.entities.GameInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;

@Service
public class CommonMessageHolderService {

    @Autowired
    private MessageService messageService;
    @Autowired
    private GameInfoService gameInfoService;
    @Autowired
    private KeyboardHolderService keyboardHolderService;

    public EditMessageReplyMarkup getRemoveKeyboardMessage(int messageId, Long chatId) {
        return new EditMessageReplyMarkup()
                .setChatId(chatId)
                .setMessageId(messageId);
    }

    public SendMessage getSimpleMessage(long chatId, String text) {
        return new SendMessage()
                .setChatId(chatId)
                .setText(text);
    }

    public SendMessage getMessageWithKeyboard(long chatId, String text, ReplyKeyboard keyboard) {
        return getSimpleMessage(chatId, text)
                .setReplyMarkup(keyboard);
    }


    public SendMessage getYouAreLeaderMessage(GameInfo gameInfo) {
        ReplyKeyboard keyboard = null;
        if (!gameInfoService.getLeaderOrThrowException(gameInfo).isBot()) {
            keyboard = keyboardHolderService.getPlayersPickerKeyboard(gameInfo.getPlayers());
        }
        return getMessageWithKeyboard(gameInfo.getChatId(),
                messageService.getYouAreLeaderMessage(gameInfo),
                keyboard);
    }
}
