package com.james.playground.telegram.bot;

import org.telegram.abilitybots.api.bot.AbilityWebhookBot;

public class MyWebhookBot extends AbilityWebhookBot {
  private static final String BOT_USERNAME = "AnotherLearningBot";
  private static final String BOT_TOKEN = "7595376834:AAHaszid97hejVBIOgnQKb7feOd4nAy62MQ";

  public MyWebhookBot(String botPath) {
    super(BOT_TOKEN, BOT_USERNAME, botPath);
  }

  @Override
  public long creatorId() {
    return 6374401545L;
  }
}
