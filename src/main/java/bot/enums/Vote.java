package bot.enums;

import lombok.Getter;

import java.util.Arrays;

public enum Vote {
    NONE(-1, "-"),
    AGAINST(0, "against"),
    FOR(1, "for");

    @Getter
    private int code;
    @Getter
    private String message;

    Vote(int code, String message) {
        this.code = code;
        this.message = message;

    }

    public static Vote getVoteByCode(int code) {
        return Arrays.stream(Vote.values())
                .filter(p -> p.code == code)
                .findAny().orElse(null);
    }
}
