package bot.ai;

import bot.ai.mind.Mind;
import bot.ai.mind.ResistanceMind;
import bot.ai.mind.SpyMind;
import bot.entities.GameInfo;
import bot.entities.Player;

public class AIPlayer extends Player implements Brainy {

    public static final String NAMES[] = new String[]{
            "Bot_Andrew",
            "Bot_Bob",
            "Bot_Charley",
            "Bot_Din",
            "Bot_Eugen",
            "Bot_Freddie",
            "Bot_Greg",
            "Bot_Hue",
            "Bot_Ivan"};

    private Mind mind = new ResistanceMind(this);

    public AIPlayer(int ORDER) {
        super(ORDER);
        setBot(true);
    }

    @Override
    public void setSpy(boolean spy) {
        super.setSpy(spy);
        mind = new SpyMind(this);
    }

    public void vote(GameInfo gameInfo){
        setVote(mind.vote(gameInfo));
    }


    public void chooseTeam(GameInfo gameInfo) {
        mind.chooseTeam(gameInfo);
    }

    public void goOnMission(GameInfo gameInfo) {
        setCard(mind.goOnMission(gameInfo));
    }

    @Override
    public Mind getMind() {
        return mind;
    }
}
