package com.james.playground.telegram.bot.webhook;

import com.james.playground.telegram.bot.WebhookBotRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebhookConfigurations {
  @Bean
  public WebhookBotRegistry<MyWebhookBot> myWebhookBotRegistry(MyWebhookBotFactory myWebhookBotFactory) {
    return new WebhookBotRegistry<>(myWebhookBotFactory);
  }
}
