package bot.ai.mind;

import bot.enums.Vote;

public class ResistanceMind implements Mind{
    @Override
    public Vote vote() {
        return Vote.FOR;
    }
}
