package bot.ai.mind.data;

import bot.enums.Vote;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PlayerInfo {

    private String login;
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

    public void recalculateProbability() {
        processMissionResults();
    }

    private void processMissionResults() {
        for( MissionResult result: missionResults){
            if(result.team.contains(login)){
                if(result.redCardsCount > 0)
                    spyProbability += (int)(30L * result.redCardsCount / result.team.size());
                else {
                    spyProbability -= 5;
                }
            }
        }

    }

    @AllArgsConstructor
    private class MissionResult {
        private List<String> team;
        private int redCardsCount;
    }

    @AllArgsConstructor
    private class VoteResult {
        private List<String> team;
        private Vote vote;
    }
}
