package com.james.playground.telegram.bot.longpolling;

import java.util.List;
import java.util.function.BiConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Flag;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.abilitybots.api.util.AbilityUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

@Slf4j
@Component
public class MyLongPollingBot extends AbilityBot {
  private static final String BOT_USERNAME = "okx_faq_bot";
  private static final String BOT_TOKEN = "7635664233:AAFceN-ZD8Lx4Mm1LkF8iEVzwhD5A7IwLT0";

  public MyLongPollingBot() {
    super(BOT_TOKEN, BOT_USERNAME);
    log.info("MyBot is starting...");
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
    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(chatId);
    sendMessage.setText(text);
    sendMessage.setReplyMarkup(keyboard);

    this.silent.execute(sendMessage);
  }
}
