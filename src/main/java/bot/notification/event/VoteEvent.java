package bot.notification.event;

import bot.enums.Vote;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class VoteEvent extends AbstractGameEvent {
    private List<String> team;
    private Map<String, Vote> votes;

    public VoteEvent(int round){
        super(round);
    }
}
