package bot.handler.game;

import bot.entities.GameInfo;
import bot.entities.Player;
import bot.enums.GamePhase;
import bot.enums.Vote;
import bot.exception.ValidationException;
import bot.handler.game.data.CallbackQueryData;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.methods.BotApiMethod;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class VoteHandler extends AbstractUpdateHandler {


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
        if (gameInfoService.areAllPlayersVoted(gameInfo)) {
            botService.vote(gameInfo);
            int messageId = update.getCallbackQuery().getMessage().getMessageId();
            result.add(getRemoveKeyboardMessage(messageId, gameInfo.getChatId()));
            result.add(getVotesMessages(gameInfo));
            result.addAll(processVoteResult(gameInfo));
        }

        return result;
    }

    private SendMessage getPlayerVotedMessage(long chatId, Player player) {
        return getSimpleMessage(chatId, messageService.getPlayerVotedMessage(player));
    }


    private SendMessage getVotesMessages(GameInfo gameInfo) {
        return getSimpleMessage(gameInfo.getChatId(),
                messageService.getVoteResultMessage(gameInfo.getPlayers())).setParseMode("Markdown");
    }

    private List<SendMessage> processVoteResult(GameInfo gameInfo) {
        List<SendMessage> result = new ArrayList<>();
        if (isTeamConfirmed(gameInfo)) {
            result.add(getStartMissionMessage(gameInfo));
            gameInfo.setPhase(GamePhase.ROUND_PLAY);
        } else {
            gameInfo.newStep();
            if (checkStepsOver(gameInfo)) {
                result.addAll(gameOver(gameInfo, messageService.getRunOutOfStepsMessage(), false));
            } else {
                result.add(startNewGameCycle(gameInfo));
            }
        }
        return result;
    }

    private boolean checkStepsOver(GameInfo gameInfo) {
        return gameInfo.getStep() >= 5;
    }

    private boolean isTeamConfirmed(GameInfo gameInfo) {
        Map<Vote, Long> votes = gameInfo.getPlayers().stream()
                .map(Player::getVote)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return votes.getOrDefault(Vote.FOR, 0L) >
                votes.getOrDefault(Vote.AGAINST, 0L);
    }

    private SendMessage getStartMissionMessage(GameInfo gameInfo) {
        return getMessageWithKeyboard(gameInfo.getChatId(),
                messageService.getStartMissionMessage(gameInfoService.getPlayersInMission(gameInfo)),
                keyboardHolderService.getMissionKeyboard());
    }

}
