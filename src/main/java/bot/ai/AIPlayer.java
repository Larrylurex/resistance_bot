package bot.ai;

import bot.ai.mind.Mind;
import bot.ai.mind.ResistanceMind;
import bot.ai.mind.SpyMind;
import bot.entities.Player;

public class AIPlayer extends Player {

    public static final String NAMES[] = new String[]{
            "Bot_Bob",
            "Bot_Bill",
            "Bot_Vasya",
            "Bot_Petya",
            "Bot_Grisha",
            "Bot_Sanyok",
            "Bot_Kolyan",
            "Bot_Stepan",
            "Bot_Petrovich"};

    private Mind mind = new ResistanceMind();

    public AIPlayer(int ORDER) {
        super(ORDER);
        setBot(true);
    }

    @Override
    public void setSpy(boolean spy) {
        super.setSpy(spy);
        mind = new SpyMind();
    }

    public void vote(){
        setVote(mind.vote());
    }


}
