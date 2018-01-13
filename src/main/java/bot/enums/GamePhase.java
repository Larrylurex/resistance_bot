package bot.enums;

import lombok.Getter;

import java.util.Arrays;

public enum GamePhase {

    UNKNOWN(-1),
    START(0),
    REGISTRATION(1),
    ROUND_PICK_USER(2),
    ROUND_VOTE(3),
    ROUND_PLAY(4),
    END(5);

    @Getter
    private int code;

    GamePhase(int code) {
        this.code = code;
    }

    public static GamePhase getPhaseByCode(int code) {
        return Arrays.stream(GamePhase.values())
                .filter(p -> p.code == code)
                .findAny().orElse(null);
    }

}
