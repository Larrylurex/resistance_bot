package bot.entities;

import bot.enums.MissionCard;
import bot.enums.Vote;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "login")
public class Player {

    private final int ORDER;
    private Integer id;
    private String login;
    private boolean spy;
    private boolean onMission;
    private boolean leader;
    private boolean bot;
    private Vote vote = Vote.NONE;
    private MissionCard card = MissionCard.NONE;


}
