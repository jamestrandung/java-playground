package com.james.playground.telegram.bot.method;

import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiValidationException;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelegramGenericBotApiMethod {
  private TelegramMethodTypeEnum type;

  private String chatId;
  private Long userId;

  private Integer untilDate;
  private Boolean revokeMessages;

  public static <T extends Serializable, M extends BotApiMethod<T>> TelegramGenericBotApiMethod fromBotApiMethod(M method)
      throws TelegramApiValidationException {
    Objects.requireNonNull(method);
    method.validate();

    TelegramMethodTypeEnum type = TelegramMethodTypeEnum.fromBotApiMethod(method);

    return switch (Objects.requireNonNull(type)) {
      case GET_CHAT_MEMBER -> {
        GetChatMember getChatMember = (GetChatMember) method;
        yield TelegramGenericBotApiMethod.builder()
            .type(type)
            .chatId(getChatMember.getChatId())
            .userId(getChatMember.getUserId())
            .build();
      }
      case BAN_CHAT_MEMBER -> {
        BanChatMember banChatMember = (BanChatMember) method;
        yield TelegramGenericBotApiMethod.builder()
            .type(type)
            .chatId(banChatMember.getChatId())
            .userId(banChatMember.getUserId())
            .untilDate(banChatMember.getUntilDate())
            .revokeMessages(banChatMember.getRevokeMessages())
            .build();
      }
    };
  }

  public BotApiMethod<?> toBotApiMethod() throws TelegramApiValidationException {
    var result = switch (this.type) {
      case GET_CHAT_MEMBER -> GetChatMember.builder()
          .chatId(this.chatId)
          .userId(this.userId)
          .build();
      case BAN_CHAT_MEMBER -> BanChatMember.builder()
          .chatId(this.chatId)
          .userId(this.userId)
          .untilDate(this.untilDate)
          .revokeMessages(this.revokeMessages)
          .build();
    };

    result.validate();

    return result;
  }
}
