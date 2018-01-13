package bot.service;

import bot.ai.AIPlayer;
import bot.dao.GameInfoDao;
import bot.entities.GameInfo;
import bot.entities.Player;
import bot.enums.GamePhase;
import bot.enums.MissionCard;
import bot.enums.Vote;
import bot.exception.ProcessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.api.objects.User;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GameInfoService {

    @Autowired
    private GameInfoDao gameInfoDao;
    @Autowired
    private SettingsService settingsService;

    public GameInfo getGameInfoByChatId(long chatId) {
        return gameInfoDao.getByChatId(chatId);
    }

    public void clearChatData(long chatId) {
        gameInfoDao.clearChatData(chatId);
    }

    public void save(GameInfo gameInfo) {
        gameInfoDao.save(gameInfo);
    }

    public void savePlayer(Player player, GameInfo gameInfo) {
        gameInfo.addPlayer(player);
    }

    public Player getPlayerByLoginOrThrowException(String login, GameInfo gameInfo) {
        String message = String.format("Couldn't find user: %s", login);
        return gameInfo.getPlayers().stream()
                .filter(p -> p.getLogin().equals(login))
                .findAny()
                .orElseThrow(() -> new ProcessException(message));
    }

    public List<Player> getPlayersInMission(GameInfo gameInfo) {
        return gameInfo.getPlayers().stream()
                .filter(Player::isOnMission)
                .collect(Collectors.toList());
    }

    public List<Player> getSpies(GameInfo gameInfo) {
        return gameInfo.getPlayers().stream()
                .filter(Player::isSpy)
                .collect(Collectors.toList());
    }

    public long getNumberOfMissioners(GameInfo gameInfo) {
        return gameInfo.getPlayers().stream()
                .filter(Player::isOnMission)
                .count();
    }

    public Player getLeader(GameInfo gameInfo) {
        return gameInfo.getPlayers().stream()
                .filter(Player::isLeader)
                .findAny()
                .orElseThrow(() -> new ProcessException("Couldn't find leader"));
    }

    public void changeLeader(GameInfo gameInfo) {
        Player currentLeader = getLeader(gameInfo);
        currentLeader.setLeader(false);
        int curLeaderOrder = currentLeader.getORDER();
        int nextLeaderOrder = curLeaderOrder + 1 >= gameInfo.getPlayers().size() ? 0 : curLeaderOrder + 1;
        Player nextLeader = gameInfo.getPlayers().stream()
                .filter(p -> p.getORDER() == nextLeaderOrder)
                .findAny().get();
        nextLeader.setLeader(true);
        nextLeader.setVote(Vote.FOR);
    }

    public void startGame(GameInfo gameInfo) {
        gameInfo.setPhase(GamePhase.ROUND_PICK_USER);
        Set<Player> players = gameInfo.getPlayers();
        pickLeader(players);
        pickSpies(players);
    }

    private void pickLeader(Set<Player> players) {
        List<Player> randomPlayers = getRandomPlayers(1, players);
        Player leader = randomPlayers.get(0);
        leader.setLeader(true);
        leader.setVote(Vote.FOR);
    }

    private void pickSpies(Set<Player> players) {
        int spiesCount = settingsService.getSpiesCount(players.size());
        List<Player> randomPlayers = getRandomPlayers(spiesCount, players);
        randomPlayers.forEach(p -> p.setSpy(true));
    }

    private List<Player> getRandomPlayers(int number, Set<Player> players) {
        List<Player> playersList = Arrays.asList(players.toArray(new Player[0]));
        Collections.shuffle(playersList);
        return playersList.subList(0, number);
    }

    public boolean areAllPlayersVoted(GameInfo gameInfo) {
        return gameInfo.getPlayers().stream()
                .filter(player -> !player.isBot())
                .map(Player::getVote)
                .noneMatch(v -> v == Vote.NONE);
    }

    public boolean areAllMissionersMadeAMove(GameInfo gameInfo) {
        return gameInfo.getPlayers().stream()
                .filter(Player::isOnMission)
                .map(Player::getCard)
                .noneMatch(v -> v == MissionCard.NONE);
    }

    public int getBotsCount(GameInfo gameInfo) {
        return (int) gameInfo.getPlayers().stream().filter(Player::isBot).count();
    }

    public List<Player> getRealPlayers(GameInfo gameInfo) {
        return gameInfo.getPlayers().stream()
                .filter(player -> !player.isBot())
                .collect(Collectors.toList());
    }

    public List<AIPlayer> getBots(GameInfo gameInfo) {
        return gameInfo.getPlayers().stream()
                .filter(Player::isBot)
                .map(AIPlayer.class::cast)
                .collect(Collectors.toList());
    }
}
