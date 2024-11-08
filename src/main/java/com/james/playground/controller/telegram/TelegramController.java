package com.james.playground.controller.telegram;

import com.james.playground.telegram.bot.TelegramUtils;
import com.james.playground.telegram.bot.TelegramUtils.BotConfigs;
import com.james.playground.telegram.bot.WebhookBotFactory.SecuredWebhookBotReference;
import com.james.playground.telegram.bot.WebhookBotRegistry;
import com.james.playground.telegram.bot.webhook.MyWebhookBot;
import com.james.playground.telegram.bot.webhook.MyWebhookBotFactory;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@RestController
@RequestMapping("/telegram")
public class TelegramController {
  @Autowired
  WebhookBotRegistry<MyWebhookBot> myWebhookBotRegistry;
  @Autowired
  MyWebhookBotFactory myWebhookBotFactory;

  @PostMapping("/callback/{botPath}")
  public void onWebhookUpdate(
      HttpServletRequest request,
      @PathVariable String botPath,
      @RequestBody Update update
  ) {
    String secretToken = request.getHeader("X-Telegram-Bot-Api-Secret-Token");
    log.info("Received webhook update, bot path: {}, token: {}, data: {}", botPath, secretToken, update);

    this.myWebhookBotRegistry.onWebhookUpdateReceived(botPath, secretToken, update);
  }

  @PostMapping("/webhook")
  public void refreshWebhook(
      @RequestParam String botPath,
      @RequestParam String secretToken
  ) {
    this.myWebhookBotFactory.refresh(botPath, secretToken);

    SecuredWebhookBotReference reference = this.myWebhookBotFactory.findBotReference(botPath);
    MyWebhookBot bot = this.myWebhookBotFactory.createBot("botId", reference.getBotPath());

    this.myWebhookBotRegistry.registerWebhook(reference, bot.getBotToken());
  }

  @PostMapping("/webhook/delete")
  public void clearWebhook(
      @RequestParam String botPath
  ) {

    SecuredWebhookBotReference reference = this.myWebhookBotFactory.findBotReference(botPath);
    MyWebhookBot bot = this.myWebhookBotFactory.createBot("botId", reference.getBotPath());

    TelegramUtils.clearWebhook(BotConfigs.builder()
                                   .botToken(bot.getBotToken())
                                   .botPath(reference.getBotPath())
                                   .build());
  }
}
