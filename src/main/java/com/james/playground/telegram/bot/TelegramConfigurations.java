package com.james.playground.telegram.bot;

import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.starter.TelegramBotInitializer;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Configuration
public class TelegramConfigurations {
  @Autowired
  private ApplicationContext context;

  @PostConstruct
  public void init() throws TelegramApiException {
    //    TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
    //    telegramBotsApi.registerBot(this.context.getBean(MyLongPollingBot.class));
  }

  @Bean
  @ConditionalOnMissingBean(TelegramBotsLongPollingApplication.class)
  public TelegramBotsLongPollingApplication telegramBotsApplication() {
    return new TelegramBotsLongPollingApplication();
  }

  @Bean
  @ConditionalOnMissingBean
  public TelegramBotInitializer telegramBotInitializer(
      TelegramBotsLongPollingApplication telegramBotsApplication,
      ObjectProvider<List<SpringLongPollingBot>> longPollingBots
  ) {
    return new TelegramBotInitializer(
        telegramBotsApplication,
        longPollingBots.getIfAvailable(Collections::emptyList)
    );
  }
}
