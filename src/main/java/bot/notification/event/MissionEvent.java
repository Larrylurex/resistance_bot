package bot.notification.event;

import lombok.Data;

import java.util.List;

@Data
public class MissionEvent implements GameEvent {

    private List<String> team;
    private int redCardsCount;
}
