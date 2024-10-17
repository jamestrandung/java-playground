package com.james.playground.controller.telegram;

import com.james.playground.telegram.bot.MyLongPollingBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@RestController
@RequestMapping("/telegram")
public class TelegramController {
  @Autowired
  MyLongPollingBot myLongPollingBot;

  @PostMapping
  public void onWebhookUpdate(@RequestBody Update update) {
    log.info("Received webhook update: {}", update);
    this.myLongPollingBot.onUpdateReceived(update);
  }
}
