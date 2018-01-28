package bot.ai.mind;

import bot.entities.Player;
import bot.notification.event.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractMind implements Mind {

    protected Player me;

    public AbstractMind(Player me) {
        this.me = me;
    }

    @Override
    public void processEvent(GameEvent event) {
        log.info("Process {} event", event.getClass().getSimpleName());
        if(event instanceof VoteEvent){
            processVoteEvent((VoteEvent) event);
        } else if (event instanceof MissionEvent) {
            processMissionEvent((MissionEvent)event);
        } else if (event instanceof TeamChoosingEvent) {
            processTeamChoosingEvent((TeamChoosingEvent)event);
        } else if (event instanceof IntroductionEvent) {
            processIntroductionEvent((IntroductionEvent) event);
        }
        recalculateProbability(event.getRound());
    }

    protected void recalculateProbability(int round){}

    protected void processIntroductionEvent(IntroductionEvent event){}

    protected void processTeamChoosingEvent(TeamChoosingEvent event){}

    protected void processMissionEvent(MissionEvent event){}

    protected void processVoteEvent(VoteEvent event){}
}
