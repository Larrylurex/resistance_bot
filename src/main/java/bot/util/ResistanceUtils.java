package bot.util;

import bot.entities.Player;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@UtilityClass
public class ResistanceUtils {

    public static List<Player> getRandomPlayers(int number, Collection<Player> players) {
        if (number > players.size()) {
            throw new IndexOutOfBoundsException();
        }
        List<Player> playersList = Arrays.asList(players.toArray(new Player[0]));
        Collections.shuffle(playersList);

        return playersList.subList(0, number);
    }
}
