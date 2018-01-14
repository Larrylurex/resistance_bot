package bot.handler.game;

import bot.entities.GameInfo;
import bot.entities.Player;
import bot.enums.GamePhase;
import bot.enums.Vote;
import bot.exception.ValidationException;
import bot.handler.game.data.CallbackQueryData;
import bot.service.game.VoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Component
public class VoteHandler extends AbstractUpdateHandler {

    @Autowired
    private VoteService voteService;

    @Override
    protected GamePhase getPhase() {
        return GamePhase.ROUND_VOTE;
    }

    @Override
    protected long getChatId(Update update) {
        return update.getCallbackQuery().getMessage().getChatId();
    }

    @Override
    protected void validateQuery(Update update, GameInfo gameInfo) {
        super.validateQuery(update, gameInfo);
        Player sender = getSender(update, gameInfo);
        if (sender.isLeader()) {
            String message = String.format("%s is a leader and cannot vote", sender.getLogin());
            throw new ValidationException(message);
        }
    }

    @Override
    protected List<BotApiMethod<? extends Serializable>> processUpdate(Update update, GameInfo gameInfo) {
        String dataJson = update.getCallbackQuery().getData();
        CallbackQueryData queryData = CallbackQueryData.parseQueryData(dataJson);
        String data = queryData.getData();

        Vote vote = Vote.getVoteByCode(Integer.parseInt(data));
        String senderName = update.getCallbackQuery().getFrom().getUserName();
        Player player = gameInfoService.getPlayerByLoginOrThrowException(senderName, gameInfo);
        player.setVote(vote);

        List<BotApiMethod<? extends Serializable>> result = new ArrayList<>();
        result.add(getPlayerVotedMessage(gameInfo.getChatId(), player));
        if (!voteService.needToVote(gameInfo)) {
            result.addAll(voteService.processVote(gameInfo));
        }

        return result;
    }

    private SendMessage getPlayerVotedMessage(long chatId, Player player) {
        return commonMessageHolder.getSimpleMessage(chatId, messageService.getPlayerVotedMessage(player));
    }
}
