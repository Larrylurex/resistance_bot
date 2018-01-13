package bot.ai.mind;

import bot.enums.Vote;

public class SpyMind implements Mind {

    @Override
    public Vote vote() {
        return Vote.AGAINST;
    }
}
