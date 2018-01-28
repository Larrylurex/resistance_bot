package bot.notification.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class IntroductionEvent implements GameEvent{
    List<String> players;
}
