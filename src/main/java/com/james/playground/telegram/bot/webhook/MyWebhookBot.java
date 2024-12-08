package com.james.playground.telegram.bot.webhook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.groupadministration.ApproveChatJoinRequest;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updates.DeleteWebhook;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
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
          ReplyKeyboardMarkup replyKeyboard = this.createReplyKeyboard(
              List.of(
                  Collections.singletonList("sing"),
                  Collections.singletonList("stop")
              )
          );

          this.replyWithKeyboard(ctx.chatId(), "Hello, I'm a bot!", replyKeyboard);
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
        .action(ctx -> this.silent.send("Goodbye, I'm really dying...", ctx.chatId()))
        .build();
  }

  public Reply replyToCallbackQuery() {
    BiConsumer<BaseAbilityBot, Update> action = (abilityBot, update) -> {
      log.info("Handling callback query update: {}", update);

      this.silent.execute(
          AnswerCallbackQuery.builder()
              .callbackQueryId(update.getCallbackQuery().getId())
              //              .text("Callback text")
              .cacheTime(5) // 2
              .build()
      );


    };

    return Reply.of(action, Flag.CALLBACK_QUERY);
  }

  public Reply replyToInlineQuery() {
    BiConsumer<BaseAbilityBot, Update> action = (abilityBot, update) -> {
      log.info("Handling inline query update: {}", update);

      this.silent.send("Got inline query update", AbilityUtils.getChatId(update));
    };

    return Reply.of(action, Flag.INLINE_QUERY);
  }

  public Reply replyToChatJoinRequest() {
    BiConsumer<BaseAbilityBot, Update> action = (abilityBot, update) -> {
      log.info("Handling chat join update: {}", update);

      this.silent.execute(
          ApproveChatJoinRequest.builder()
              .chatId(update.getChatJoinRequest().getChat().getId())
              .userId(update.getChatJoinRequest().getUser().getId())
              .build()
      );

      this.silent.send("Got chat join update", AbilityUtils.getChatId(update));
    };

    return Reply.of(action, Flag.CHAT_JOIN_REQUEST);
  }

  public Ability replyToEverythingElse() {
    return Ability
        .builder()
        .name("default")
        .info("Default ability")
        .locality(Locality.ALL)
        .privacy(Privacy.PUBLIC)
        .action(ctx -> {
          log.info("Handling random update: {}", ctx.update());
          log.info("Random update arguments: {}", List.of(ctx.arguments()));

          if (!ctx.update().hasMessage()) {
            return;
          }

          String text = ctx.update().getMessage().getText();
          if (StringUtils.isBlank(text)) {
            return;
          }

          if (text.equals("run")) {
            InlineKeyboardMarkup inlineKeyboard = this.createInlineKeyboard(
                List.of(
                    List.of(
                        InlineKeyboardButton.builder()
                            .text("callback")
                            .callbackData("abc")
                            .build(),
                        InlineKeyboardButton.builder()
                            .text("website")
                            .url("https://www.google.com")
                            .build()
                    ),
                    List.of(
                        InlineKeyboardButton.builder()
                            .text("switch inline")
                            .switchInlineQuery("sing")
                            .build(),
                        InlineKeyboardButton.builder()
                            .text("switch inline current chat")
                            .switchInlineQueryCurrentChat("switch_inline_current_chat")
                            .build()
                    )
                )
            );

            this.replyWithKeyboard(ctx.chatId(), "Hello, I'm a bot!", inlineKeyboard);
          }

          if (text.equals("photo")) {
            InlineKeyboardMarkup inlineKeyboard = this.createInlineKeyboard(
                List.of(
                    List.of(
                        InlineKeyboardButton.builder()
                            .text("callback")
                            .callbackData("abc")
                            .build(),
                        InlineKeyboardButton.builder()
                            .text("website")
                            .url("https://www.google.com")
                            .build()
                    ),
                    List.of(
                        InlineKeyboardButton.builder()
                            .text("switch inline")
                            .switchInlineQuery("sing")
                            .build(),
                        InlineKeyboardButton.builder()
                            .text("switch inline current chat")
                            .switchInlineQueryCurrentChat("switch_inline_current_chat")
                            .build()
                    )
                )
            );

            this.replyWithPhotoKeyboard(ctx.chatId(), "Hello, I'm a PHOTO bot!", inlineKeyboard);
          }

          if (text.equals("sing")) {
            this.silent.send("La la la...", ctx.chatId());
          }

          if (text.equals("stop")) {
            this.replyWithKeyboard(ctx.chatId(), "Goodbye, I'm dying!", new ReplyKeyboardRemove(true));
          }
        })
        .build();
  }

  ReplyKeyboardMarkup createReplyKeyboard(List<List<String>> commands) {
    List<KeyboardRow> rows = new ArrayList<>();
    for (List<String> commandRow : commands) {
      KeyboardRow row = new KeyboardRow();
      for (String command : commandRow) {
        row.add(command);
      }

      rows.add(row);
    }

    return new ReplyKeyboardMarkup(rows);
  }

  InlineKeyboardMarkup createInlineKeyboard(List<List<InlineKeyboardButton>> buttons) {
    List<InlineKeyboardRow> rows = new ArrayList<>();
    for (List<InlineKeyboardButton> buttonRow : buttons) {
      InlineKeyboardRow row = new InlineKeyboardRow();
      row.addAll(buttonRow);
      rows.add(row);
    }

    return new InlineKeyboardMarkup(rows);
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

  private void replyWithPhotoKeyboard(
      long chatId,
      String text,
      ReplyKeyboard keyboard
  ) {
    SendPhoto sendPhoto = SendPhoto.builder()
        .chatId(chatId)
        .photo(new InputFile("https://m.media-amazon.com/images/I/61eo0WHaqzL.jpg"))
        .caption(text)
        .replyMarkup(keyboard)
        .build();

    try {
      this.telegramClient.execute(sendPhoto);
    } catch (TelegramApiException e) {
      throw new RuntimeException(e);
    }
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
