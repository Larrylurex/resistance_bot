package bot.notification.event;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class IntroductionEvent extends AbstractGameEvent{
    private List<String> players;

    public IntroductionEvent(List<String> players, int round) {
        super(round);
        this.players = players;
    }
}
