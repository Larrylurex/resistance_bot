package bot.dao;

import bot.entities.GameInfo;

public interface GameInfoDao {

    GameInfo getByChatId(long chatId);

    void save(GameInfo gameInfo);

    void clearChatData(long chatId);
}
