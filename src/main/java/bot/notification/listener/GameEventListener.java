package bot.notification.listener;

import bot.notification.event.GameEvent;

public interface GameEventListener<T extends GameEvent> {

    void processEvent(T event);
}
