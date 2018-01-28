package bot.notification.event;

import lombok.Data;

import java.util.List;

@Data
public class TeamChoosingEvent implements GameEvent {
    private String leader;
    private List<String> team;
}
