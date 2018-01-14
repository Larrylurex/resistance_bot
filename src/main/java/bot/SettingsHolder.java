package bot;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

public class SettingsHolder {

    public static final String BOT_NAME = "The_Resistance_Game_Bot";
    public static final String BOT_TOKEN = "520738837:AAGew20dH6V4wacKpc3S53apl1GtQELf-j4";
    public static final int MIN_PLAYERS = 5;
    public static final int MAX_PLAYERS = 10;

    private static final Map<Integer, Integer> spiesAmongPlayers = new HashMap<>();

    static {
        spiesAmongPlayers.put(5, 2);
        spiesAmongPlayers.put(6, 2);
        spiesAmongPlayers.put(7, 3);
        spiesAmongPlayers.put(8, 3);
        spiesAmongPlayers.put(9, 3);
        spiesAmongPlayers.put(10, 4);
    }

    private static final Map<MissionKey, MissionSettings> playersInMission = new HashMap<>();

    static {
        playersInMission.put(new MissionKey(0, 5), new MissionSettings(2));
        playersInMission.put(new MissionKey(0, 6), new MissionSettings(2));
        playersInMission.put(new MissionKey(0, 7), new MissionSettings(2));
        playersInMission.put(new MissionKey(0, 8), new MissionSettings(3));
        playersInMission.put(new MissionKey(0, 9), new MissionSettings(3));
        playersInMission.put(new MissionKey(0, 10), new MissionSettings(3));

        playersInMission.put(new MissionKey(1, 5), new MissionSettings(3));
        playersInMission.put(new MissionKey(1, 6), new MissionSettings(3));
        playersInMission.put(new MissionKey(1, 7), new MissionSettings(3));
        playersInMission.put(new MissionKey(1, 8), new MissionSettings(4));
        playersInMission.put(new MissionKey(1, 9), new MissionSettings(4));
        playersInMission.put(new MissionKey(1, 10), new MissionSettings(4));

        playersInMission.put(new MissionKey(2, 5), new MissionSettings(2));
        playersInMission.put(new MissionKey(2, 6), new MissionSettings(4));
        playersInMission.put(new MissionKey(2, 7), new MissionSettings(3));
        playersInMission.put(new MissionKey(2, 8), new MissionSettings(4));
        playersInMission.put(new MissionKey(2, 9), new MissionSettings(4));
        playersInMission.put(new MissionKey(2, 10), new MissionSettings(4));

        playersInMission.put(new MissionKey(3, 5), new MissionSettings(3));
        playersInMission.put(new MissionKey(3, 6), new MissionSettings(3));
        playersInMission.put(new MissionKey(3, 7), new MissionSettings(4, 2));
        playersInMission.put(new MissionKey(3, 8), new MissionSettings(5, 2));
        playersInMission.put(new MissionKey(3, 9), new MissionSettings(5, 2));
        playersInMission.put(new MissionKey(3, 10), new MissionSettings(5, 2));

        playersInMission.put(new MissionKey(4, 5), new MissionSettings(3));
        playersInMission.put(new MissionKey(4, 6), new MissionSettings(4));
        playersInMission.put(new MissionKey(4, 7), new MissionSettings(4));
        playersInMission.put(new MissionKey(4, 8), new MissionSettings(5));
        playersInMission.put(new MissionKey(4, 9), new MissionSettings(5));
        playersInMission.put(new MissionKey(4, 10), new MissionSettings(5));
    }

    public static int getSpiesCount(int numberOfPlayers) {
        return spiesAmongPlayers.get(numberOfPlayers);
    }

    public static int getMissionersCount(int mission, int numberOfPlayers) {
        return playersInMission.get(new MissionKey(mission, numberOfPlayers)).numberOfMissioners;
    }

    public static int getRedCardsCountToWin(int mission, int numberOfPlayers) {
        return playersInMission.get(new MissionKey(mission, numberOfPlayers)).numberOfSpiesCards;
    }

    @Data
    @AllArgsConstructor
    private static class MissionKey {
        int missionNumber;
        int numberOfPlayers;
    }

    @Data
    @AllArgsConstructor
    private static class MissionSettings {
        int numberOfMissioners;
        int numberOfSpiesCards = 1;

        public MissionSettings(int numberOfMissioners) {
            this.numberOfMissioners = numberOfMissioners;
        }
    }
}
