package com.james.playground.telegram.bot.method;

import lombok.AllArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;

@AllArgsConstructor
public enum TelegramMethodTypeEnum {
  GET_CHAT_MEMBER("getChatMember"),
  BAN_CHAT_MEMBER("banChatMember");

  private final String methodName;

  public static TelegramMethodTypeEnum fromBotApiMethod(BotApiMethod<?> botApiMethod) {
    for (TelegramMethodTypeEnum method : TelegramMethodTypeEnum.values()) {
      if (method.methodName.equals(botApiMethod.getMethod())) {
        return method;
      }
    }

    return null;
  }
}
