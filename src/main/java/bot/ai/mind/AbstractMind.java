package bot.ai.mind;

import bot.SettingsHolder;
import bot.entities.GameInfo;
import bot.entities.Player;
import bot.notification.event.*;
import bot.util.ResistanceUtils;

import java.util.List;

public abstract class AbstractMind implements Mind {

    protected Player me;

    public AbstractMind(Player me) {
        this.me = me;
    }

    @Override
    public void processEvent(GameEvent event) {
        if(event instanceof VoteEvent){
            processVoteEvent((VoteEvent) event);
        } else if (event instanceof MissionEvent) {
            processMissionEvent((MissionEvent)event);
        } else if (event instanceof TeamChoosingEvent) {
            processTeamChoosingEvent((TeamChoosingEvent)event);
        } else if (event instanceof IntroductionEvent) {
            processIntroductionEvent((IntroductionEvent) event);
        }
        think();
    }

    protected void think(){}

    protected void processIntroductionEvent(IntroductionEvent event){}

    protected void processTeamChoosingEvent(TeamChoosingEvent event){}

    protected void processMissionEvent(MissionEvent event){}

    protected void processVoteEvent(VoteEvent event){}
}
