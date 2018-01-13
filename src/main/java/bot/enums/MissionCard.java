package bot.enums;

import lombok.Getter;

import java.util.Arrays;

public enum MissionCard {
    NONE(-1),
    BLUE(0),
    RED(1);

    @Getter
    private int code;

    MissionCard(int code) {
        this.code = code;
    }

    public static MissionCard getMissionCardByCode(int code) {
        return Arrays.stream(MissionCard.values())
                .filter(p -> p.code == code)
                .findAny().orElse(null);
    }
}
