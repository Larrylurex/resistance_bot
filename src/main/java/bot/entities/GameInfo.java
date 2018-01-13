package bot.entities;

import bot.enums.GamePhase;
import bot.enums.MissionCard;
import bot.enums.Vote;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class GameInfo {

    private final Long chatId;
    private GamePhase phase = GamePhase.START;
    private int step;
    private int round;
    private Set<Player> players = new HashSet<>();
    private Boolean[] roundResults = new Boolean[5];

    public GameInfo(Long chatId) {
        this.chatId = chatId;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void setRoundResult(boolean result) {
        roundResults[round] = result;
    }

    public boolean getRoundResult() {
        return roundResults[round];
    }

    public int getRoundNumber() {
        return round + 1;
    }

    public void newStep() {
        step++;
        players.forEach(this::resetPlayer);
    }

    public void newRound() {
        round++;
        step = 0;
        players.forEach(this::resetPlayer);

    }

    private void resetPlayer(Player player) {
        player.setCard(MissionCard.NONE);
        player.setVote(Vote.NONE);
        player.setOnMission(false);
    }
}
