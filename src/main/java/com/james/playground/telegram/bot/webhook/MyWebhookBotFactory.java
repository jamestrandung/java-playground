package com.james.playground.telegram.bot.webhook;

import com.james.playground.telegram.bot.WebhookBotFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.abilitybots.api.db.DBContext;
import org.telegram.telegrambots.abilitybots.api.db.MapDBContext;

@Component
public class MyWebhookBotFactory implements WebhookBotFactory<MyWebhookBot> {
  private final Map<String, DBContext> dbContexts = new ConcurrentHashMap<>();
  private String botPath = "myTestBotPath";
  private String secretToken = "mySecretToken";

  @Override
  public SecuredWebhookBotReference findBotReference(String botPath) {
    return SecuredWebhookBotReference.builder()
        .id(123L)
        .botPath(botPath)
        .botUsername(MyWebhookBot.BOT_USERNAME)
        .secretToken(this.secretToken)
        .build();
  }

  @Override
  public MyWebhookBot createBot(String botUsername, String botPath) {
    DBContext dbContext = this.dbContexts.computeIfAbsent(botUsername, MapDBContext::onlineInstance);

    return new MyWebhookBot(this.botPath, dbContext);
  }

  public void refresh(String botPath, String secretToken) {
    this.botPath = botPath;
    this.secretToken = secretToken;
  }
}
