package bot.handler.game;

import bot.SettingsHolder;
import bot.entities.GameInfo;
import bot.entities.Player;
import bot.enums.GamePhase;
import bot.exception.ValidationException;
import bot.handler.game.data.CallbackQueryData;
import bot.service.game.VoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Component
public class PickPlayerHandler extends AbstractUpdateHandler {

    @Autowired
    private VoteService voteService;

    @Override
    protected GamePhase getPhase() {
        return GamePhase.ROUND_PICK_USER;
    }

    @Override
    protected long getChatId(Update update) {
        return update.getCallbackQuery().getMessage().getChatId();
    }

    @Override
    protected void validateQuery(Update update, GameInfo gameInfo) {
        super.validateQuery(update, gameInfo);
        Player sender = getSender(update, gameInfo);
        if (!sender.isLeader()) {
            String message = String.format("%s is not a leader", sender.getLogin());
            throw new ValidationException(message);
        }
    }

    @Override
    protected List<BotApiMethod<? extends Serializable>> processUpdate(Update update, GameInfo gameInfo) {
        String dataJson = update.getCallbackQuery().getData();
        CallbackQueryData queryData = CallbackQueryData.parseQueryData(dataJson);
        String playerName = queryData.getData();

        Player player = gameInfoService.getPlayerByLoginOrThrowException(playerName, gameInfo);
        player.setOnMission(true);

        List<BotApiMethod<? extends Serializable>> result = new ArrayList<>();
        result.add(getPlayerChosenMessage(gameInfo, player));
        if (areAllMissionersChosen(gameInfo)) {
            result.addAll(voteService.startVote(gameInfo));
        }
        return result;
    }

    private SendMessage getPlayerChosenMessage(GameInfo gameInfo, Player player) {
        return commonMessageHolder.getSimpleMessage(gameInfo.getChatId(),
                messageService.getPlayerChosenMessage(player));
    }

    private boolean areAllMissionersChosen(GameInfo gameInfo) {
        int numberOfPlayers = gameInfo.getPlayers().size();
        int round = gameInfo.getRound();
        int numberOfMissioners = SettingsHolder.getMissionersCount(round, numberOfPlayers);
        return gameInfoService.getNumberOfMissioners(gameInfo) == numberOfMissioners;
    }

}
