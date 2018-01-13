package bot.handler.factory;

import bot.enums.GamePhase;
import bot.exception.ValidationException;
import bot.handler.*;
import bot.handler.game.data.CallbackQueryData;
import bot.handler.game.*;
import bot.handler.service.ServiceMessagesHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;

import java.util.Optional;

@Component
public class HandlerFactoryImpl implements HandlerFactory {

    @Autowired
    private StartHandler startHandler;
    @Autowired
    private RegistrationHandler registrationHandler;
    @Autowired
    private PickPlayerHandler pickPlayerHandler;
    @Autowired
    private VoteHandler voteHandler;
    @Autowired
    private MissionHandler roundHandler;
    @Autowired
    private EndGameHandler endGameHandler;
    @Autowired
    private ServiceMessagesHandler serviceMessagesHandler;

    @Override
    public UpdateHandler getHandler(Update update) {
        GamePhase phase = getGamePhase(update);
        UpdateHandler handler;
        switch (phase) {
            case START:
                handler = startHandler;
                break;
            case REGISTRATION:
                handler = registrationHandler;
                break;
            case ROUND_PICK_USER:
                handler = pickPlayerHandler;
                break;
            case ROUND_VOTE:
                handler = voteHandler;
                break;
            case ROUND_PLAY:
                handler = roundHandler;
                break;
            case END:
                handler = endGameHandler;
                break;
            default:
                handler = serviceMessagesHandler;
        }
        return handler;
    }

    private GamePhase getGamePhase(Update update) {
        GamePhase phase = GamePhase.UNKNOWN;
        if (isStartPhase(update.getMessage())) {
            phase = GamePhase.START;
        } else if (update.hasCallbackQuery()) {
            phase = getPhaseFromCallbackQuery(update.getCallbackQuery().getData());
        }
        return phase;
    }

    private boolean isStartPhase(Message message) {
        return Optional.ofNullable(message)
                .map(Message::getText)
                .filter(text -> text.startsWith("/play"))
                .isPresent();
    }

    private GamePhase getPhaseFromCallbackQuery(String queryData) {
        CallbackQueryData data = CallbackQueryData.parseQueryData(queryData);
        int phase = data.getPhase();
        return GamePhase.getPhaseByCode(phase);
    }

}
