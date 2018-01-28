package bot.notification.event;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class TeamChoosingEvent extends AbstractGameEvent {
    private String leader;
    private List<String> team;

    public TeamChoosingEvent(int round){
        super(round);
    }
}
