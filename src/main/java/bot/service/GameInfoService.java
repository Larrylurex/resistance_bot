package bot.service;

import bot.ai.AIPlayer;
import bot.dao.GameInfoDao;
import bot.entities.GameInfo;
import bot.entities.Player;
import bot.exception.ProcessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GameInfoService {

    @Autowired
    private GameInfoDao gameInfoDao;

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

    public Optional<Player> getLeader(GameInfo gameInfo) {
        return gameInfo.getPlayers().stream()
                .filter(Player::isLeader)
                .findAny();
    }

    public Player getLeaderOrThrowException(GameInfo gameInfo) {
        return getLeader(gameInfo)
                .orElseThrow(() -> new ProcessException("Leader's not found"));
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
