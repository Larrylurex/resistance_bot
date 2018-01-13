package bot.service;

import bot.entities.Player;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;

import java.util.Set;

public interface KeyboardHolderService {
    InlineKeyboardMarkup getRegistrationKeyboard();

    InlineKeyboardMarkup getPlayersPickerKeyboard(Set<Player> players);

    InlineKeyboardMarkup getVotingKeyboard();

    InlineKeyboardMarkup getMissionKeyboard();

    ReplyKeyboard getNewGameKeyboard();
}
