package bot.handler.game;

import bot.entities.GameInfo;
import bot.entities.Player;
import bot.enums.GamePhase;
import bot.enums.MissionCard;
import bot.exception.ValidationException;
import bot.handler.game.data.CallbackQueryData;
import bot.service.game.MissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Component
public class MissionHandler extends AbstractUpdateHandler {

    @Autowired
    private MissionService missionService;


    @Override
    protected GamePhase getPhase() {
        return GamePhase.ROUND_PLAY;
    }

    @Override
    protected long getChatId(Update update) {
        return update.getCallbackQuery().getMessage().getChatId();
    }

    @Override
    protected void validateQuery(Update update, GameInfo gameInfo) {
        super.validateQuery(update, gameInfo);
        Player sender = getSender(update, gameInfo);
        if (!sender.isOnMission()) {
            String message = String.format("%s is not in this Mission", sender.getLogin());
            throw new ValidationException(message);
        }
    }

    @Override
    protected List<BotApiMethod<? extends Serializable>> processUpdate(Update update, GameInfo gameInfo) {
        String dataJson = update.getCallbackQuery().getData();
        CallbackQueryData queryData = CallbackQueryData.parseQueryData(dataJson);
        String data = queryData.getData();

        MissionCard card = MissionCard.getMissionCardByCode(Integer.parseInt(data));
        Player player = getSender(update, gameInfo);
        player.setCard(card);

        List<BotApiMethod<? extends Serializable>> result = new ArrayList<>();
        result.add(getPlayerPickedCardMessage(gameInfo.getChatId(), player));
        if (missionService.areAllMissionersMadeAMove(gameInfo)) {
            result.addAll(missionService.processMissionResults(gameInfo));
        }
        return result;
    }

    private SendMessage getPlayerPickedCardMessage(long chatId, Player player) {
        return commonMessageHolder.getSimpleMessage(chatId, messageService.getPlayerMadeAMoveMessage(player));
    }
}
