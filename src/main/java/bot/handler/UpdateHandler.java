package bot.handler;

import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.objects.Update;

import java.io.Serializable;
import java.util.List;

public interface UpdateHandler {

    List<BotApiMethod<? extends Serializable>> handleUpdate(Update update);
}
