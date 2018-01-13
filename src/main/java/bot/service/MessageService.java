package bot.service;

import bot.entities.GameInfo;
import bot.entities.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageService {

    @Autowired
    private GameInfoService gameInfoService;
    @Autowired
    private SettingsService settingsService;

    public String getRegisterMessage() {
        return "Register to the game\n" +
                "You need at least 5 players, 10 max\n" +
                "Press PLAY when everyone's registered";
    }

    public String getYouAreSpyMessage(Player player, List<Player> spies) {
        String otherSpies = spies.stream()
                .filter(p -> !p.equals(player))
                .map(Player::getLogin)
                .collect(Collectors.joining(", "));
        return String.format("%s and you are the spies", otherSpies);
    }

    public String getYouAreResistanceMessage() {
        return "You're the resistance member";
    }

    public String getWhoAreYouMessage() {
        return "I've sent you private message whether you're a spy or a resistance. Check it out!";
    }

    public String getRoundMessage(int round) {
        return String.format("ROUND %d", round);
    }

    public String getRegistrationIsOverMessage() {
        return "Registration is over";
    }

    public String getYouAreLeaderMessage(GameInfo gameInfo) {
        String leader = gameInfoService.getLeader(gameInfo).getLogin();
        int numberOfPlayers = gameInfo.getPlayers().size();
        int round = gameInfo.getRound();
        int numberOfMissioners = settingsService.getMissionersCount(round, numberOfPlayers);
        return String.format("%s - you're the leader! Pick %d players to be on a mission.", leader, numberOfMissioners);
    }

    public String getYouAreInMessage(String player) {
        return String.format("%s's in", player);
    }

    public String getPlayerVotedMessage(Player player) {
        return String.format("%s has voted", player.getLogin());
    }

    public String getVoteResultMessage(Collection<Player> players) {
        return players.stream()
                .map(player -> String.format("%s has vote *%s*", player.getLogin(), player.getVote().getMessage()))
                .collect(Collectors.joining("\n", "Vote results:\n", ""));
    }

    public String getRunOutOfStepsMessage() {
        return "You've switched leader 5 times in this round";
    }

    public String getStartMissionMessage(List<Player> playersInRound) {
        return String.format("%s make your move", formatPlayerList(playersInRound));
    }

    public String getLeaderChoiceMessage(Player leader, List<Player> playersInRound) {
        return String.format("%s chose %s to play this round. Let's vote!", leader.getLogin(), formatPlayerList(playersInRound));
    }

    private String formatPlayerList(Collection<Player> players) {
        return players.stream()
                .map(Player::getLogin)
                .collect(Collectors.joining(", "));
    }

    public String getPlayerMadeAMoveMessage(Player player) {
        return String.format("%s has made a move", player.getLogin());
    }

    public String getCardsCountMessage(long blue, long red) {
        return String.format("There are %d blue card(s) and %d red one(s)", blue, red);
    }

    public String getWonRoundMessage(boolean resistanceWon, int round) {
        String winner = resistanceWon ? "Resistance" : "Spies";
        return String.format("%s won round %d! Congratulations!", winner, round);
    }

    public String getThreeRoundsWon(Boolean resistanceWon) {
        String winner = resistanceWon ? "resistance" : "spies";
        return String.format("3 round were won by %s", winner);
    }

    public String getPlayerChosenMessage(Player player) {
        return String.format("%s plays this round", player.getLogin());
    }

    public String getNotEnoughPlayersMessage(int numberOfPlayers) {
        return String.format("There should be at least %d players registered\n" +
                "You've got only %d.\n" +
                "Please, invite someone else to play", SettingsService.MIN_PLAYERS, numberOfPlayers);
    }

    public String getCantBeMorePlayersMessage() {
        return String.format("There have been %d players registered already", SettingsService.MAX_PLAYERS);
    }
}
