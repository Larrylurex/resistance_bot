package bot.handler.factory;

import bot.handler.UpdateHandler;
import org.telegram.telegrambots.api.objects.Update;

public interface HandlerFactory {

    UpdateHandler getHandler(Update update);
}
