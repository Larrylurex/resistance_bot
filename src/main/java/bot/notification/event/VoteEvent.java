package bot.notification.event;

import bot.enums.Vote;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class VoteEvent implements GameEvent {
    private List<String> team;
    private Map<String, Vote> votes;
}
