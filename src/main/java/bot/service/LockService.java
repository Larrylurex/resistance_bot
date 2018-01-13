package bot.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class LockService {

    private Map<Long, ReentrantLock> locks = new ConcurrentHashMap<>();

    public ReentrantLock getLock(long chatId) {
        locks.putIfAbsent(chatId, new ReentrantLock());
        return locks.get(chatId);
    }

    public void removeLock(long chatId) {
        locks.remove(chatId);
    }

}
