package com.james.playground.telegram.bot.webhook;

import java.util.List;
import java.util.function.BiConsumer;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.abilitybots.api.bot.AbilityWebhookBot;
import org.telegram.telegrambots.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.telegrambots.abilitybots.api.db.DBContext;
import org.telegram.telegrambots.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.abilitybots.api.objects.Flag;
import org.telegram.telegrambots.abilitybots.api.objects.Locality;
import org.telegram.telegrambots.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.abilitybots.api.objects.Reply;
import org.telegram.telegrambots.abilitybots.api.util.AbilityUtils;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.DeleteWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
public class MyWebhookBot extends AbilityWebhookBot {
  public static final String BOT_USERNAME = "AnotherLearningBot";
  public static final String BOT_TOKEN = "7595376834:AAHaszid97hejVBIOgnQKb7feOd4nAy62MQ";

  public MyWebhookBot(String botPath, DBContext dbContext) {
    super(new OkHttpTelegramClient(BOT_TOKEN), BOT_USERNAME, botPath, dbContext);
  }

  @Override
  public long creatorId() {
    return 6374401545L;
  }

  public Ability start() {
    return Ability
        .builder()
        .name("start")
        .info("Starting the bot")
        .locality(Locality.ALL)
        .privacy(Privacy.PUBLIC)
        .action(ctx -> {
          KeyboardRow row = new KeyboardRow();
          row.add("sing");
          row.add("stop");

          this.replyWithKeyboard(ctx.chatId(), "Hello, I'm a bot!", new ReplyKeyboardMarkup(List.of(row)));
        })
        .build();
  }

  public Ability stop() {
    return Ability
        .builder()
        .name("stop")
        .info("Killing the bot")
        .locality(Locality.ALL)
        .privacy(Privacy.PUBLIC)
        .action(ctx -> this.silent.send("Goodbye, I'm dying...", ctx.chatId()))
        .build();
  }

  public Reply replyToButtons() {
    BiConsumer<BaseAbilityBot, Update> action = (abilityBot, update) -> {
      log.info("Handling update: {}", update);

      String text = update.getMessage().getText();

      if (text.equals("/start")) {
        KeyboardRow row = new KeyboardRow();
        row.add("sing");
        row.add("stop");

        this.replyWithKeyboard(AbilityUtils.getChatId(update), "Hello, I'm a bot!", new ReplyKeyboardMarkup(List.of(row)));
      }

      if (text.equals("stop")) {
        this.replyWithKeyboard(AbilityUtils.getChatId(update), "Goodbye, I'm dying!", new ReplyKeyboardRemove(true));
      }

      if (text.equals("sing")) {
        this.silent.send("La la la...", AbilityUtils.getChatId(update));
      }
    };

    // Last argument determines when to trigger this method.
    // Since it always returns true, other Ability method
    // will not be triggered.
    return Reply.of(action, Flag.TEXT, update -> true);
  }

  private void replyWithKeyboard(
      long chatId,
      String text,
      ReplyKeyboard keyboard
  ) {
    SendMessage sendMessage = SendMessage.builder()
        .chatId(chatId)
        .text(text)
        .replyMarkup(keyboard)
        .build();

    this.silent.execute(sendMessage);
  }

  @Override
  public void runDeleteWebhook() {
    try {
      this.telegramClient.execute(new DeleteWebhook());
    } catch (TelegramApiException e) {
      log.info("Error deleting webhook");
    }
  }

  @Override
  public void runSetWebhook() {

  }
}
