package com.james.playground.telegram.bot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityWebhookBot;

public interface WebhookBotFactory<B extends AbilityWebhookBot> {
  SecuredWebhookBotReference findBotReference(String botPath);

  B createBot(String botUsername, String botPath);

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  class SecuredWebhookBotReference {
    Long id;
    String botUsername;
    String botType;
    String botPath;
    String secretToken;
  }
}
