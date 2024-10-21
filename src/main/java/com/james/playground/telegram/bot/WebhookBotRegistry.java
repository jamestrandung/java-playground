package com.james.playground.telegram.bot;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.james.playground.telegram.bot.WebhookBotFactory.SecuredWebhookBotReference;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiValidationException;
import org.telegram.telegrambots.meta.generics.TelegramBot;
import org.telegram.telegrambots.meta.generics.WebhookBot;

/**
 * Combination of TelegramBotsApi and ServerlessWebhook
 */
@Slf4j
public class WebhookBotRegistry<B extends WebhookBot> {
  // bot path -> SecuredWebhookBotReference
  private final Cache<String, SecuredWebhookBotReference> redisReferenceCache = Caffeine.newBuilder()
      .expireAfterWrite(Duration.ofSeconds(300))
      .build();
  // bot ID -> WebhookBot
  private final Cache<String, B> localBotCache = Caffeine.newBuilder()
      .expireAfterAccess(Duration.ofSeconds(120))
      .expireAfterWrite(Duration.ofMinutes(10))
      .build();

  WebhookBotFactory<B> factory;

  public WebhookBotRegistry(WebhookBotFactory<B> factory) {
    this.factory = factory;
  }

  public void registerWebhook(SecuredWebhookBotReference reference, B bot) {
    if (!this.validateBotUsernameAndToken(bot)) {
      throw new RuntimeException("Bot token and username can't be empty");
    }

    try {
      SetWebhook request = SetWebhook.builder()
          .url("https://a853-203-127-162-66.ngrok-free.app/telegram")
          .maxConnections(100)
          .dropPendingUpdates(false)
          .secretToken(reference.getSecretToken())
          .build();

      bot.setWebhook(request);

      this.redisReferenceCache.put(bot.getBotPath(), reference);

    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private boolean validateBotUsernameAndToken(TelegramBot telegramBot) {
    return StringUtils.isNotEmpty(telegramBot.getBotToken()) && StringUtils.isNotEmpty(telegramBot.getBotUsername());
  }

  public BotApiMethod<?> onWebhookUpdateReceived(String botPath, String secretToken, Update update) {
    WebhookBot bot = this.findBot(botPath, secretToken);

    try {
      BotApiMethod<?> response = bot.onWebhookUpdateReceived(update);
      if (response != null) {
        response.validate();
      }

      return response;

    } catch (TelegramApiValidationException ex) {
      throw new RuntimeException(ex);
    }
  }

  public B findBot(String botPath, String secretToken) {
    SecuredWebhookBotReference reference = this.redisReferenceCache.get(botPath, path -> this.findBotReference(botPath, secretToken));

    return this.localBotCache.get(reference.botUsername, botUsername -> this.buildBot(botUsername, botPath));
  }

  SecuredWebhookBotReference findBotReference(String botPath, String secretToken) {
    SecuredWebhookBotReference reference = this.factory.findBotReference(botPath);

    if (reference == null) {
      throw new RuntimeException("Bot reference not found");
    }

    if (!StringUtils.equals(reference.getSecretToken(), secretToken)) {
      throw new RuntimeException("Invalid secret token");
    }

    return reference;
  }

  B buildBot(String botUsername, String botPath) {
    B bot = this.factory.createBot(botUsername, botPath);

    if (bot == null) {
      throw new RuntimeException("Bot not found");
    }

    bot.onRegister();

    return bot;
  }
}
