package com.james.playground.telegram.bot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.generics.WebhookBot;

public interface WebhookBotFactory<B extends WebhookBot> {
  SecuredWebhookBotReference findBotReference(String botPath);

  B createBot(String botId);

  @Getter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  class SecuredWebhookBotReference {
    Long id;
    String botPath;
    String botUsername;
    String secretToken;
  }
}
