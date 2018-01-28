package bot.ai.mind.data;

import bot.enums.Vote;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PlayerInfo {

    public static final int MAX_PERCENTAGE = 100;
    public static final int MIN_PERCENTAGE = 0;

    private String login;

    public void setSpyProbability(int spyProbability) {
        int totalSpyProbability = Math.min(Math.max(MIN_PERCENTAGE , spyProbability), MAX_PERCENTAGE);
        this.spyProbability = totalSpyProbability;
    }

    private int spyProbability;
    private List<List<String>> leaderChoice = new ArrayList<>();
    private List<MissionResult> missionResults = new ArrayList<>();
    private List<VoteResult> voteResults = new ArrayList<>();

    public PlayerInfo(String login) {
        this.login = login;
        this.spyProbability = 50;
    }

    public void addLeaderChoiceResult(List<String> team) {
        leaderChoice.add(team);
    }

    public void addMissionResult(List<String> team, int redCardsCount) {
        missionResults.add(new MissionResult(team, redCardsCount));
    }

    public void addVoteResultResult(List<String> team, Vote vote) {
        voteResults.add(new VoteResult(team, vote));
    }


    @AllArgsConstructor
    @Data
    public static class MissionResult {
        private List<String> team;
        private int redCardsCount;
    }

    @AllArgsConstructor
    @Data
    public static class VoteResult {
        private List<String> team;
        private Vote vote;
    }
}
