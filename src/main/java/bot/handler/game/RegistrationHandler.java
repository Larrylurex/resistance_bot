package bot.handler.game;

import bot.ai.AIPlayer;
import bot.entities.GameInfo;
import bot.entities.Player;
import bot.enums.GamePhase;
import bot.exception.ProcessException;
import bot.handler.game.data.CallbackQueryData;
import bot.service.SettingsService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class RegistrationHandler extends AbstractUpdateHandler {


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
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String command = queryData.getData();

        List<BotApiMethod<? extends Serializable>> result = new ArrayList<>();
        if (command.equals("register")) {
            result.addAll(processRegistrationMessage(gameInfo, user, messageId));
        } else if (command.equals("add_bot")) {
            result.addAll(processAddBotMessage(gameInfo, messageId));
        } else if (command.equals("play")) {
            result.addAll(processOverRegistrationMessage(gameInfo, messageId));
        } else {
            throw new ProcessException("Unknown request command: " + command);
        }
        return result;
    }

    private Collection<? extends BotApiMethod<? extends Serializable>> processAddBotMessage(GameInfo gameInfo, int messageId) {
        Player player = getBotPlayer(gameInfo);
        return registerPlayer(gameInfo, player, messageId);
    }

    private Player getBotPlayer(GameInfo gameInfo) {
        Player player = new AIPlayer(gameInfo.getPlayers().size());
        player.setLogin(AIPlayer.NAMES[gameInfoService.getBotsCount(gameInfo)]);
        return player;
    }

    private Collection<? extends BotApiMethod<? extends Serializable>> processRegistrationMessage(GameInfo gameInfo, User user, int messageId) {
        Player player = convertUserToPlayer(user, gameInfo);
        return registerPlayer(gameInfo, player, messageId);
    }

    private Player convertUserToPlayer(User user, GameInfo gameInfo) {
        Player player = new Player(gameInfo.getPlayers().size());
        player.setId(user.getId());
        player.setLogin(user.getUserName());
        return player;
    }

    private Collection<? extends BotApiMethod<? extends Serializable>> registerPlayer(GameInfo gameInfo, Player player, int messageId) {
        List<BotApiMethod<? extends Serializable>> result = new ArrayList<>();
        result.add(getSimpleMessage(gameInfo.getChatId(), messageService.getYouAreInMessage(player.getLogin())));
        gameInfoService.savePlayer(player, gameInfo);
        if (isEveryoneRegistered(gameInfo)) {
            result.add(getSimpleMessage(gameInfo.getChatId(), messageService.getCantBeMorePlayersMessage()));
            result.addAll(startGame(gameInfo, messageId));
        }
        return result;
    }

    private boolean isEveryoneRegistered(GameInfo gameInfo) {
        return gameInfo.getPlayers().size() == SettingsService.MAX_PLAYERS;
    }

    private List<BotApiMethod<? extends Serializable>> processOverRegistrationMessage(GameInfo gameInfo, int messageId) {
        List<BotApiMethod<? extends Serializable>> result = new ArrayList<>();
        if (isEnoughPlayers(gameInfo)) {
            result.addAll(startGame(gameInfo, messageId));
        } else {
            SendMessage notEnoughPlayersMessage = getSimpleMessage(gameInfo.getChatId(), messageService.getNotEnoughPlayersMessage(gameInfo.getPlayers().size()));
            result.add(notEnoughPlayersMessage);
        }
        return result;
    }

    private boolean isEnoughPlayers(GameInfo gameInfo) {
        return gameInfo.getPlayers().size() >= SettingsService.MIN_PLAYERS;
    }

    private List<BotApiMethod<? extends Serializable>> startGame(GameInfo gameInfo, int messageId) {
        List<BotApiMethod<? extends Serializable>> result = new ArrayList<>();
        gameInfoService.startGame(gameInfo);
        Long chatId = gameInfo.getChatId();
        EditMessageReplyMarkup removeKeyboard = getRemoveKeyboardMessage(messageId, chatId);
        SendMessage registrationIsOver = getSimpleMessage(chatId, messageService.getRegistrationIsOverMessage());
        SendMessage round = getSimpleMessage(chatId, messageService.getRoundMessage(gameInfo.getRoundNumber()));
        SendMessage whoAmIMessage = getSimpleMessage(chatId, messageService.getWhoAreYouMessage());
        List<SendMessage> whoAmIMessages = getWhoAmIMessages(gameInfo);
        SendMessage youAreLeaderMessage = getYouAreLeaderMessage(gameInfo);

        result.add(removeKeyboard);
        result.add(registrationIsOver);
        result.add(round);
        result.add(whoAmIMessage);
        result.addAll(whoAmIMessages);
        result.add(youAreLeaderMessage);
        return result;
    }

    private List<SendMessage> getWhoAmIMessages(GameInfo gameInfo) {
        List<SendMessage> messages = new ArrayList<>();
        List<Player> spies = gameInfoService.getSpies(gameInfo);
        List<Player> realPlayers = gameInfoService.getRealPlayers(gameInfo);
        for (Player p : realPlayers) {
            String whoAmI;
            if (p.isSpy()) {
                whoAmI = messageService.getYouAreSpyMessage(p, spies);
            } else {
                whoAmI = messageService.getYouAreResistanceMessage();
            }
            messages.add(getSimpleMessage(p.getId(), whoAmI));
        }
        return messages;
    }
}
