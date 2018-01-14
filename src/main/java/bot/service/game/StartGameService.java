package bot.service.game;

import bot.SettingsHolder;
import bot.ai.AIPlayer;
import bot.entities.GameInfo;
import bot.entities.Player;
import bot.enums.GamePhase;
import bot.enums.Vote;
import bot.service.AIBotService;
import bot.service.CommonMessageHolderService;
import bot.service.GameInfoService;
import bot.service.MessageService;
import bot.util.ResistanceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendMessage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class StartGameService {

    @Autowired
    private GameInfoService gameInfoService;
    @Autowired
    private AIBotService botService;
    @Autowired
    private CommonMessageHolderService commonMessageHolder;
    @Autowired
    private MessageService messageService;
    @Autowired
    private VoteService voteService;

    public List<BotApiMethod<? extends Serializable>> startGame(GameInfo gameInfo) {
        List<BotApiMethod<? extends Serializable>> result = new ArrayList<>();
        Set<Player> players = gameInfo.getPlayers();
        pickSpies(players);

        Long chatId = gameInfo.getChatId();
        SendMessage registrationIsOver = commonMessageHolder.getSimpleMessage(chatId, messageService.getRegistrationIsOverMessage());
        SendMessage whoAmIMessage = commonMessageHolder.getSimpleMessage(chatId, messageService.getWhoAreYouMessage());
        List<SendMessage> whoAmIMessages = getWhoAmIMessages(gameInfo);
        List<BotApiMethod<? extends Serializable>> startRoundMessages = startRound(gameInfo);

        result.add(registrationIsOver);
        result.add(whoAmIMessage);
        result.addAll(whoAmIMessages);
        result.addAll(startRoundMessages);
        return result;
    }

    private void pickSpies(Set<Player> players) {
        int spiesCount = SettingsHolder.getSpiesCount(players.size());
        List<Player> randomPlayers = ResistanceUtils.getRandomPlayers(spiesCount, players);
        randomPlayers.forEach(p -> p.setSpy(true));
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
            messages.add(commonMessageHolder.getSimpleMessage(p.getId(), whoAmI));
        }
        return messages;
    }

    public List<BotApiMethod<? extends Serializable>> startRound(GameInfo gameInfo) {
        List<BotApiMethod<? extends Serializable>> result = new ArrayList<>();
        SendMessage round = commonMessageHolder.getSimpleMessage(gameInfo.getChatId(), messageService.getRoundMessage(gameInfo.getRoundNumber()));
        List<BotApiMethod<? extends Serializable>> gameCycleMessages = startGameCycle(gameInfo);
        result.add(round);
        result.addAll(gameCycleMessages);
        return result;
    }

    public List<BotApiMethod<? extends Serializable>> startGameCycle(GameInfo gameInfo) {
        List<BotApiMethod<? extends Serializable>> result = new ArrayList<>();
        gameInfo.setPhase(GamePhase.ROUND_PICK_USER);
        pickLeader(gameInfo);
        SendMessage youAreLeaderMessage = commonMessageHolder.getYouAreLeaderMessage(gameInfo);
        List<BotApiMethod<? extends Serializable>> botLeaderMessages = processBotLeader(gameInfo);
        result.add(youAreLeaderMessage);
        result.addAll(botLeaderMessages);
        return result;
    }

    private List<BotApiMethod<? extends Serializable>> processBotLeader(GameInfo gameInfo) {
        List<BotApiMethod<? extends Serializable>> result = new ArrayList<>();
        Player leader = gameInfoService.getLeaderOrThrowException(gameInfo);
        if (leader.isBot()) {
            botService.chooseTeam(gameInfo);
            result.addAll(voteService.startVote(gameInfo));
        }
        return result;
    }

    private void pickLeader(GameInfo gameInfo) {
        Optional<Player> leader = gameInfoService.getLeader(gameInfo);
        Player newLeader;
        if (leader.isPresent()) {
            newLeader = getNextLeader(gameInfo, leader.get());
        } else {
            newLeader = getNewLeader(gameInfo);
        }

        newLeader.setLeader(true);
        newLeader.setVote(Vote.FOR);
    }

    private Player getNewLeader(GameInfo gameInfo) {
        List<Player> randomPlayers = ResistanceUtils.getRandomPlayers(1, gameInfo.getPlayers());
        return randomPlayers.get(0);
    }

    private Player getNextLeader(GameInfo gameInfo, Player currentLeader) {
        currentLeader.setLeader(false);
        int curLeaderOrder = currentLeader.getORDER();
        int nextLeaderOrder = curLeaderOrder + 1 >= gameInfo.getPlayers().size() ? 0 : curLeaderOrder + 1;
        Player nextLeader = gameInfo.getPlayers().stream()
                .filter(p -> p.getORDER() == nextLeaderOrder)
                .findAny().get();
        return nextLeader;

    }
}
