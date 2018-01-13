package bot.service;

import bot.entities.Player;
import bot.enums.GamePhase;
import bot.enums.MissionCard;
import bot.enums.Vote;
import bot.handler.game.data.CallbackQueryData;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class KeyboardHolderServiceImpl implements KeyboardHolderService {

    @Override
    public InlineKeyboardMarkup getRegistrationKeyboard() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(new InlineKeyboardButton()
                .setText("I'm in")
                .setCallbackData(createCallbackDataJson(GamePhase.REGISTRATION, "register")));
        rowInline.add(new InlineKeyboardButton()
                .setText("Add bot")
                .setCallbackData(createCallbackDataJson(GamePhase.REGISTRATION, "add_bot")));
        rowsInline.add(rowInline);
        rowInline = new ArrayList<>();
        rowInline.add(new InlineKeyboardButton()
                .setText("PLAY")
                .setCallbackData(createCallbackDataJson(GamePhase.REGISTRATION, "play")));
        rowsInline.add(rowInline);

        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    @Override
    public InlineKeyboardMarkup getPlayersPickerKeyboard(Set<Player> players) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        for (Player player : players) {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            rowInline.add(new InlineKeyboardButton()
                    .setText(player.getLogin())
                    .setCallbackData(createCallbackDataJson(GamePhase.ROUND_PICK_USER, player.getLogin())));
            rowsInline.add(rowInline);
        }
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    @Override
    public InlineKeyboardMarkup getVotingKeyboard() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        rowInline.add(new InlineKeyboardButton()
                .setText("For")
                .setCallbackData(createCallbackDataJson(GamePhase.ROUND_VOTE, Integer.toString(Vote.FOR.getCode()))));

        rowInline.add(new InlineKeyboardButton()
                .setText("Against")
                .setCallbackData(createCallbackDataJson(GamePhase.ROUND_VOTE, Integer.toString(Vote.AGAINST.getCode()))));

        rowsInline.add(rowInline);

        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    @Override
    public InlineKeyboardMarkup getMissionKeyboard() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        rowInline.add(new InlineKeyboardButton()
                .setText("Red")
                .setCallbackData(createCallbackDataJson(GamePhase.ROUND_PLAY, Integer.toString(MissionCard.RED.getCode()))));

        rowInline.add(new InlineKeyboardButton()
                .setText("Blue")
                .setCallbackData(createCallbackDataJson(GamePhase.ROUND_PLAY, Integer.toString(MissionCard.BLUE.getCode()))));

        rowsInline.add(rowInline);

        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    @Override
    public ReplyKeyboard getNewGameKeyboard() {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        rowInline.add(new InlineKeyboardButton()
                .setText("PLAY AGAIN")
                .setCallbackData(createCallbackDataJson(GamePhase.START, "")));

        rowInline.add(new InlineKeyboardButton()
                .setText("FINISH")
                .setCallbackData(createCallbackDataJson(GamePhase.END, "")));

        rowsInline.add(rowInline);

        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    private String createCallbackDataJson(GamePhase phase, String text) {
        CallbackQueryData queryData = new CallbackQueryData(phase.getCode(), text);
        return queryData.writeToJson();
    }


}
