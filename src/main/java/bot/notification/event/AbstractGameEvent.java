package bot.notification.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class AbstractGameEvent implements GameEvent {
    private int round;
}
