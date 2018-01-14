package bot.service.game;

import bot.entities.GameInfo;
import bot.service.CommonMessageHolderService;
import bot.service.GameInfoService;
import bot.service.KeyboardHolderService;
import bot.service.LockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.api.methods.BotApiMethod;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Service
public class GameOverService {

    @Autowired
    private GameInfoService gameInfoService;
    @Autowired
    private LockService lockService;
    @Autowired
    private CommonMessageHolderService commonMessageHolder;
    @Autowired
    private KeyboardHolderService keyboardHolderService;

    public List<BotApiMethod<? extends Serializable>> gameOver(GameInfo gameInfo, String reason, boolean resistanceWon) {
        long chatId = gameInfo.getChatId();
        gameInfoService.clearChatData(chatId);
        lockService.removeLock(chatId);
        List<BotApiMethod<? extends Serializable>> result = new ArrayList<>();
        result.add(commonMessageHolder.getSimpleMessage(chatId, reason));
        String message = resistanceWon ? "RESISTANCE WON THE GAME" : "SPIES WON THE GAME";
        result.add(commonMessageHolder.getMessageWithKeyboard(chatId, message, keyboardHolderService.getNewGameKeyboard()));
        return result;
    }
}
