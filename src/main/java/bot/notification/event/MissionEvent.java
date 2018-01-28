package bot.notification.event;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class MissionEvent extends AbstractGameEvent {
    private List<String> team;
    private int redCardsCount;

    public MissionEvent(int round){
        super(round);
    }


}
