package bot.dao;

import bot.entities.GameInfo;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MapGameInfoDao implements GameInfoDao {

    private Map<Long, GameInfo> storage = new HashMap<>();

    @Override
    public GameInfo getByChatId(long chatId) {
        return storage.getOrDefault(chatId, new GameInfo(chatId));
    }

    @Override
    public void save(GameInfo gameInfo) {
        storage.put(gameInfo.getChatId(), gameInfo);
    }

    @Override
    public void clearChatData(long chatId) {
        storage.remove(chatId);
    }
}
