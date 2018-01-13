package bot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

@Component
public class BotRunner {

    @Autowired
    private ResistanceBotPolling botPolling;

    public static void main(String[] args) {
        ApiContextInitializer.init();

        ApplicationContext ctx = new AnnotationConfigApplicationContext("bot");
        BotRunner bean = ctx.getBean(BotRunner.class);
        bean.run();
    }

    public void run() {
        // Instantiate Telegram Bots API
        TelegramBotsApi botsApi = new TelegramBotsApi();

        // Register our bot
        try {
            botsApi.registerBot(botPolling);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
