package bot.handler.game;

import bot.SettingsHolder;
import bot.ai.AIPlayer;
import bot.entities.GameInfo;
import bot.entities.Player;
import bot.enums.GamePhase;
import bot.exception.ProcessException;
import bot.handler.game.data.CallbackQueryData;
import bot.service.game.StartGameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class RegistrationHandler extends AbstractUpdateHandler {

    @Autowired
    private StartGameService startGameService;

    @Override
    protected GamePhase getPhase() {
        return GamePhase.REGISTRATION;
    }

    @Override
    protected long getChatId(Update update) {
        return update.getCallbackQuery().getMessage().getChatId();
    }

    @Override
    protected List<BotApiMethod<? extends Serializable>> processUpdate(Update update, GameInfo gameInfo) {

        String dataJson = update.getCallbackQuery().getData();
        CallbackQueryData queryData = CallbackQueryData.parseQueryData(dataJson);
        User user = update.getCallbackQuery().getFrom();
        String command = queryData.getData();

        List<BotApiMethod<? extends Serializable>> result = new ArrayList<>();
        if (command.equals("register")) {
            result.addAll(processRegistrationMessage(gameInfo, user));
        } else if (command.equals("add_bot")) {
            result.addAll(processAddBotMessage(gameInfo));
        } else if (command.equals("play")) {
            result.addAll(processOverRegistrationMessage(gameInfo));
        } else {
            throw new ProcessException("Unknown request command: " + command);
        }
        return result;
    }

    private Collection<? extends BotApiMethod<? extends Serializable>> processRegistrationMessage(GameInfo gameInfo, User user) {
        Player player = convertUserToPlayer(user, gameInfo);
        return registerPlayer(gameInfo, player);
    }

    private Player convertUserToPlayer(User user, GameInfo gameInfo) {
        Player player = new Player(gameInfo.getPlayers().size());
        player.setId(user.getId());
        player.setLogin(user.getUserName());
        return player;
    }

    private Collection<? extends BotApiMethod<? extends Serializable>> processAddBotMessage(GameInfo gameInfo) {
        Player player = getBotPlayer(gameInfo);
        return registerPlayer(gameInfo, player);
    }

    private Player getBotPlayer(GameInfo gameInfo) {
        Player player = new AIPlayer(gameInfo.getPlayers().size());
        player.setLogin(AIPlayer.NAMES[gameInfoService.getBotsCount(gameInfo)]);
        return player;
    }

    private List<? extends BotApiMethod<? extends Serializable>> registerPlayer(GameInfo gameInfo, Player player) {
        List<BotApiMethod<? extends Serializable>> result = new ArrayList<>();
        result.add(commonMessageHolder.getSimpleMessage(gameInfo.getChatId(),
                messageService.getYouAreInMessage(player.getLogin())));
        gameInfoService.savePlayer(player, gameInfo);
        if (isEveryoneRegistered(gameInfo)) {
            result.add(commonMessageHolder.getSimpleMessage(gameInfo.getChatId(),
                    messageService.getCantBeMorePlayersMessage()));
            result.addAll(startGameService.startGame(gameInfo));
        }
        return result;
    }

    private boolean isEveryoneRegistered(GameInfo gameInfo) {
        return gameInfo.getPlayers().size() == SettingsHolder.MAX_PLAYERS;
    }

    private List<BotApiMethod<? extends Serializable>> processOverRegistrationMessage(GameInfo gameInfo) {
        List<BotApiMethod<? extends Serializable>> result = new ArrayList<>();
        if (isEnoughPlayers(gameInfo)) {
            result.addAll(startGameService.startGame(gameInfo));
        } else {
            SendMessage notEnoughPlayersMessage = commonMessageHolder.getSimpleMessage(gameInfo.getChatId(),
                    messageService.getNotEnoughPlayersMessage(gameInfo.getPlayers().size()));
            result.add(notEnoughPlayersMessage);
        }
        return result;
    }

    private boolean isEnoughPlayers(GameInfo gameInfo) {
        return gameInfo.getPlayers().size() >= SettingsHolder.MIN_PLAYERS;
    }

}
